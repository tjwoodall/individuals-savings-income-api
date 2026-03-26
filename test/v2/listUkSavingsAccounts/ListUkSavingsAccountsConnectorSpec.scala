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

package v2.listUkSavingsAccounts

import models.domain.SavingsAccountId
import shared.config.MockSharedAppConfig
import shared.connectors.{ConnectorSpec, DownstreamOutcome}
import shared.models.domain.Nino
import shared.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import v2.listUkSavingsAccounts.def1.model.request.Def1_ListUkSavingsAccountsRequestData
import v2.listUkSavingsAccounts.def1.model.response.{Def1_ListUkSavingsAccountsResponse, Def1_UkSavingsAccount}
import v2.listUkSavingsAccounts.model.response.ListUkSavingsAccountsResponse

import scala.concurrent.Future

class ListUkSavingsAccountsConnectorSpec extends ConnectorSpec with MockSharedAppConfig {

  val nino: String                       = "AA111111A"
  val savingsAccountId: SavingsAccountId = SavingsAccountId("SAVKB2UVwUTBQGJ")

  val request: Def1_ListUkSavingsAccountsRequestData                     = Def1_ListUkSavingsAccountsRequestData(Nino(nino), None)
  val requestWithSavingsAccountId: Def1_ListUkSavingsAccountsRequestData = Def1_ListUkSavingsAccountsRequestData(Nino(nino), Some(savingsAccountId))

  private val validResponse = Def1_ListUkSavingsAccountsResponse(
    savingsAccounts = Some(
      Seq(
        Def1_UkSavingsAccount(savingsAccountId = "000000000000001", accountName = Some("Bank Account 1")),
        Def1_UkSavingsAccount(savingsAccountId = "000000000000002", accountName = Some("Bank Account 2")),
        Def1_UkSavingsAccount(savingsAccountId = "000000000000003", accountName = Some("Bank Account 3"))
      )
    )
  )

  trait Test {
    self: ConnectorTest =>

    val connector: ListUkSavingsAccountsConnector =
      new ListUkSavingsAccountsConnector(http = mockHttpClient, appConfig = mockSharedAppConfig)

  }

  "listUkSavingsAccounts" must {
    "return a valid response upon receiving SUCCESS response from the backend service" in new HipTest with Test {
      val outcome: Right[Nothing, ResponseWrapper[ListUkSavingsAccountsResponse]] =
        Right(ResponseWrapper(correlationId, validResponse))

      willGet(
        url = url"$baseUrl/itsd/income-sources/v2/$nino",
        parameters = Seq("incomeSourceType" -> "09")
      ).returns(Future.successful(outcome))

      val result: DownstreamOutcome[ListUkSavingsAccountsResponse] = await(connector.listUkSavingsAccounts(request))

      result shouldBe outcome
    }

    "return a valid response, list of a single uk savings account when incomeSourceId was sent" in new HipTest with Test {
      val outcome: Right[Nothing, ResponseWrapper[ListUkSavingsAccountsResponse]] =
        Right(ResponseWrapper(correlationId, validResponse))

      willGet(
        url = url"$baseUrl/itsd/income-sources/v2/$nino",
        parameters = Seq("incomeSourceType" -> "09", "incomeSourceId" -> savingsAccountId.toString)
      ).returns(Future.successful(outcome))

      val result: DownstreamOutcome[ListUkSavingsAccountsResponse] =
        await(connector.listUkSavingsAccounts(requestWithSavingsAccountId))

      result shouldBe outcome
    }
  }

}
