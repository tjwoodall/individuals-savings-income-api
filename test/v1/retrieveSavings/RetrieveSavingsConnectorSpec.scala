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

package v1.retrieveSavings

import shared.connectors.{ConnectorSpec, DownstreamOutcome}
import shared.models.domain.{Nino, TaxYear, Timestamp}
import shared.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import v1.retrieveSavings.def1.model.request.Def1_RetrieveSavingsRequestData
import v1.retrieveSavings.def1.model.response.Def1_RetrieveSavingsResponse
import v1.retrieveSavings.model.request.RetrieveSavingsRequestData
import v1.retrieveSavings.model.response.RetrieveSavingsResponse

import scala.concurrent.Future

class RetrieveSavingsConnectorSpec extends ConnectorSpec {

  "RetrieveSavingsConnector" should {
    "return the expected response for a non-TYS request" when {
      "a valid request is made" in new IfsTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2019-20")

        val outcome = Right(ResponseWrapper(correlationId, response))

        willGet(
          url = url"$baseUrl/income-tax/income/savings/$nino/${taxYear.asMtd}"
        ).returns(Future.successful(outcome))

        val result: DownstreamOutcome[RetrieveSavingsResponse] = await(connector.retrieveSavings(request))
        result shouldBe outcome
      }
    }

    "return the expected response for a TYS request" when {
      "a valid request is made" in new TysIfsTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2023-24")

        val outcome = Right(ResponseWrapper(correlationId, response))

        willGet(
          url = url"$baseUrl/income-tax/income/savings/${taxYear.asTysDownstream}/$nino"
        ).returns(Future.successful(outcome))

        val result: DownstreamOutcome[RetrieveSavingsResponse] = await(connector.retrieveSavings(request))
        result shouldBe outcome
      }
    }
  }

  trait Test {
    _: ConnectorTest =>

    protected val nino: String = "AA111111A"
    def taxYear: TaxYear

    val request: RetrieveSavingsRequestData =
      Def1_RetrieveSavingsRequestData(Nino(nino), taxYear)

    val response: Def1_RetrieveSavingsResponse = Def1_RetrieveSavingsResponse(
      submittedOn = Timestamp("2019-04-04T01:01:01.000Z"),
      securities = None,
      foreignInterest = None
    )

    val connector: RetrieveSavingsConnector =
      new RetrieveSavingsConnector(http = mockHttpClient, appConfig = mockSharedAppConfig)

  }

}
