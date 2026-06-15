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

import api.controllers.validators.RulesValidator
import api.controllers.validators.resolvers.ResolveStringPattern
import api.models.errors.*
import cats.data.Validated
import models.errors.AccountNameFormatError
import v2.updateUKSavingsAccountName.model.request.UpdateUKSavingsAccountNameRequest

object UpdateUKSavingsAccountNameRulesValidator extends RulesValidator[UpdateUKSavingsAccountNameRequest] {

  private val accountNameRegex = "^[A-Za-z0-9 &'\\(\\)\\*,\\-\\./@£]{1,32}$".r

  def validateBusinessRules(parsed: UpdateUKSavingsAccountNameRequest): Validated[Seq[MtdError], UpdateUKSavingsAccountNameRequest] = {
    import parsed.body.*
    ResolveStringPattern(accountName, accountNameRegex, AccountNameFormatError.withPath("/accountName")).onSuccess(parsed)
  }

}
