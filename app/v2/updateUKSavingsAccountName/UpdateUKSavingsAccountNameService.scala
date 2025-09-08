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

package v2.updateUKSavingsAccountName

import cats.implicits.*
import models.errors.SavingsAccountIdFormatError
import shared.controllers.RequestContext
import shared.models.errors.*
import shared.services.{BaseService, ServiceOutcome}
import v2.updateUKSavingsAccountName.model.request.UpdateUKSavingsAccountNameRequest

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateUKSavingsAccountNameService @Inject() (connector: UpdateUKSavingsAccountNameConnector) extends BaseService {

  def update(request: UpdateUKSavingsAccountNameRequest)(implicit ctx: RequestContext, ec: ExecutionContext): Future[ServiceOutcome[Unit]] =
    connector.update(request).map(_.leftMap(mapDownstreamErrors(downstreamErrorMap)))

  private val downstreamErrorMap: Map[String, MtdError] = Map(
    "1215" -> NinoFormatError,
    "1007" -> SavingsAccountIdFormatError,
    "1000" -> InternalError,
    "5010" -> NotFoundError
  )

}
