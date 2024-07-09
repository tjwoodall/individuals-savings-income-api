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

package v1.addUkSavingsAccount

import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import play.api.libs.json.JsValue
import shared.controllers.validators.Validator
import shared.models.errors.MtdError
import v1.addUkSavingsAccount.model.request.AddUkSavingsAccountRequestData

trait MockAddUkSavingsAccountValidatorFactory extends MockFactory {

  val mockAddUkSavingsAccountValidatorFactory: AddUkSavingsAccountValidatorFactory =
    mock[AddUkSavingsAccountValidatorFactory]

  object MockAddUkSavingsAccountValidatorFactory {

    def validator(): CallHandler[Validator[AddUkSavingsAccountRequestData]] =
      (mockAddUkSavingsAccountValidatorFactory.validator(_: String, _: JsValue)).expects(*, *)

  }

  def willUseValidator(use: Validator[AddUkSavingsAccountRequestData]): CallHandler[Validator[AddUkSavingsAccountRequestData]] = {
    MockAddUkSavingsAccountValidatorFactory
      .validator()
      .anyNumberOfTimes()
      .returns(use)
  }

  def returningSuccess(result: AddUkSavingsAccountRequestData): Validator[AddUkSavingsAccountRequestData] =
    new Validator[AddUkSavingsAccountRequestData] {
      def validate: Validated[Seq[MtdError], AddUkSavingsAccountRequestData] = Valid(result)
    }

  def returning(result: MtdError*): Validator[AddUkSavingsAccountRequestData] = returningErrors(result)

  def returningErrors(result: Seq[MtdError]): Validator[AddUkSavingsAccountRequestData] =
    new Validator[AddUkSavingsAccountRequestData] {
      def validate: Validated[Seq[MtdError], AddUkSavingsAccountRequestData] = Invalid(result)
    }

}
