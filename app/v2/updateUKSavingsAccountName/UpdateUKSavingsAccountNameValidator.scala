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

import cats.data.Validated
import cats.implicits.catsSyntaxTuple3Semigroupal
import play.api.libs.json.JsValue
import resolvers.ResolveSavingsAccountId
import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers.{ResolveNino, ResolveNonEmptyJsonObject}
import shared.models.errors.MtdError
import v2.updateUKSavingsAccountName.model.request.{UpdateUKSavingsAccountNameRequest, UpdateUKSavingsAccountNameRequestBody}

class UpdateUKSavingsAccountNameValidator(nino: String, savingsAccountId: String, body: JsValue)
    extends Validator[UpdateUKSavingsAccountNameRequest] {

  private val resolveJson = ResolveNonEmptyJsonObject.resolver[UpdateUKSavingsAccountNameRequestBody]

  override def validate: Validated[Seq[MtdError], UpdateUKSavingsAccountNameRequest] =
    (
      ResolveNino(nino),
      ResolveSavingsAccountId(savingsAccountId),
      resolveJson(body)
    ).mapN(UpdateUKSavingsAccountNameRequest.apply) andThen UpdateUKSavingsAccountNameRulesValidator.validateBusinessRules

}
