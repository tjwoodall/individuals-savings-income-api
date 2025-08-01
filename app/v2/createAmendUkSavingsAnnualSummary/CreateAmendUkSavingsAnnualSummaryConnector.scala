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

package v2.createAmendUkSavingsAnnualSummary

import config.SavingsConfig
import play.api.http.Status
import shared.config.SharedAppConfig
import shared.connectors.DownstreamUri.{DesUri, IfsUri}
import shared.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import v2.createAmendUkSavingsAnnualSummary.model.request.CreateAmendUkSavingsAnnualSummaryRequestData

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateAmendUkSavingsAnnualSummaryConnector @Inject() (val http: HttpClientV2, val appConfig: SharedAppConfig, savingsConfig: SavingsConfig)
    extends BaseDownstreamConnector {

  def createOrAmendUKSavingsAccountSummary(requestData: CreateAmendUkSavingsAnnualSummaryRequestData)(implicit
      hc: HeaderCarrier,
      cc: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[Unit]] = {
    import requestData._
    import shared.connectors.httpparsers.StandardDownstreamHttpParser._

    implicit val successCode: SuccessCode = SuccessCode(Status.OK)

    val path = s"income-tax/nino/${nino.nino}/income-source/savings/annual/${taxYear.asDownstream}"

    val downstreamUri =
      if (taxYear.useTaxYearSpecificApi) {
        IfsUri[Unit](s"income-tax/${taxYear.asTysDownstream}/$nino/income-source/savings/annual")
      } else if (savingsConfig.featureSwitches.isDesIf_MigrationEnabled) {
        IfsUri[Unit](path)
      } else {
        DesUri[Unit](path)
      }
    post(
      uri = downstreamUri,
      body = mtdBody.asDownstreamRequestBody(savingsAccountId)
    )
  }

}
