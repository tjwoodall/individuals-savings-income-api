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

package v2.addUkSavingsAccount

import cats.implicits._
import shared.controllers.RequestContext
import shared.models.errors._
import shared.services.{BaseService, ServiceOutcome}
import v2.addUkSavingsAccount.model.request.AddUkSavingsAccountRequestData
import v2.addUkSavingsAccount.model.response.AddUkSavingsAccountResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AddUkSavingsAccountService @Inject() (connector: AddUkSavingsAccountConnector) extends BaseService {

  def addSavings(request: AddUkSavingsAccountRequestData)(implicit
      ctx: RequestContext,
      ec: ExecutionContext): Future[ServiceOutcome[AddUkSavingsAccountResponse]] = {

    connector.addSavings(request).map(_.leftMap(mapDownstreamErrors(downstreamErrorMap)))
  }

  private val downstreamErrorMap: Map[String, MtdError] =
    Map(
      "INVALID_IDVALUE"      -> NinoFormatError,
      "MAX_ACCOUNTS_REACHED" -> RuleMaximumSavingsAccountsLimitError,
      "ALREADY_EXISTS"       -> RuleDuplicateAccountNameError,
      "INVALID_IDTYPE"       -> InternalError,
      "INVALID_PAYLOAD"      -> InternalError,
      "SERVER_ERROR"         -> InternalError,
      "SERVICE_UNAVAILABLE"  -> InternalError
    )

}
