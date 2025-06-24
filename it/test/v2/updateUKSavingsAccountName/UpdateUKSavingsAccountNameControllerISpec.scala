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

package v2.updateUKSavingsAccountName

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import models.errors.{AccountNameFormatError, SavingsAccountIdFormatError}
import play.api.http.Status._
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.{ACCEPT, AUTHORIZATION, NO_CONTENT}
import shared.models.errors._
import shared.services.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}
import shared.support.IntegrationBaseSpec
import v2.updateUKSavingsAccountName.fixture.UpdateUKSavingsAccountNameFixtures.{nonValidRequestBodyJson, validRequestJson}

class UpdateUKSavingsAccountNameControllerISpec extends IntegrationBaseSpec {

  "calling the 'update uk savings account name' endpoint" should {
    "return a 204 status code" when {
      "any valid request is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.PUT, downstreamUri, NO_CONTENT, JsObject.empty)
        }

        val response: WSResponse = await(request().put(validRequestJson))
        response.status shouldBe NO_CONTENT
        response.body shouldBe ""
      }
    }

    "return error according to spec" when {
      "validation error" when {
        def validationErrorTest(requestNino: String,
                                requestSavingsAccountId: String,
                                requestBody: JsValue,
                                expectedStatus: Int,
                                expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new Test {

            override val nino: String             = requestNino
            override val savingsAccountId: String = requestSavingsAccountId

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
            }

            val response: WSResponse = await(request().put(requestBody))
            response.status shouldBe expectedStatus
          }
        }
        val input = Seq(
          ("AA1123A", "ABCDE1234567890", validRequestJson, BAD_REQUEST, NinoFormatError),
          ("AA123456A", "ABCDE1234567890", JsObject.empty, BAD_REQUEST, RuleIncorrectOrEmptyBodyError),
          ("AA123456A", "BAD_ACCT_ID", validRequestJson, BAD_REQUEST, SavingsAccountIdFormatError),
          ("AA123456A", "ABCDE1234567890", nonValidRequestBodyJson, BAD_REQUEST, AccountNameFormatError.withPath("/accountName"))
        )

        input.foreach(args => (validationErrorTest _).tupled(args))
      }

      "downstream service error" when {
        def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"downstream returns a code $downstreamCode error and status $downstreamStatus" in new Test {

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
              DownstreamStub.onError(DownstreamStub.PUT, downstreamUri, downstreamStatus, errorBody(downstreamCode))
            }

            val response: WSResponse = await(request().put(validRequestJson))
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
          (BAD_REQUEST, "1007", BAD_REQUEST, SavingsAccountIdFormatError),
          (BAD_REQUEST, "1000", INTERNAL_SERVER_ERROR, InternalError),
          (NOT_FOUND, "5010", NOT_FOUND, NotFoundError)
        )

        input.foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }

  private trait Test {

    val nino: String             = "AA123456A"
    val savingsAccountId: String = "SAVKB2UVwUTBQGJ"

    private def uri: String = s"/uk-accounts/$nino/account-name/$savingsAccountId"

    def downstreamUri: String = s"/itsd/income-sources/$nino/non-business/$savingsAccountId"

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.2.0+json"),
          (AUTHORIZATION, "Bearer 123")
        )
    }

  }

}
