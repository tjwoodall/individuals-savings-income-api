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

package v2.addUkSavingsAccount

import models.errors.RuleMaximumSavingsAccountsLimitError
import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import shared.config.MockSharedAppConfig
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.models.audit.*
import shared.models.domain.Nino
import shared.models.errors.*
import shared.models.outcomes.ResponseWrapper
import v2.addUkSavingsAccount.def1.model.request.{Def1_AddUkSavingsAccountRequestBody, Def1_AddUkSavingsAccountRequestData}
import v2.addUkSavingsAccount.def1.model.response.Def1_AddUkSavingsAccountResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AddUkSavingsAccountControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockSharedAppConfig
    with MockAddUkSavingsAccountService
    with MockAddUkSavingsAccountValidatorFactory {

  val savingsAccountId: String = "SAVKB2UVwUTBQGJ"
  val mtdId: String            = "test-mtd-id"

  "AddUkSavingsAccountController" should {
    "return OK" when {
      "happy path" in new Test {
        MockedSharedAppConfig.apiGatewayContext.returns("individuals/savings-income").anyNumberOfTimes()
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
        MockedSharedAppConfig.apiGatewayContext.returns("individuals/savings-income").anyNumberOfTimes()
        willUseValidator(returningSuccess(requestData))

        MockAddUkSavingsAccountService
          .addUkSavingsAccountService(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleMaximumSavingsAccountsLimitError))))

        runErrorTest(RuleMaximumSavingsAccountsLimitError)
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] {

    protected val controller: AddUkSavingsAccountController = new AddUkSavingsAccountController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockAddUkSavingsAccountValidatorFactory,
      service = mockAddUkSavingsAccountService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedSharedAppConfig
      .endpointAllowsSupportingAgents(controller.endpointName)
      .anyNumberOfTimes()
      .returns(true)

    protected def callController(): Future[Result] = controller.addUkSavingsAccount(validNino)(fakePostRequest(requestBodyJson))

    protected def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "AddUkSavingsAccount",
        transactionName = "add-uk-savings-account",
        detail = GenericAuditDetail(
          userType = "Individual",
          agentReferenceNumber = None,
          versionNumber = "2.0",
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

    val requestData: Def1_AddUkSavingsAccountRequestData = Def1_AddUkSavingsAccountRequestData(
      nino = Nino(validNino),
      body = Def1_AddUkSavingsAccountRequestBody("Shares savings account")
    )

    val responseData: Def1_AddUkSavingsAccountResponse = Def1_AddUkSavingsAccountResponse(
      savingsAccountId = savingsAccountId
    )

    val responseJson: JsValue = Json.parse(s"""
      |{
      |    "savingsAccountId": "$savingsAccountId"
      |}
      |""".stripMargin)

  }

}
