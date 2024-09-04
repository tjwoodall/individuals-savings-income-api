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

package v1.createAmendUkSavingsAnnualSummary

import models.domain.SavingsAccountId
import play.api.libs.json.{JsObject, Json}
import shared.connectors.{ConnectorSpec, DownstreamOutcome}
import shared.models.domain.{Nino, TaxYear}
import shared.models.outcomes.ResponseWrapper
import config.MockSavingsConfig
import v1.createAmendUkSavingsAnnualSummary.def1.model.request.{
  Def1_CreateAmendUkSavingsAnnualSummaryRequestBody,
  Def1_CreateAmendUkSavingsAnnualSummaryRequestData,
  Def1_DownstreamCreateAmendUkSavingsAnnualSummaryRequestBody
}

import scala.concurrent.Future

class CreateAmendUkSavingsAnnualSummaryConnectorSpec extends ConnectorSpec with MockSavingsConfig {

  val nino: String = "AA111111A"

  val savingsAccountId: SavingsAccountId    = SavingsAccountId("ABCDE1234567890")
  val taxedUkInterest: Option[BigDecimal]   = Some(31554452289.99)
  val untaxedUkInterest: Option[BigDecimal] = Some(91523009816.00)

  val transactionReference: String = "0000000000000001"

  val requestBody: Def1_CreateAmendUkSavingsAnnualSummaryRequestBody =
    Def1_CreateAmendUkSavingsAnnualSummaryRequestBody(taxedUkInterest, untaxedUkInterest)

  val downstreamRequestBody: Def1_DownstreamCreateAmendUkSavingsAnnualSummaryRequestBody = requestBody.asDownstreamRequestBody(savingsAccountId)

  private val validResponse: JsObject = Json.obj("transactionReference" -> transactionReference)
  val outcome                         = Right(ResponseWrapper(correlationId, validResponse))

  "CreateAmendUkSavingsAccountAnnualSummaryConnector" when {
    "createAmendUkSavingsAccountAnnualSummary called for a non Tax Year Specific tax year" must {
      "return a 200 status for a success scenario" in new DesTest with Test {

        MockedSavingsConfig.featureSwitches.returns(mockSavingsFeatureSwitches).anyNumberOfTimes()
        MockedSavingsConfig.isDesIf_MigrationEnabled.returns(false)
        def taxYear: TaxYear = TaxYear.fromMtd("2019-20")
        val url: String      = s"$baseUrl/income-tax/nino/$nino/income-source/savings/annual/${taxYear.asDownstream}"
        willPost(url, downstreamRequestBody) returns Future.successful(outcome)

        val result: DownstreamOutcome[Unit] = await(connector.createOrAmendUKSavingsAccountSummary(requestData))
        result shouldBe outcome
      }

      "return a 200 status for a success scenario when desIf_Migration is enabled" in new IfsTest with Test {
        MockedSavingsConfig.featureSwitches.returns(mockSavingsFeatureSwitches).anyNumberOfTimes()
        MockedSavingsConfig.isDesIf_MigrationEnabled.returns(true)
        def taxYear: TaxYear = TaxYear.fromMtd("2019-20")
        val url: String      = s"$baseUrl/income-tax/nino/$nino/income-source/savings/annual/${taxYear.asDownstream}"
        willPost(url, downstreamRequestBody) returns Future.successful(outcome)

        val result: DownstreamOutcome[Unit] = await(connector.createOrAmendUKSavingsAccountSummary(requestData))
        result shouldBe outcome
      }

      "createAmendUkSavingsAccountAnnualSummary for a Tax Year Specific tax year" must {
        "return a 200 status for a success scenario " in new TysIfsTest with Test {
          def taxYear: TaxYear = TaxYear.fromMtd("2023-24")
          val url              = s"$baseUrl/income-tax/${taxYear.asTysDownstream}/$nino/income-source/savings/annual"
          willPost(url, downstreamRequestBody) returns Future.successful(outcome)
          val result: DownstreamOutcome[Unit] = await(connector.createOrAmendUKSavingsAccountSummary(requestData))
          result shouldBe outcome
        }
      }
    }
  }

  trait Test {
    _: ConnectorTest =>

    def taxYear: TaxYear

    protected val connector: CreateAmendUkSavingsAnnualSummaryConnector =
      new CreateAmendUkSavingsAnnualSummaryConnector(http = mockHttpClient, appConfig = mockAppConfig, savingsConfig = mockSavingsConfig)

    protected val requestData: Def1_CreateAmendUkSavingsAnnualSummaryRequestData =
      Def1_CreateAmendUkSavingsAnnualSummaryRequestData(
        nino = Nino(nino),
        taxYear = taxYear,
        savingsAccountId = savingsAccountId,
        mtdBody = requestBody
      )

  }

}
