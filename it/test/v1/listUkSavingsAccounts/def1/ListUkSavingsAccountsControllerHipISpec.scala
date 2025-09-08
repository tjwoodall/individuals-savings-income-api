/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package v1.listUkSavingsAccounts.def1

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import models.errors.SavingsAccountIdFormatError
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.*
import shared.models.errors.*
import shared.services.*
import shared.support.IntegrationBaseSpec

class ListUkSavingsAccountsControllerHipISpec extends IntegrationBaseSpec {

  private trait Test {

    val nino: String             = "AA123456A"
    val savingsAccountId: String = "SAVKB2UVwUTBQGJ"

    val downstreamResponse: JsValue = Json.parse(
      """
        |{
        |  "bbsi": [
        |    {
        |      "incomeSourceId": "000000000000001",
        |      "incomeSourceName": "Bank Account 1",
        |      "startDate": "2025-01-30",
        |      "endDate": "2025-03-30"
        |    },
        |    {
        |      "incomeSourceId": "000000000000002",
        |      "incomeSourceName": "Bank Account 2",
        |      "startDate": "2025-01-30",
        |      "endDate": "2025-03-30"
        |    },
        |    {
        |      "incomeSourceId": "000000000000003",
        |      "incomeSourceName": "Bank Account 3",
        |      "startDate": "2025-01-30",
        |      "endDate": "2025-03-30"
        |    }
        |  ]
        |}
      """.stripMargin
    )

    val mtdResponse: JsValue = Json.parse(
      """
        |{
        |  "savingsAccounts": [
        |    {
        |      "savingsAccountId": "000000000000001",
        |      "accountName": "Bank Account 1"
        |    },
        |    {
        |      "savingsAccountId": "000000000000002",
        |      "accountName": "Bank Account 2"
        |    },
        |    {
        |      "savingsAccountId": "000000000000003",
        |      "accountName": "Bank Account 3"
        |    }
        |  ]
        |}
      """.stripMargin
    )

    private def uri: String = s"/uk-accounts/$nino"

    def downstreamUri: String = s"/itsd/income-sources/v2/$nino"

    private def queryParams: Seq[(String, String)] =
      Seq("savingsAccountId" -> Some(savingsAccountId))
        .collect { case (k, Some(v)) =>
          (k, v)
        }

    def setupStubs(): StubMapping

    def request: WSRequest = {
      setupStubs()
      buildRequest(uri)
        .addQueryStringParameters(queryParams*)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.1.0+json"),
          (AUTHORIZATION, "Bearer 123") // some bearer token
        )
    }

  }

  "Calling the 'list UK Savings Accounts' endpoint" should {
    "return a 200 status code" when {
      "any valid request is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUri, Map("incomeSourceId" -> savingsAccountId), OK, downstreamResponse)
        }

        val response: WSResponse = await(request.get())
        response.status shouldBe OK
        response.json shouldBe mtdResponse
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return error according to spec" when {

      "validation error" when {
        def validationErrorTest(requestNino: String, requestSavingsAccountId: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new Test {

            override val nino: String             = requestNino
            override val savingsAccountId: String = requestSavingsAccountId

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
            }

            val response: WSResponse = await(request.get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }

        val input = Seq(
          ("AA1123A", "SAVKB2UVwUTBQGJ", BAD_REQUEST, NinoFormatError),
          ("AA123456A", "SAVKB2UVwUTBQG", BAD_REQUEST, SavingsAccountIdFormatError)
        )
        input.foreach(args => validationErrorTest.tupled(args))
      }

      "downstream service error" when {
        def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"downstream returns a code $downstreamCode error and status $downstreamStatus" in new Test {

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
              DownstreamStub.onError(
                DownstreamStub.GET,
                downstreamUri,
                Map("incomeSourceId" -> savingsAccountId),
                downstreamStatus,
                errorBody(downstreamCode))
            }

            val response: WSResponse = await(request.get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }

        def errorBody(code: String): String =
          s"""
            |[
            |    {
            |        "errorCode": "$code",
            |        "errorDescription": "error description"
            |    }
            |]
          """.stripMargin

        val input = Seq(
          (BAD_REQUEST, "1215", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "1117", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "1122", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "1007", BAD_REQUEST, SavingsAccountIdFormatError),
          (BAD_REQUEST, "1229", INTERNAL_SERVER_ERROR, InternalError),
          (NOT_FOUND, "5010", NOT_FOUND, NotFoundError)
        )

        input.foreach(args => serviceErrorTest.tupled(args))
      }
    }
  }

}
