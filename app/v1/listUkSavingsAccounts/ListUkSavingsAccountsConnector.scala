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

package v1.listUkSavingsAccounts

import shared.config.{ConfigFeatureSwitches, SharedAppConfig}
import shared.connectors.DownstreamUri.{HipUri, IfsUri}
import shared.connectors.httpparsers.StandardDownstreamHttpParser.reads
import shared.connectors.{BaseDownstreamConnector, DownstreamOutcome, DownstreamUri}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import v1.listUkSavingsAccounts.model.request.ListUkSavingsAccountsRequestData
import v1.listUkSavingsAccounts.model.response.{ListUkSavingsAccountsResponse, UkSavingsAccount}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ListUkSavingsAccountsConnector @Inject() (val http: HttpClientV2, val appConfig: SharedAppConfig)
    extends BaseDownstreamConnector {

  def listUkSavingsAccounts(request: ListUkSavingsAccountsRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[ListUkSavingsAccountsResponse[UkSavingsAccount]]] = {

    import request._
    import schema._

    val nino: String = request.nino.nino
    val incomeSourceTypeParam: (String, String) = "incomeSourceType" -> "09"

    val downstreamUri: DownstreamUri[DownstreamResp] = if (ConfigFeatureSwitches().isEnabled("ifs_hip_migration_2085")) {
      HipUri[DownstreamResp](s"itsd/income-sources/v2/$nino")
    } else {
      IfsUri[DownstreamResp](s"income-tax/income-sources/$nino")
    }

    get(
      downstreamUri,
      request.savingsAccountId
        .fold(Seq(incomeSourceTypeParam))(savingsId => Seq(incomeSourceTypeParam, "incomeSourceId" -> savingsId.toString))
    )
  }

}
