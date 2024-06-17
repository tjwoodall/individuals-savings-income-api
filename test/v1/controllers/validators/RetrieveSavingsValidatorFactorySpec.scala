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

import config.SavingsAppConfig
import mocks.MockSavingsAppConfig
import shared.UnitSpec
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import v1.models.request.retrieveSavings.RetrieveSavingsRequestData

class RetrieveSavingsValidatorFactorySpec extends UnitSpec with MockSavingsAppConfig{

  private implicit val correlationId: String = "1234"

  private val validNino    = "AA123456A"
  private val validTaxYear = "2020-21"
  private val parsedNino    = Nino(validNino)
  private val parsedTaxYear = TaxYear.fromMtd(validTaxYear)

  implicit val savingsAppConfig: SavingsAppConfig = mockSavingsAppConfig
  val validator = new RetrieveSavingsValidatorFactory(savingsAppConfig)


  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied" in {
        validator.validator(validNino, validTaxYear).validateAndWrapResult() shouldBe
          Right(RetrieveSavingsRequestData(parsedNino, parsedTaxYear))
      }
    }

    "return NinoFormatError error" when {
      "an invalid nino is supplied" in {
        validator.validator("A12344A", validTaxYear).validateAndWrapResult() shouldBe
          Left(
            ErrorWrapper(correlationId, NinoFormatError)
          )
      }
    }

    "return TaxYearFormatError error" when {
      "an invalid tax year is supplied" in {
        validator.validator(validNino, "20178").validateAndWrapResult() shouldBe
          Left(
            ErrorWrapper(correlationId, TaxYearFormatError)
          )
      }
    }

    "return RuleTaxYearRangeInvalidError error" when {
      "an invalid tax year range is supplied" in {
        validator.validator(validNino, "2019-21").validateAndWrapResult() shouldBe
          Left(
            ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError)
          )
      }
    }

    "return RuleTaxYearNotSupportedError error" when {
      "an invalid tax year is supplied" in {
        validator.validator(validNino, "2018-19").validateAndWrapResult() shouldBe
          Left(
            ErrorWrapper(correlationId, RuleTaxYearNotSupportedError)
          )
      }
    }

    "return multiple errors" when {
      "request supplied has multiple errors" in {
        validator.validator("A12344A", "20178").validateAndWrapResult() shouldBe
          Left(
            ErrorWrapper(
              correlationId,
              BadRequestError,
              Some(List(NinoFormatError, TaxYearFormatError))
            )
          )
      }
    }
  }

}
