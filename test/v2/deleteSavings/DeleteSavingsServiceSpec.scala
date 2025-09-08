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

package v2.deleteSavings

import models.errors.RuleOutsideAmendmentWindowError
import shared.controllers.EndpointLogContext
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors.*
import shared.models.outcomes.ResponseWrapper
import shared.services.ServiceSpec
import v2.deleteSavings.def1.model.request.Def1_DeleteSavingsRequestData
import v2.deleteSavings.model.request.DeleteSavingsRequestData

import scala.concurrent.Future

class DeleteSavingsServiceSpec extends ServiceSpec {

  "DeleteSavingsService" should {
    "deleteSavings" must {
      "return correct result for a success" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, ()))

        MockDeleteSavingsConnector
          .deleteSavings(request)
          .returns(Future.successful(outcome))

        await(service.deleteSavings(request)) shouldBe outcome
      }

      "map errors according to spec" when {
        def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
          s"a $downstreamErrorCode error is returned from the service" in new Test {

            MockDeleteSavingsConnector
              .deleteSavings(request)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

            await(service.deleteSavings(request)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val errors = List(
          ("INVALID_TAXABLE_ENTITY_ID", NinoFormatError),
          ("INVALID_TAX_YEAR", TaxYearFormatError),
          ("INVALID_CORRELATIONID", InternalError),
          ("NO_DATA_FOUND", NotFoundError),
          ("OUTSIDE_AMENDMENT_WINDOW", RuleOutsideAmendmentWindowError),
          ("SERVER_ERROR", InternalError),
          ("SERVICE_UNAVAILABLE", InternalError)
        )

        val extraTysErrors = List(
          ("INVALID_CORRELATION_ID", InternalError),
          ("TAX_YEAR_NOT_SUPPORTED", RuleTaxYearNotSupportedError)
        )

        (errors ++ extraTysErrors).foreach(args => serviceError.tupled(args))
      }
    }
  }

  trait Test extends MockDeleteSavingsConnector {
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    private val nino    = Nino("AA112233A")
    private val taxYear = TaxYear.fromMtd("2021-22")

    val request: DeleteSavingsRequestData = Def1_DeleteSavingsRequestData(
      nino = nino,
      taxYear = taxYear
    )

    val service: DeleteSavingsService = new DeleteSavingsService(
      connector = mockDeleteSavingsConnector
    )

  }

}
