/*
 * Copyright 2023 HM Revenue & Customs
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

package v1.controllers

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import shared.config.MockAppConfig
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.models.audit._
import shared.models.domain.Nino
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import v1.mocks.services.MockAddUkSavingsAccountService
import v1.mocks.validators.MockAddUkSavingsAccountValidatorFactory
import v1.models.request.addUkSavingsAccount.{AddUkSavingsAccountRequestBody, AddUkSavingsAccountRequestData}
import v1.models.response.addUkSavingsAccount.AddUkSavingsAccountResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AddUkSavingsAccountControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockAppConfig
    with MockAddUkSavingsAccountService
    with MockAddUkSavingsAccountValidatorFactory {

  val savingsAccountId: String = "SAVKB2UVwUTBQGJ"
  val mtdId: String            = "test-mtd-id"

  "AddUkSavingsAccountController" should {
    "return OK" when {
      "happy path" in new Test {
        MockAppConfig.apiGatewayContext.returns("individuals/savings-income").anyNumberOfTimes()
        willUseValidator(returningSuccess(requestData))

        MockAddUkSavingsAccountService
          .addUkSavingsAccountService(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseData))))

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(responseJson))
      }
    }
    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))
        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        MockAppConfig.apiGatewayContext.returns("individuals/savings-income").anyNumberOfTimes()
        willUseValidator(returningSuccess(requestData))

        MockAddUkSavingsAccountService
          .addUkSavingsAccountService(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleMaximumSavingsAccountsLimitError))))

        runErrorTest(RuleMaximumSavingsAccountsLimitError)
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking {

    private val controller = new AddUkSavingsAccountController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockAddUkSavingsAccountValidatorFactory,
      service = mockAddUkSavingsAccountService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.addUkSavingsAccount(validNino)(fakePostRequest(requestBodyJson))

    protected def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "AddUkSavingsAccount",
        transactionName = "add-uk-savings-account",
        detail = GenericAuditDetail(
          userType = "Individual",
          agentReferenceNumber = None,
          versionNumber = "1.0",
          params = Map("nino" -> validNino),
          requestBody = maybeRequestBody,
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )

    val requestBodyJson: JsValue = Json.parse("""
      |{
      |   "accountName": "Shares savings account"
      |}
      |""".stripMargin)

    val requestData: AddUkSavingsAccountRequestData = AddUkSavingsAccountRequestData(
      nino = Nino(validNino),
      body = AddUkSavingsAccountRequestBody("Shares savings account")
    )

    val responseData: AddUkSavingsAccountResponse = AddUkSavingsAccountResponse(
      savingsAccountId = savingsAccountId
    )

    val responseJson: JsValue = Json.parse(s"""
      |{
      |    "savingsAccountId": "$savingsAccountId"
      |}
      |""".stripMargin)

  }

}
