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

package v1.controllers.requestParsers.validators.resolvers


import shared.models.errors.AccountNameFormatError
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import shared.models.errors.MtdError
import v1.controllers.requestParsers.validators.resolvers.ResolveAccountName.regex

case class ResolveAccountName(path: String) {

  def apply(value: String): Validated[Seq[MtdError], String] = {
    if (value.matches(regex)) {
      Valid(value)
    } else {
      Invalid(List(AccountNameFormatError.withExtraPath(path)))
    }
  }

}

object ResolveAccountName {
  private val regex = "^[A-Za-z0-9 &'\\(\\)\\*,\\-\\./@£]{1,32}$"

  def apply(value: String, path: String): Validated[Seq[MtdError], String] = {
    val resolver = ResolveAccountName(path)

    resolver(value)
  }

}
