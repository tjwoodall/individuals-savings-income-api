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

package v2.deleteSavings

import shared.connectors.ConnectorSpec
import shared.models.domain.{Nino, TaxYear}
import shared.models.outcomes.ResponseWrapper
import v2.deleteSavings.def1.model.request.Def1_DeleteSavingsRequestData
import v2.deleteSavings.model.request.DeleteSavingsRequestData

import scala.concurrent.Future

class DeleteSavingsConnectorSpec extends ConnectorSpec {

  "DeleteSavingsConnector" should {
    "return the expected response for a non-TYS request" when {
      "a valid request is made" in new IfsTest with Test {
        val taxYear = "2021-22"
        val outcome = Right(ResponseWrapper(correlationId, ()))

        willDelete(
          url = s"$baseUrl/income-tax/income/savings/AA111111A/2021-22"
        ).returns(Future.successful(outcome))

        await(connector.deleteSavings(request)) shouldBe outcome
      }
    }

    "return the expected response for a TYS request" when {
      "a valid request is made" in new TysIfsTest with Test {
        val taxYear = "2023-24"
        val outcome = Right(ResponseWrapper(correlationId, ()))

        willDelete(
          url = s"$baseUrl/income-tax/income/savings/23-24/AA111111A"
        ).returns(Future.successful(outcome))

        await(connector.deleteSavings(request)) shouldBe outcome
      }
    }
  }

  trait Test { _: ConnectorTest =>
    val taxYear: String

    val connector: DeleteSavingsConnector = new DeleteSavingsConnector(
      http = mockHttpClient,
      appConfig = mockSharedAppConfig
    )

    lazy val request: DeleteSavingsRequestData = Def1_DeleteSavingsRequestData(Nino("AA111111A"), TaxYear.fromMtd(taxYear))
  }

}
