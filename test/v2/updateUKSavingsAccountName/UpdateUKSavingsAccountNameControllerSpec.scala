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

import models.domain.SavingsAccountId
import models.errors.SavingsAccountIdFormatError
import play.api.Configuration
import play.api.libs.json.JsValue
import play.api.mvc.Result
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import v2.updateUKSavingsAccountName.fixture.UpdateUKSavingsAccountNameFixtures.{requestBodyModel, validRequestJson}
import v2.updateUKSavingsAccountName.model.request.UpdateUKSavingsAccountNameRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UpdateUKSavingsAccountNameControllerSpec
  extends ControllerBaseSpec
    with ControllerTestRunner
    with MockUpdateUKSavingsAccountNameService
    with MockUpdateUKSavingsAccountNameValidatorFactory {

  val savingsAccountId: String            = "SAVKB2UVwUTBQGJ"

  private val requestData: UpdateUKSavingsAccountNameRequest = UpdateUKSavingsAccountNameRequest(
    nino = parsedNino,
    savingsAccountId = SavingsAccountId(savingsAccountId),
    body = requestBodyModel
  )

  "UpdateUKSavingsAccountNameController" should {
    "return a successful response with status 204 (NO_CONTENT)" when {
      "given a valid request" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockUpdateUKSavingsAccountNameService
          .update(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        runOkTestWithAudit(expectedStatus = NO_CONTENT, maybeAuditRequestBody = Some(validRequestJson))
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))

        runErrorTestWithAudit(NinoFormatError, Some(validRequestJson))
      }

      "service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockUpdateUKSavingsAccountNameService
          .update(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, SavingsAccountIdFormatError))))

        runErrorTestWithAudit(SavingsAccountIdFormatError, Some(validRequestJson))
      }
    }
  }

  private trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] {

    val controller: UpdateUKSavingsAccountNameController = new UpdateUKSavingsAccountNameController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockUpdateUKSavingsAccountNameValidatorFactory,
      service = mockUpdateUKSavingsAccountNameService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedSharedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = controller.updateUKSavingsAccountName(validNino, savingsAccountId)(fakeRequest.withBody(validRequestJson))

    def event(auditResponse: AuditResponse, requestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "UpdateUKSavingsAccountName",
        transactionName = "update-uk-savings-account-name",
        detail = GenericAuditDetail(
          userType = "Individual",
          versionNumber = apiVersion.name,
          agentReferenceNumber = None,
          params = Map("nino" -> validNino, "savingsAccountId" -> savingsAccountId),
          `X-CorrelationId` = correlationId,
          requestBody = Some(validRequestJson),
          auditResponse = auditResponse
        )
      )

  }

}
