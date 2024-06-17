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

package v1.controllers.validators

import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import cats.implicits.catsSyntaxTuple2Semigroupal
import models.domain.SavingsAccountId
//import resolvers.ResolveOptionalSavingsAccountId
import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers.ResolveNino
import shared.models.errors.{MtdError, SavingsAccountIdFormatError}
import v1.models.request.listUkSavingsAccounts.ListUkSavingsAccountsRequestData

import javax.inject.{Inject, Singleton}

@Singleton
class ListUkSavingsAccountsValidatorFactory @Inject() {

  def validator(nino: String, savingsAccountId: Option[String]): Validator[ListUkSavingsAccountsRequestData] =
    new Validator[ListUkSavingsAccountsRequestData] {

      def validate: Validated[Seq[MtdError], ListUkSavingsAccountsRequestData] =
        (
          ResolveNino(nino),
          resolveOptionalSavingsAccountId(savingsAccountId)
          ).mapN(ListUkSavingsAccountsRequestData)

    }

  private def resolveOptionalSavingsAccountId(maybeid: Option[String]): Validated[Seq[MtdError], Option[SavingsAccountId]] = {
    val regex = "^[A-Za-z0-9]{15}$"
    maybeid
      .map { id =>
        if (id.matches(regex)) Valid(Some(SavingsAccountId(id))) else Invalid(Seq[MtdError](SavingsAccountIdFormatError))
      }
      .getOrElse(Valid(None))
  }

}
