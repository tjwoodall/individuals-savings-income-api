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

package v2.retrieveSavings

import shared.controllers.EndpointLogContext
import shared.models.domain.*
import shared.models.errors.*
import shared.models.outcomes.ResponseWrapper
import shared.services.ServiceSpec
import v2.retrieveSavings.def1.model.request.Def1_RetrieveSavingsRequestData
import v2.retrieveSavings.def1.model.response.Def1_RetrieveSavingsResponse

import scala.concurrent.Future

class RetrieveSavingsServiceSpec extends ServiceSpec {

  "RetrieveSavingsServiceSpec" should {
    "retrieveSavings" must {
      "return correct result for a success" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, response))

        MockRetrieveSavingsConnector
          .retrieve(request)
          .returns(Future.successful(outcome))

        await(service.retrieveSavings(request)) shouldBe outcome
      }

      "map errors according to spec" when {
        def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
          s"a $downstreamErrorCode error is returned from the service" in new Test {

            MockRetrieveSavingsConnector
              .retrieve(request)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

            await(service.retrieveSavings(request)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val errors = List(
          ("INVALID_TAXABLE_ENTITY_ID", NinoFormatError),
          ("INVALID_TAX_YEAR", TaxYearFormatError),
          ("INVALID_CORRELATIONID", InternalError),
          ("NO_DATA_FOUND", NotFoundError),
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

  trait Test extends MockRetrieveSavingsConnector {
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    private val nino    = Nino("AA112233A")
    private val taxYear = TaxYear.fromMtd("2019-20")

    val request: Def1_RetrieveSavingsRequestData = Def1_RetrieveSavingsRequestData(
      nino = nino,
      taxYear = taxYear
    )

    val response: Def1_RetrieveSavingsResponse = Def1_RetrieveSavingsResponse(
      submittedOn = Timestamp("2019-04-04T01:01:01.000Z"),
      securities = None,
      foreignInterest = None
    )

    val service: RetrieveSavingsService = new RetrieveSavingsService(
      connector = mockRetrieveSavingsConnector
    )

  }

}
