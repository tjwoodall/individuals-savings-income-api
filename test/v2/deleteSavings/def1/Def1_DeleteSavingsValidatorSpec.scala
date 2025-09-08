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

package v2.deleteSavings.def1

import shared.config.{MockSharedAppConfig, SharedAppConfig}
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors.*
import shared.utils.UnitSpec
import v2.deleteSavings.def1.model.request.Def1_DeleteSavingsRequestData
import v2.deleteSavings.model.request.DeleteSavingsRequestData

class Def1_DeleteSavingsValidatorSpec extends UnitSpec with MockSharedAppConfig {

  private implicit val correlationId: String = "1234"
  private val validNino                      = "AA123456A"
  private val validTaxYear                   = "2020-21"

  private val parsedNino    = Nino(validNino)
  private val parsedTaxYear = TaxYear.fromMtd(validTaxYear)

  implicit val appConfig: SharedAppConfig              = mockSharedAppConfig
  private def validator(nino: String, taxYear: String) = new Def1_DeleteSavingsValidator(nino, taxYear)(mockSharedAppConfig)

  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied" in new SetupConfig {
        val result: Either[ErrorWrapper, DeleteSavingsRequestData] = validator(validNino, validTaxYear).validateAndWrapResult()
        result shouldBe Right(Def1_DeleteSavingsRequestData(parsedNino, parsedTaxYear))

      }
    }

    "return NinoFormatError error" when {
      "an invalid nino is supplied" in new SetupConfig {
        val result: Either[ErrorWrapper, DeleteSavingsRequestData] = validator("A12344A", validTaxYear).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }
    }

    "return TaxYearFormatError error" when {
      "an invalid tax year is supplied" in new SetupConfig {
        val result: Either[ErrorWrapper, DeleteSavingsRequestData] = validator(validNino, "201718").validateAndWrapResult()
        result shouldBe Left(
          ErrorWrapper(correlationId, TaxYearFormatError)
        )
      }
    }

    "return RuleTaxYearRangeInvalidError error" when {
      "an invalid tax year range is supplied" in new SetupConfig {
        val result: Either[ErrorWrapper, DeleteSavingsRequestData] = validator(validNino, "2016-17").validateAndWrapResult()
        result shouldBe Left(
          ErrorWrapper(correlationId, RuleTaxYearNotSupportedError)
        )
      }
    }

    "return RuleTaxYearNotSupportedError error" when {
      "an invalid tax year is supplied" in new SetupConfig {
        val result: Either[ErrorWrapper, DeleteSavingsRequestData] = validator(validNino, "2017-19").validateAndWrapResult()
        result shouldBe Left(
          ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError)
        )
      }
    }

    "return multiple errors" when {
      "request supplied has multiple errors" in new SetupConfig {
        val result: Either[ErrorWrapper, DeleteSavingsRequestData] = validator("not-a-nino", "2017-19").validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            BadRequestError,
            Some(List(NinoFormatError, RuleTaxYearRangeInvalidError))
          )
        )
      }
    }
  }

}
