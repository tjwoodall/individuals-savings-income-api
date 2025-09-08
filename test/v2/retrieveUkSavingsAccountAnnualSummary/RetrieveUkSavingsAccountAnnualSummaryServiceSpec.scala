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
import models.errors.SavingsAccountIdFormatError
import shared.controllers.EndpointLogContext
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors.*
import shared.models.outcomes.ResponseWrapper
import shared.services.ServiceSpec
import v2.retrieveUkSavingsAccountAnnualSummary.def1.model.request.Def1_RetrieveUkSavingsAccountAnnualSummaryRequestData
import v2.retrieveUkSavingsAccountAnnualSummary.def1.model.response.{
  Def1_RetrieveUkSavingsAccountAnnualSummaryResponse,
  Def1_RetrieveUkSavingsAnnualIncomeItem
}
import v2.retrieveUkSavingsAccountAnnualSummary.model.response.RetrieveUkSavingsAccountAnnualSummaryResponse

import scala.concurrent.Future

class RetrieveUkSavingsAccountAnnualSummaryServiceSpec extends ServiceSpec {

  private val nino           = "AA112233A"
  private val taxYear        = "2019-20"
  private val incomeSourceId = SavingsAccountId("SAVKB2UVwUTBQGJ")

  private val requestData = Def1_RetrieveUkSavingsAccountAnnualSummaryRequestData(Nino(nino), TaxYear.fromMtd(taxYear), incomeSourceId)

  trait Test extends MockRetrieveUkSavingsAccountAnnualSummaryConnector {
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service: RetrieveUkSavingsAccountAnnualSummaryService =
      new RetrieveUkSavingsAccountAnnualSummaryService(connector = mockRetrieveUkSavingsAccountAnnualSummaryConnector)

  }

  "RetrieveUkSavingsAccountAnnualSummaryService" when {
    "the downstream returns a single account" must {
      "return a successful result" in new Test {
        private val responseData = Def1_RetrieveUkSavingsAccountAnnualSummaryResponse(
          Seq(Def1_RetrieveUkSavingsAnnualIncomeItem(incomeSourceId = "ignored", taxedUkInterest = Some(2000.99), untaxedUkInterest = Some(5000.50))))

        val downstreamResponse: ResponseWrapper[RetrieveUkSavingsAccountAnnualSummaryResponse] =
          ResponseWrapper(
            correlationId,
            responseData
          )

        MockRetrieveUkSavingsAccountAnnualSummaryConnector
          .retrieveUkSavingsAccountAnnualSummary(requestData) returns Future.successful(Right(downstreamResponse))

        await(service.retrieveUkSavingsAccountAnnualSummary(requestData)) shouldBe
          Right(ResponseWrapper(correlationId, responseData))
      }
    }

    "the downstream returns multiple accounts" must {
      "return an internal server error" in new Test {
        val downstreamResponse: ResponseWrapper[RetrieveUkSavingsAccountAnnualSummaryResponse] =
          ResponseWrapper(
            correlationId,
            Def1_RetrieveUkSavingsAccountAnnualSummaryResponse(
              Seq(
                Def1_RetrieveUkSavingsAnnualIncomeItem(incomeSourceId = "ignored1", taxedUkInterest = Some(1), untaxedUkInterest = Some(1)),
                Def1_RetrieveUkSavingsAnnualIncomeItem(incomeSourceId = "ignored1", taxedUkInterest = Some(2), untaxedUkInterest = Some(3))
              )
            )
          )

        MockRetrieveUkSavingsAccountAnnualSummaryConnector
          .retrieveUkSavingsAccountAnnualSummary(requestData) returns Future.successful(Right(downstreamResponse))

        await(service.retrieveUkSavingsAccountAnnualSummary(requestData)) shouldBe
          Left(ErrorWrapper(correlationId, InternalError))
      }
    }

    "the downstream returns no accounts" must {
      "return a NotFoundError" in new Test {
        val downstreamResponse: ResponseWrapper[RetrieveUkSavingsAccountAnnualSummaryResponse] =
          ResponseWrapper(correlationId, Def1_RetrieveUkSavingsAccountAnnualSummaryResponse(Nil))

        MockRetrieveUkSavingsAccountAnnualSummaryConnector
          .retrieveUkSavingsAccountAnnualSummary(requestData) returns Future.successful(Right(downstreamResponse))

        await(service.retrieveUkSavingsAccountAnnualSummary(requestData)) shouldBe
          Left(ErrorWrapper(correlationId, NotFoundError))
      }
    }

    "map errors according to spec" when {

      def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
        s"a $downstreamErrorCode error is returned from the service" in new Test {

          MockRetrieveUkSavingsAccountAnnualSummaryConnector
            .retrieveUkSavingsAccountAnnualSummary(requestData)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

          await(service.retrieveUkSavingsAccountAnnualSummary(requestData)) shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val errors = List(
        ("INVALID_NINO", NinoFormatError),
        ("INVALID_TYPE", InternalError),
        ("INVALID_TAXYEAR", TaxYearFormatError), // remove once DES to IFS migration complete
        ("INVALID_INCOME_SOURCE", SavingsAccountIdFormatError),
        ("NOT_FOUND_PERIOD", NotFoundError),
        ("NOT_FOUND_INCOME_SOURCE", NotFoundError),
        ("SERVER_ERROR", InternalError),
        ("SERVICE_UNAVAILABLE", InternalError)
      )

      val tysErrors = List(
        ("INVALID_TAX_YEAR", TaxYearFormatError),
        ("INVALID_CORRELATION_ID", InternalError),
        ("INVALID_INCOMESOURCE_ID", SavingsAccountIdFormatError),
        ("INVALID_INCOMESOURCE_TYPE", InternalError),
        ("SUBMISSION_PERIOD_NOT_FOUND", NotFoundError),
        ("INCOME_DATA_SOURCE_NOT_FOUND", NotFoundError),
        ("TAX_YEAR_NOT_SUPPORTED", RuleTaxYearNotSupportedError)
      )

      (errors ++ tysErrors).foreach(args => serviceError.tupled(args))
    }
  }

}
