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

import config.SavingsConfig
import shared.config.AppConfig
import shared.connectors.DownstreamUri.{DesUri, IfsUri}
import shared.connectors.httpparsers.StandardDownstreamHttpParser.reads
import shared.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v1.listUkSavingsAccounts.model.request.ListUkSavingsAccountsRequestData
import v1.listUkSavingsAccounts.model.response.{ListUkSavingsAccountsResponse, UkSavingsAccount}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ListUkSavingsAccountsConnector @Inject() (val http: HttpClient, val appConfig: AppConfig, val savingsConfig: SavingsConfig)
    extends BaseDownstreamConnector {

  def listUkSavingsAccounts(request: ListUkSavingsAccountsRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[ListUkSavingsAccountsResponse[UkSavingsAccount]]] = {

    import request._
    import schema._

    val nino = request.nino.nino

    if (savingsConfig.featureSwitches.isListUkSavingsDownstreamURLEnabled) {
      val incomeSourceTypeParam = "incomeSourceType" -> "09"
      get(
        IfsUri[DownstreamResp](s"income-tax/income-sources/$nino"),
        request.savingsAccountId
          .fold(Seq(incomeSourceTypeParam))(savingsId => Seq(incomeSourceTypeParam, "incomeSourceId" -> savingsId.toString))
      )
    } else {
      val incomeSourceTypeParam = "incomeSourceType" -> "interest-from-uk-banks"
      get(
        DesUri[DownstreamResp](s"income-tax/income-sources/nino/$nino"),
        request.savingsAccountId
          .fold(Seq(incomeSourceTypeParam))(savingsId => Seq(incomeSourceTypeParam, "incomeSourceId" -> savingsId.toString))
      )
    }
  }

}
