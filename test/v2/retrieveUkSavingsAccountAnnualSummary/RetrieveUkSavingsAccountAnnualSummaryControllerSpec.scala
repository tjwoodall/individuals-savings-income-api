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

package v2.retrieveUkSavingsAccountAnnualSummary

import models.domain.SavingsAccountId
import play.api.Configuration
import play.api.mvc.Result
import shared.config.MockSharedAppConfig
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors.*
import shared.models.outcomes.ResponseWrapper
import v2.retrieveUkSavingsAccountAnnualSummary.def1.model.RetrieveUkSavingsAccountAnnualSummaryControllerFixture
import v2.retrieveUkSavingsAccountAnnualSummary.def1.model.request.Def1_RetrieveUkSavingsAccountAnnualSummaryRequestData
import v2.retrieveUkSavingsAccountAnnualSummary.def1.model.response.{
  Def1_RetrieveUkSavingsAccountAnnualSummaryResponse,
  Def1_RetrieveUkSavingsAnnualIncomeItem
}
import v2.retrieveUkSavingsAccountAnnualSummary.model.request.RetrieveUkSavingsAccountAnnualSummaryRequestData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveUkSavingsAccountAnnualSummaryControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockRetrieveUkSavingsAnnualSummaryService
    with MockRetrieveUkSavingsAccountAnnualSummaryValidatorFactory
    with MockSharedAppConfig {

  val nino: String                        = "AA123456A"
  val taxYear: String                     = "2019-20"
  val savingsAccountId: String            = "ABCDE0123456789"
  val taxedUkIncome: Option[BigDecimal]   = Some(93556675358.99)
  val unTaxedUkIncome: Option[BigDecimal] = Some(34514974058.99)

  private val requestData: RetrieveUkSavingsAccountAnnualSummaryRequestData = Def1_RetrieveUkSavingsAccountAnnualSummaryRequestData(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(taxYear),
    savingsAccountId = SavingsAccountId(savingsAccountId)
  )

  private val retrieveUkSavingsAccountAnnualSummaryResponse: Def1_RetrieveUkSavingsAccountAnnualSummaryResponse =
    new Def1_RetrieveUkSavingsAccountAnnualSummaryResponse(
      Seq(
        Def1_RetrieveUkSavingsAnnualIncomeItem(incomeSourceId = "id1", taxedUkInterest = Some(1.12), untaxedUkInterest = Some(2.12)),
        Def1_RetrieveUkSavingsAnnualIncomeItem(incomeSourceId = "id2", taxedUkInterest = Some(3.12), untaxedUkInterest = Some(4.12))
      ))

  private val mtdResponse = RetrieveUkSavingsAccountAnnualSummaryControllerFixture.mtdRetrieveResponse

  "RetrieveUkSavingsAccountSummaryControllerSpec" should {
    "return OK" when {
      "happy path" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrieveUkSavingsAnnualSummaryService
          .retrieveUkSavings(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, retrieveUkSavingsAccountAnnualSummaryResponse))))

        runOkTest(
          expectedStatus = OK,
          maybeExpectedResponseBody = Some(mtdResponse)
        )
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returningErrors(Seq(ErrorWrapper(correlationId, NinoFormatError).error)))

        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrieveUkSavingsAnnualSummaryService
          .retrieveUkSavings(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, NotFoundError))))

        runErrorTest(NotFoundError)
      }
    }
  }

  trait Test extends ControllerTest {

    val controller: RetrieveUkSavingsAccountAnnualSummaryController = new RetrieveUkSavingsAccountAnnualSummaryController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockRetrieveUkSavingsAccountValidatorFactory,
      service = mockRetrieveUkSavingsAnnualSummaryService,
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

    protected def callController(): Future[Result] = controller.retrieveUkSavingAccount(nino, taxYear, savingsAccountId)(fakeRequest)
  }

}
