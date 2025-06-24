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

package v1.addUkSavingsAccount

import shared.config.{ConfigFeatureSwitches, SharedAppConfig}
import shared.connectors.DownstreamUri.{DesUri, HipUri}
import shared.connectors.httpparsers.StandardDownstreamHttpParser.reads
import shared.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import v1.addUkSavingsAccount.model.request.AddUkSavingsAccountRequestData
import v1.addUkSavingsAccount.model.response.AddUkSavingsAccountResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AddUkSavingsAccountConnector @Inject() (val http: HttpClientV2, val appConfig: SharedAppConfig) extends BaseDownstreamConnector {

  def addSavings(request: AddUkSavingsAccountRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[AddUkSavingsAccountResponse]] = {

    import request._
    import schema._

    if (ConfigFeatureSwitches().isEnabled("des_hip_migration_1393")) {
      val downstreamUri = HipUri[DownstreamResp](s"itsd/income-sources/$nino")
      post(body, downstreamUri)
    } else {
      val downstreamUri = DesUri[DownstreamResp](s"income-tax/income-sources/nino/$nino")
      post(body, downstreamUri)
    }
  }

}
