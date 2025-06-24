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

package v2.createAmendSavings

import shared.connectors.ConnectorSpec
import shared.models.domain.{Nino, TaxYear}
import shared.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import v2.createAmendSavings.def1.model.request.{Def1_CreateAmendSavingsRequestBody, Def1_CreateAmendSavingsRequestData}
import v2.createAmendSavings.model.request.CreateAmendSavingsRequestData

import scala.concurrent.Future

class CreateAmendSavingsConnectorSpec extends ConnectorSpec {

  "CreateAmendSavingsConnector" when {
    "createAmendSaving" must {
      "return a 204 status for a success scenario" in new IfsTest with Test {

        willPut(url = url"$baseUrl/income-tax/income/savings/$nino/${taxYear.asMtd}", body = requestBody).returns(Future.successful(outcome))

        await(connector.createAmendSavings(request)) shouldBe outcome
      }

      "return a 204 status for a success scenario for Tax Year Specific (TYS)" in new TysIfsTest with Test {
        override def taxYear: TaxYear = TaxYear.fromMtd("2023-24")

        willPut(url = url"$baseUrl/income-tax/income/savings/${taxYear.asTysDownstream}/$nino", body = requestBody).returns(Future.successful(outcome))

        await(connector.createAmendSavings(request)) shouldBe outcome
      }
    }
  }

  trait Test {
    _: ConnectorTest =>
    def nino: String     = "AA111111A"
    def taxYear: TaxYear = TaxYear.fromMtd("2019-20")

    val requestBody: Def1_CreateAmendSavingsRequestBody = Def1_CreateAmendSavingsRequestBody(securities = None, foreignInterest = None)

    val request: CreateAmendSavingsRequestData = Def1_CreateAmendSavingsRequestData(
      nino = Nino(nino),
      taxYear = taxYear,
      body = requestBody
    )

    val connector: CreateAmendSavingsConnector = new CreateAmendSavingsConnector(
      http = mockHttpClient,
      appConfig = mockSharedAppConfig
    )

    val outcome = Right(ResponseWrapper(correlationId, ()))

  }

}
