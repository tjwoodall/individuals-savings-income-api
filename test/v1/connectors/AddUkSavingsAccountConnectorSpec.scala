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

package v1.connectors

import shared.config.MockAppConfig
import shared.connectors.ConnectorSpec
import shared.mocks.MockHttpClient
import shared.models.domain.Nino
import shared.models.outcomes.ResponseWrapper
import v1.models.request.addUkSavingsAccount.{AddUkSavingsAccountRequestBody, AddUkSavingsAccountRequestData}
import v1.models.response.addUkSavingsAccount.AddUkSavingsAccountResponse

import scala.concurrent.Future

class AddUkSavingsAccountConnectorSpec extends ConnectorSpec {

  val nino: String = "AA111111A"

  val addUkSavingsAccountRequestBody: AddUkSavingsAccountRequestBody = AddUkSavingsAccountRequestBody(accountName = "Shares savings account")

  val addUkSavingsAccountRequest: AddUkSavingsAccountRequestData = AddUkSavingsAccountRequestData(
    nino = Nino(nino),
    body = addUkSavingsAccountRequestBody
  )

  val addUkSavingsAccountResponse: AddUkSavingsAccountResponse = AddUkSavingsAccountResponse(
    savingsAccountId = "SAVKB2UVwUTBQGJ"
  )

  trait Test extends MockHttpClient with MockAppConfig {

    val connector: AddUkSavingsAccountConnector = new AddUkSavingsAccountConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    MockAppConfig.desBaseUrl returns baseUrl
    MockAppConfig.desToken returns "des-token"
    MockAppConfig.desEnvironment returns "des-environment"
    MockAppConfig.desEnvironmentHeaders returns Some(allowedDesHeaders)
  }

  "addSavingsConnector" when {
    "addSavings" must {
      "return a 200 status for a success scenario" in new DesTest with Test {
        val outcome = Right(ResponseWrapper(correlationId, addUkSavingsAccountResponse))

        willPost(s"$baseUrl/income-tax/income-sources/nino/$nino", addUkSavingsAccountRequestBody).returns(Future.successful(outcome))

        await(connector.addSavings(addUkSavingsAccountRequest)) shouldBe outcome
      }
    }
  }

}
