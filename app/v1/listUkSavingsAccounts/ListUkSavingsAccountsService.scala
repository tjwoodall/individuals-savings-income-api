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

import cats.implicits.*
import models.errors.SavingsAccountIdFormatError
import shared.controllers.RequestContext
import shared.models.errors.*
import shared.services.{BaseService, ServiceOutcome}
import v1.listUkSavingsAccounts.model.request.ListUkSavingsAccountsRequestData
import v1.listUkSavingsAccounts.model.response.ListUkSavingsAccountsResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ListUkSavingsAccountsService @Inject() (connector: ListUkSavingsAccountsConnector) extends BaseService {

  def listUkSavingsAccounts(request: ListUkSavingsAccountsRequestData)(implicit
      ctx: RequestContext,
      ec: ExecutionContext): Future[ServiceOutcome[ListUkSavingsAccountsResponse]] =
    connector.listUkSavingsAccounts(request).map(_.leftMap(mapDownstreamErrors(downstreamErrorMap)))

  private val downstreamErrorMap: Map[String, MtdError] = {
    val ifsErrors = Map(
      "INVALID_TAXABLE_ENTITY_ID"  -> NinoFormatError,
      "INVALID_TAX_YEAR"           -> InternalError,
      "INVALID_INCOME_SOURCE_TYPE" -> InternalError,
      "INVALID_CORRELATION_ID"     -> InternalError,
      "INVALID_INCOME_SOURCE_ID"   -> SavingsAccountIdFormatError,
      "INVALID_ENDDATE"            -> InternalError,
      "NOT_FOUND"                  -> NotFoundError,
      "SERVER_ERROR"               -> InternalError,
      "SERVICE_UNAVAILABLE"        -> InternalError
    )

    val hipErrors = Map(
      "1215" -> NinoFormatError,
      "1117" -> InternalError,
      "1122" -> InternalError,
      "1007" -> SavingsAccountIdFormatError,
      "1229" -> InternalError,
      "5010" -> NotFoundError
    )

    ifsErrors ++ hipErrors
  }

}
