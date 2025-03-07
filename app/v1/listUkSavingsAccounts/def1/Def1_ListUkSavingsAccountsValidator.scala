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

package v1.listUkSavingsAccounts.def1

import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import cats.implicits.catsSyntaxTuple2Semigroupal
import models.domain.SavingsAccountId
import models.errors.SavingsAccountIdFormatError
import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers.ResolveNino
import shared.models.errors.MtdError
import v1.listUkSavingsAccounts.def1.model.request.Def1_ListUkSavingsAccountsRequestData
import v1.listUkSavingsAccounts.model.request.ListUkSavingsAccountsRequestData

class Def1_ListUkSavingsAccountsValidator(nino: String, savingsAccountId: Option[String]) extends Validator[ListUkSavingsAccountsRequestData] {

  def validate: Validated[Seq[MtdError], ListUkSavingsAccountsRequestData] =
    (
      ResolveNino(nino),
      resolveOptionalSavingsAccountId(savingsAccountId)
    ).mapN(Def1_ListUkSavingsAccountsRequestData)

  private def resolveOptionalSavingsAccountId(maybeId: Option[String]): Validated[Seq[MtdError], Option[SavingsAccountId]] = {
    val regex = "^[A-Za-z0-9]{15}$"
    maybeId
      .map { id =>
        if (id.matches(regex)) Valid(Some(SavingsAccountId(id))) else Invalid(Seq[MtdError](SavingsAccountIdFormatError))
      }
      .getOrElse(Valid(None))
  }

}
