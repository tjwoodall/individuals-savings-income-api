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

package v1.deleteSavings

import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import shared.controllers.validators.Validator
import shared.models.errors.MtdError
import v1.deleteSavings.model.request.DeleteSavingsRequestData

trait MockDeleteSavingsValidatorFactory extends TestSuite with MockFactory {

  val mockDeleteSavingsValidatorFactory: DeleteSavingsValidatorFactory =
    mock[DeleteSavingsValidatorFactory]

  object MockedDeleteSavingsValidatorFactory {

    def validator(): CallHandler[Validator[DeleteSavingsRequestData]] =
      (mockDeleteSavingsValidatorFactory.validator(_: String, _: String)).expects(*, *)

  }

  def willUseValidator(use: Validator[DeleteSavingsRequestData]): CallHandler[Validator[DeleteSavingsRequestData]] = {
    MockedDeleteSavingsValidatorFactory
      .validator()
      .anyNumberOfTimes()
      .returns(use)
  }

  def returningSuccess(result: DeleteSavingsRequestData): Validator[DeleteSavingsRequestData] =
    new Validator[DeleteSavingsRequestData] {
      def validate: Validated[Seq[MtdError], DeleteSavingsRequestData] = Valid(result)
    }

  def returning(result: MtdError*): Validator[DeleteSavingsRequestData] = returningErrors(result)

  def returningErrors(result: Seq[MtdError]): Validator[DeleteSavingsRequestData] =
    new Validator[DeleteSavingsRequestData] {
      def validate: Validated[Seq[MtdError], DeleteSavingsRequestData] = Invalid(result)
    }

}
