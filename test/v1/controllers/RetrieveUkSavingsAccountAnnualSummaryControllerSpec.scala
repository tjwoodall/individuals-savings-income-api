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

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import mocks.MockAppConfig
import play.api.mvc.Result
import v1.fixtures.RetrieveUkSavingsAccountAnnualSummaryControllerFixture
import v1.mocks.requestParsers.MockRetrieveUkSavingsAnnualRequestParser
import v1.mocks.services.MockRetrieveUkSavingsAnnualSummaryService
import v1.models.request.retrieveUkSavingsAnnualSummary.{RetrieveUkSavingsAnnualSummaryRawData, RetrieveUkSavingsAnnualSummaryRequest}
import v1.models.response.retrieveUkSavingsAnnualSummary.RetrieveUkSavingsAnnualSummaryResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveUkSavingsAccountAnnualSummaryControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockRetrieveUkSavingsAnnualSummaryService
    with MockRetrieveUkSavingsAnnualRequestParser
    with MockAppConfig {

  val taxYear: String                     = "2019-20"
  val savingsAccountId: String            = "ABCDE0123456789"
  val taxedUkIncome: Option[BigDecimal]   = Some(93556675358.99)
  val unTaxedUkIncome: Option[BigDecimal] = Some(34514974058.99)

  private val rawData: RetrieveUkSavingsAnnualSummaryRawData = RetrieveUkSavingsAnnualSummaryRawData(nino, taxYear, savingsAccountId)

  private val requestData: RetrieveUkSavingsAnnualSummaryRequest = RetrieveUkSavingsAnnualSummaryRequest(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(taxYear),
    savingsAccountId = savingsAccountId
  )

  private val retrieveUkSavingsAnnualSummaryResponse: RetrieveUkSavingsAnnualSummaryResponse = new RetrieveUkSavingsAnnualSummaryResponse(
    taxedUkInterest = taxedUkIncome,
    untaxedUkInterest = unTaxedUkIncome
  )

  private val mtdResponse = RetrieveUkSavingsAccountAnnualSummaryControllerFixture.mtdRetrieveResponse

  "RetrieveUkSavingsAccountSummaryControllerSpec" should {
    "return OK" when {
      "happy path" in new Test {
        MockRetrieveUkSavingsAnnualRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockRetrieveUkSavingsAnnualSummaryService
          .retrieveUkSavings(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, retrieveUkSavingsAnnualSummaryResponse))))

        runOkTest(
          expectedStatus = OK,
          maybeExpectedResponseBody = Some(mtdResponse)
        )
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        MockRetrieveUkSavingsAnnualRequestParser
          .parse(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError, None)))

        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        MockRetrieveUkSavingsAnnualRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockRetrieveUkSavingsAnnualSummaryService
          .retrieveUkSavings(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, NotFoundError))))

        runErrorTest(NotFoundError)
      }
    }
  }

  trait Test extends ControllerTest {

    val controller = new RetrieveUkSavingsAccountAnnualSummaryController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockRetrieveUkSavingsSummaryRequestParser,
      service = mockRetrieveUkSavingsAnnualSummaryService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.retrieveUkSavingAccount(nino, taxYear, savingsAccountId)(fakeRequest)
  }

}
