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

package v2.addUkSavingsAccount

import play.api.Configuration
import shared.config.MockSharedAppConfig
import shared.connectors.ConnectorSpec
import shared.mocks.MockHttpClient
import shared.models.domain.Nino
import shared.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import v2.addUkSavingsAccount.def1.model.request.{Def1_AddUkSavingsAccountRequestBody, Def1_AddUkSavingsAccountRequestData}
import v2.addUkSavingsAccount.def1.model.response.Def1_AddUkSavingsAccountResponse

import scala.concurrent.Future

class AddUkSavingsAccountConnectorSpec extends ConnectorSpec {

  val nino: String = "AA111111A"

  val addUkSavingsAccountRequestBody: Def1_AddUkSavingsAccountRequestBody =
    Def1_AddUkSavingsAccountRequestBody(accountName = "Shares savings account")

  val addUkSavingsAccountRequest: Def1_AddUkSavingsAccountRequestData = Def1_AddUkSavingsAccountRequestData(
    nino = Nino(nino),
    body = addUkSavingsAccountRequestBody
  )

  val addUkSavingsAccountResponse: Def1_AddUkSavingsAccountResponse = Def1_AddUkSavingsAccountResponse(
    savingsAccountId = "SAVKB2UVwUTBQGJ"
  )

  trait Test extends MockHttpClient with MockSharedAppConfig {

    val connector: AddUkSavingsAccountConnector = new AddUkSavingsAccountConnector(
      http = mockHttpClient,
      appConfig = mockSharedAppConfig
    )

  }

  "AddUkSavingsAccountConnector" when {
    "addSavings" must {
      val outcome = Right(ResponseWrapper(correlationId, addUkSavingsAccountResponse))

      "return a 200 status for a success scenario when feature switch is disabled (DES enabled)" in new DesTest with Test {
        MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes().returns(Configuration("des_hip_migration_1393.enabled" -> false))

        willPost(url"$baseUrl/income-tax/income-sources/nino/$nino", addUkSavingsAccountRequestBody)
          .returns(Future.successful(outcome))

        await(connector.addSavings(addUkSavingsAccountRequest)) shouldBe outcome
      }

      "return a 200 status for a success scenario when feature switch is enabled (HIP enabled)" in new HipTest with Test {
        MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes().returns(Configuration("des_hip_migration_1393.enabled" -> true))

        willPost(url"$baseUrl/itsd/income-sources/$nino", addUkSavingsAccountRequestBody)
          .returns(Future.successful(outcome))

        await(connector.addSavings(addUkSavingsAccountRequest)) shouldBe outcome
      }
    }
  }

}
