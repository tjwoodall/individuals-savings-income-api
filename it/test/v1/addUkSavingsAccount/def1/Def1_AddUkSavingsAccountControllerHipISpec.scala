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

package v1.addUkSavingsAccount.def1

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import models.errors.*
import play.api.http.Status.*
import play.api.libs.json.*
import play.api.libs.ws.WSBodyWritables.writeableOf_JsValue
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.{ACCEPT, AUTHORIZATION}
import shared.models.errors.*
import shared.services.*
import shared.support.IntegrationBaseSpec

class Def1_AddUkSavingsAccountControllerHipISpec extends IntegrationBaseSpec {

  private trait Test {

    val nino: String                     = "AA123456A"
    private val savingsAccountId: String = "SAVKB2UVwUTBQGJ"
    val taxYear: String                  = "2020-21"

    val requestBodyJson: JsValue = Json.parse(
      """
        |{
        |   "accountName": "Shares savings account"
        |}
      """.stripMargin
    )

    val downstreamResponseJson: JsValue = Json.parse(
      s"""
        |{
        |   "incomeSourceId": "$savingsAccountId"
        |}
      """.stripMargin
    )

    val responseJson: JsValue = Json.parse(
      s"""
        |{
        |   "savingsAccountId": "$savingsAccountId"
        |}
      """.stripMargin
    )

    private def uri: String = s"/uk-accounts/$nino"

    def downstreamUri: String = s"/itsd/income-sources/$nino"

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.1.0+json"),
          (AUTHORIZATION, "Bearer 123") // some bearer token
        )
    }

  }

  "calling the 'add uk savings account' endpoint" should {
    "return a 200 status code" when {
      "any valid request is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.POST, downstreamUri, OK, downstreamResponseJson)
        }

        val response: WSResponse = await(request().post(requestBodyJson))
        response.status shouldBe OK
        response.json shouldBe Json.toJson(responseJson)
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return error according to spec" when {

      val validRequestJson: JsValue = Json.parse(
        """
          |{
          |   "accountName": "Shares savings account"
          |}
        """.stripMargin
      )

      val emptyRequestJson: JsValue = JsObject.empty

      val nonValidRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |   "accountName": "Shares savings account!"
          |}
        """.stripMargin
      )

      "validation error" when {
        def validationErrorTest(requestNino: String,
                                requestTaxYear: String,
                                requestBody: JsValue,
                                expectedStatus: Int,
                                expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new Test {

            override val nino: String             = requestNino
            override val taxYear: String          = requestTaxYear
            override val requestBodyJson: JsValue = requestBody

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
            }

            val response: WSResponse = await(request().post(requestBodyJson))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val accountNameError: MtdError = AccountNameFormatError.copy(
          paths = Some(List("/accountName"))
        )

        val input = List(
          ("AA1123A", "2019-20", validRequestJson, BAD_REQUEST, NinoFormatError),
          ("AA123456A", "2019-20", emptyRequestJson, BAD_REQUEST, RuleIncorrectOrEmptyBodyError),
          ("AA123456A", "2019-20", nonValidRequestBodyJson, BAD_REQUEST, accountNameError)
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
              DownstreamStub.onError(DownstreamStub.POST, downstreamUri, downstreamStatus, errorBody(downstreamCode))
            }

            val response: WSResponse = await(request().post(requestBodyJson))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
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

        val input = List(
          (BAD_REQUEST, "1215", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "1000", INTERNAL_SERVER_ERROR, InternalError),
          (UNPROCESSABLE_ENTITY, "1011", BAD_REQUEST, RuleMaximumSavingsAccountsLimitError),
          (UNPROCESSABLE_ENTITY, "1214", BAD_REQUEST, RuleDuplicateAccountNameError)
        )

        input.foreach(args => serviceErrorTest.tupled(args))
      }
    }
  }

}
