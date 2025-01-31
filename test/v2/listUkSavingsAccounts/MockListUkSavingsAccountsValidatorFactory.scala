/*
 * Copyright 2024 HM Revenue & Customs
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

package v2.listUkSavingsAccounts

import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import shared.controllers.validators.Validator
import shared.models.errors.MtdError
import v2.listUkSavingsAccounts.model.request.ListUkSavingsAccountsRequestData

trait MockListUkSavingsAccountsValidatorFactory extends MockFactory {

  val mockListUkSavingsAccountValidator: ListUkSavingsAccountsValidatorFactory = mock[ListUkSavingsAccountsValidatorFactory]

  object MockListUkSavingsAccountValidatorFactory {

    def validator(): CallHandler[Validator[ListUkSavingsAccountsRequestData]] =
      (mockListUkSavingsAccountValidator.validator(_: String, _: Option[String])).expects(*, *)

  }

  def willUseValidator(use: Validator[ListUkSavingsAccountsRequestData]): CallHandler[Validator[ListUkSavingsAccountsRequestData]] = {
    MockListUkSavingsAccountValidatorFactory
      .validator()
      .anyNumberOfTimes()
      .returns(use)
  }

  def returningSuccess(result: ListUkSavingsAccountsRequestData): Validator[ListUkSavingsAccountsRequestData] =
    new Validator[ListUkSavingsAccountsRequestData] {
      def validate: Validated[Seq[MtdError], ListUkSavingsAccountsRequestData] = Valid(result)
    }

  def returning(result: MtdError*): Validator[ListUkSavingsAccountsRequestData] = returningErrors(result)

  def returningErrors(result: Seq[MtdError]): Validator[ListUkSavingsAccountsRequestData] =
    new Validator[ListUkSavingsAccountsRequestData] {
      def validate: Validated[Seq[MtdError], ListUkSavingsAccountsRequestData] = Invalid(result)
    }

}
