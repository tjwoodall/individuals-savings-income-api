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

package resolvers

import cats.data.Validated
import models.domain.SavingsAccountId
import models.errors.SavingsAccountIdFormatError
import shared.controllers.validators.resolvers.{ResolveStringPattern, ResolverSupport}
import shared.models.errors.MtdError

object ResolveSavingsAccountId extends ResolverSupport {

  private val savingsAccountIdRegex = "^[A-Za-z0-9]{15}$".r

  val resolver: Resolver[String, SavingsAccountId] = {
    ResolveStringPattern(savingsAccountIdRegex, SavingsAccountIdFormatError).resolver.map(SavingsAccountId.apply)
  }

  def apply(value: String): Validated[Seq[MtdError], SavingsAccountId] = resolver(value)
}
