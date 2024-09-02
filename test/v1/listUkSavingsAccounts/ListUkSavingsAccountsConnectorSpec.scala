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

package v1.listUkSavingsAccounts

import mocks.MockFeatureSwitches
import models.domain.SavingsAccountId
import play.api.Configuration
import shared.config.MockAppConfig
import shared.connectors.{ConnectorSpec, DownstreamOutcome}
import shared.models.domain.{Nino, TaxYear}
import shared.models.outcomes.ResponseWrapper
import v1.listUkSavingsAccounts.def1.model.request.Def1_ListUkSavingsAccountsRequestData
import v1.listUkSavingsAccounts.def1.model.response.{Def1_ListUkSavingsAccountsResponse, Def1_UkSavingsAccount}
import v1.listUkSavingsAccounts.model.response.{ListUkSavingsAccountsResponse, UkSavingsAccount}

import scala.concurrent.Future

class ListUkSavingsAccountsConnectorSpec extends ConnectorSpec with MockAppConfig with MockFeatureSwitches {

  val nino: String = "AA111111A"
  val taxYear: String = "2019"
  val savingsAccountId: SavingsAccountId = SavingsAccountId("SAVKB2UVwUTBQGJ")

  val request: Def1_ListUkSavingsAccountsRequestData = Def1_ListUkSavingsAccountsRequestData(Nino(nino), None)
  val requestWithSavingsAccountId: Def1_ListUkSavingsAccountsRequestData = Def1_ListUkSavingsAccountsRequestData(Nino(nino), Some(savingsAccountId))

  private val validResponse = Def1_ListUkSavingsAccountsResponse(
    savingsAccounts = Some(
      Seq(
        Def1_UkSavingsAccount(savingsAccountId = "000000000000001", accountName = "Bank Account 1"),
        Def1_UkSavingsAccount(savingsAccountId = "000000000000002", accountName = "Bank Account 2"),
        Def1_UkSavingsAccount(savingsAccountId = "000000000000003", accountName = "Bank Account 3")
      )
    )
  )

  trait Test {
    _: ConnectorTest =>

    protected val nino: String = "AA111111A"

    def taxYear: TaxYear

    val connector: ListUkSavingsAccountsConnector =
      new ListUkSavingsAccountsConnector(http = mockHttpClient, appConfig = mockAppConfig)(mockFeatureSwitches)

  }

  "ListUkSavingsAccountsConnector" when {
    "listUkSavingsAccounts" must {
      "return a valid response, list of uk savings accounts when isListUkSavingsDownstreamURLEnabled is false" +
        "upon receiving SUCCESS response from the backend service" in new DesTest with Test {

        MockFeatureSwitches.isListUkSavingsDownstreamURLEnabled.returns(false)

        val outcome: Right[Nothing, ResponseWrapper[ListUkSavingsAccountsResponse[UkSavingsAccount]]] =
          Right(ResponseWrapper(correlationId, validResponse))

        override def taxYear: TaxYear = TaxYear("2021")

        willGet(
          url = s"$baseUrl/income-tax/income-sources/nino/${this.nino}",
          parameters = Seq("incomeSourceType" -> "interest-from-uk-banks")
        ).returns(Future.successful(outcome))

        val result: DownstreamOutcome[ListUkSavingsAccountsResponse[UkSavingsAccount]] = await(connector.listUkSavingsAccounts(request))
        result shouldBe outcome

      }
      "return a valid response, list of uk savings accounts when isListUkSavingsDownstreamURLEnabled is true " +
        "upon receiving SUCCESS response from the backend service" in new IfsTest with Test {


        MockAppConfig.featureSwitchConfig returns Configuration("listUkSavingsDownstreamURL.enabled" -> true)
        MockFeatureSwitches.isListUkSavingsDownstreamURLEnabled.returns(true)

        val outcome: Right[Nothing, ResponseWrapper[ListUkSavingsAccountsResponse[UkSavingsAccount]]] =
          Right(ResponseWrapper(correlationId, validResponse))

        override def taxYear: TaxYear = TaxYear("2021")

        willGet(
          url = s"$baseUrl/income-tax/income-sources/${this.nino}",
          parameters = Seq("incomeSourceType" -> "interest-from-uk-banks")
        ).returns(Future.successful(outcome))

        val result: DownstreamOutcome[ListUkSavingsAccountsResponse[UkSavingsAccount]] = await(connector.listUkSavingsAccounts(request))
        result shouldBe outcome

      }

      "return a valid response, list of a single uk savings account " +
        "when incomeSourceId was sent as part of the request and isListUkSavingsDownstreamURLEnabled is false" +
        "upon receiving SUCCESS response from the backend service " in new DesTest with Test {

        MockFeatureSwitches.isListUkSavingsDownstreamURLEnabled.returns(false)

        val outcome: Right[Nothing, ResponseWrapper[ListUkSavingsAccountsResponse[UkSavingsAccount]]] =
          Right(ResponseWrapper(correlationId, validResponse))

        override def taxYear: TaxYear = TaxYear("2021")

        willGet(
          url = s"$baseUrl/income-tax/income-sources/nino/${this.nino}",
          parameters = Seq("incomeSourceType" -> "interest-from-uk-banks", "incomeSourceId" -> savingsAccountId.toString)
        ).returns(Future.successful(outcome))

        val result: DownstreamOutcome[ListUkSavingsAccountsResponse[UkSavingsAccount]] =
          await(connector.listUkSavingsAccounts(requestWithSavingsAccountId))
        result shouldBe outcome
      }

      "return a valid response, list of a single uk savings account " +
        "when incomeSourceId was sent as part of the request and isListUkSavingsDownstreamURLEnabled is true " +
        "upon receiving SUCCESS response from the backend service " in new IfsTest with Test {

        MockFeatureSwitches.isListUkSavingsDownstreamURLEnabled.returns(true)

        val outcome: Right[Nothing, ResponseWrapper[ListUkSavingsAccountsResponse[UkSavingsAccount]]] =
          Right(ResponseWrapper(correlationId, validResponse))

        override def taxYear: TaxYear = TaxYear("2021")

        willGet(
          url = s"$baseUrl/income-tax/income-sources/${this.nino}",
          parameters = Seq("incomeSourceType" -> "interest-from-uk-banks", "incomeSourceId" -> savingsAccountId.toString)
        ).returns(Future.successful(outcome))

        val result: DownstreamOutcome[ListUkSavingsAccountsResponse[UkSavingsAccount]] =
          await(connector.listUkSavingsAccounts(requestWithSavingsAccountId))
        result shouldBe outcome
      }
    }
  }

}
