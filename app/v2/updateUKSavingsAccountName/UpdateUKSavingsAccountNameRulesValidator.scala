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
import resolvers.ResolveAccountName
import shared.controllers.validators.RulesValidator
import shared.models.errors._
import v2.updateUKSavingsAccountName.model.request.UpdateUKSavingsAccountNameRequest

object UpdateUKSavingsAccountNameRulesValidator extends RulesValidator[UpdateUKSavingsAccountNameRequest] {

  override def validateBusinessRules(parsed: UpdateUKSavingsAccountNameRequest): Validated[Seq[MtdError], UpdateUKSavingsAccountNameRequest] = {
    import parsed.body._

    validateAccountName(accountName).onSuccess(parsed)
  }

  private def validateAccountName(accountName: String): Validated[Seq[MtdError], String] =
    ResolveAccountName(accountName, "/accountName")

}
