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

import cats.implicits._
import models.errors.SavingsAccountIdFormatError
import shared.controllers.RequestContext
import shared.models.errors._
import shared.services.{BaseService, ServiceOutcome}
import v1.listUkSavingsAccounts.model.request.ListUkSavingsAccountsRequestData
import v1.listUkSavingsAccounts.model.response.{ListUkSavingsAccountsResponse, UkSavingsAccount}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ListUkSavingsAccountsService @Inject() (connector: ListUkSavingsAccountsConnector) extends BaseService {

  def listUkSavingsAccounts(request: ListUkSavingsAccountsRequestData)(implicit
      ctx: RequestContext,
      ec: ExecutionContext): Future[ServiceOutcome[ListUkSavingsAccountsResponse[UkSavingsAccount]]] =
    connector.listUkSavingsAccounts(request).map(_.leftMap(mapDownstreamErrors(downstreamErrorMap)))

  private val downstreamErrorMap: Map[String, MtdError] = Map(
    "INVALID_ID_TYPE"          -> InternalError,
    "INVALID_IDVALUE"          -> NinoFormatError,
    "INVALID_INCOMESOURCETYPE" -> InternalError,
    "INVALID_TAXYEAR"          -> InternalError,
    "INVALID_INCOMESOURCEID"   -> SavingsAccountIdFormatError,
    "INVALID_ENDDATE"          -> InternalError,
    "NOT_FOUND"                -> NotFoundError,
    "SERVER_ERROR"             -> InternalError,
    "SERVICE_UNAVAILABLE"      -> InternalError
  )

}
