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

import api.models.domain.SavingsAccountId
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import shared.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import shared.models.auth.UserDetails
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import shared.config.MockAppConfig
import play.api.libs.json.{JsObject, JsValue}
import play.api.mvc.Result
import shared.models.audit.GenericAuditDetailFixture.nino
import shared.utils.MockIdGenerator
import v1.controllers.validators.mocks.MockCreateAmendUkSavingsAnnualSummaryValidatorFactory
import v1.mocks.services.MockCreateAmendUkSavingsAnnualSummaryService
import v1.models.request.createAmendUkSavingsAnnualSummary._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateAmendUkSavingsAnnualSummaryControllerSpec
    extends ControllerBaseSpec
      with ControllerTestRunner
      with MockEnrolmentsAuthService
      with MockMtdIdLookupService
      with MockCreateAmendUkSavingsAnnualSummaryValidatorFactory
      with MockCreateAmendUkSavingsAnnualSummaryService
      with MockIdGenerator
      with MockAuditService
      with MockAppConfig {

  val taxYear: String          = "2019-20"
  val savingsAccountId: String = "acctId"
  val mtdId: String            = "test-mtd-id"

  val requestJson: JsObject = JsObject.empty

  val requestData: CreateAmendUkSavingsAnnualSummaryRequestData = CreateAmendUkSavingsAnnualSummaryRequestData(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(taxYear),
    savingsAccountId = SavingsAccountId(savingsAccountId),
    body = CreateAmendUkSavingsAnnualSummaryBody(None, None)
  )

  "CreateAmendUkSavingsAnnualSummaryController" should {
    "return OK" when {
      "happy path" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockCreateAmendAmendUkSavingsAnnualSummaryService
          .createOrAmendAnnualSummary(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        runOkTest(expectedStatus = OK)
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))
        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockCreateAmendAmendUkSavingsAnnualSummaryService
          .createOrAmendAnnualSummary(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTest(RuleTaxYearNotSupportedError)
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking {

    val controller = new CreateAmendUkSavingsAnnualSummaryController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockCreateAmendUkSavingsAnnualSummaryValidatorFactory,
      service = mockCreateAmendUkSavingsAnnualSummaryService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )(appConfig = mockAppConfig, ec = global)

    protected def callController(): Future[Result] =
      controller.createAmendUkSavingsAnnualSummary(nino, taxYear, savingsAccountId)(fakePostRequest(requestJson))

    def event(auditResponse: AuditResponse, requestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] = {
      AuditEvent(
        auditType = "createAmendUkSavingsAnnualSummary",
        transactionName = "create-and-amend-uk-savings-account-annual-summary",
        detail = GenericAuditDetail(
          UserDetails(mtdId, "Individual", None),
          "2.0",
          Map("nino" -> nino, "taxYear" -> taxYear, "savingsAccountId" -> savingsAccountId),
          requestBody,
          correlationId,
          auditResponse
        )
      )
    }

  }

}
