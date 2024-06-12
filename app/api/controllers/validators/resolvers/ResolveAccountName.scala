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

package api.controllers.validators.resolvers

import api.models.domain.AccountName
import api.models.errors.AccountNameFormatErrorNew
import cats.data.Validated
import shared.controllers.validators.resolvers.{ResolveStringPattern, ResolverSupport}
import shared.models.errors.MtdError

object ResolveAccountName extends ResolverSupport {

  private val accountNameRegex = "^[A-Za-z0-9 &'()*,\\-./@£]{1,32}$".r

  val resolver: Resolver[String, AccountName] =
    ResolveStringPattern(accountNameRegex, AccountNameFormatErrorNew).resolver.map(AccountName)

  def apply(value: String): Validated[Seq[MtdError], AccountName] = resolver(value)

}
