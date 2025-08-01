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

package v1.retrieveUkSavingsAccountAnnualSummary

import config.SavingsConfig
import shared.config.SharedAppConfig
import shared.connectors.DownstreamUri.{DesUri, IfsUri}
import shared.connectors.httpparsers.StandardDownstreamHttpParser.reads
import shared.connectors.{BaseDownstreamConnector, DownstreamOutcome, DownstreamUri}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import v1.retrieveUkSavingsAccountAnnualSummary.model.request.RetrieveUkSavingsAccountAnnualSummaryRequestData
import v1.retrieveUkSavingsAccountAnnualSummary.model.response.RetrieveUkSavingsAccountAnnualSummaryResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveUkSavingsAccountAnnualSummaryConnector @Inject() (val http: HttpClientV2, val appConfig: SharedAppConfig, savingsConfig: SavingsConfig)
    extends BaseDownstreamConnector {

  def retrieveUkSavingsAccountAnnualSummary(request: RetrieveUkSavingsAccountAnnualSummaryRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[RetrieveUkSavingsAccountAnnualSummaryResponse]] = {

    val nino           = request.nino.nino
    val incomeSourceId = request.savingsAccountId

    import request._
    import schema._

    val downstreamUri: DownstreamUri[DownstreamResp] =
      if (request.taxYear.useTaxYearSpecificApi) {
        IfsUri(s"income-tax/${request.taxYear.asTysDownstream}/$nino/income-source/savings/annual?incomeSourceId=$incomeSourceId")
      } else {
        val path = s"income-tax/nino/$nino/income-source/savings/annual/${request.taxYear.asDownstream}?incomeSourceId=$incomeSourceId"
        if (savingsConfig.featureSwitches.isDesIf_MigrationEnabled) IfsUri(path) else DesUri(path)
      }

    get(downstreamUri)
  }

}
