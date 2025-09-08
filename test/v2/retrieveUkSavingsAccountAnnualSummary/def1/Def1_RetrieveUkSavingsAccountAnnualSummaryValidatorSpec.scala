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

package v2.retrieveUkSavingsAccountAnnualSummary.def1

import models.domain.SavingsAccountId
import models.errors.SavingsAccountIdFormatError
import shared.config.MockSharedAppConfig
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors.*
import shared.utils.UnitSpec
import v2.retrieveUkSavingsAccountAnnualSummary.def1.model.request.Def1_RetrieveUkSavingsAccountAnnualSummaryRequestData

class Def1_RetrieveUkSavingsAccountAnnualSummaryValidatorSpec extends UnitSpec with MockSharedAppConfig {

  private implicit val correlationId: String = "1234"
  private val validNino                      = "AA123456A"
  private val validTaxYear                   = "2021-22"
  private val validSavingsAccountId          = SavingsAccountId("SAVKB2UVwUTBQGJ")
  private val parsedNino                     = Nino(validNino)
  private val parsedTaxYear                  = TaxYear.fromMtd(validTaxYear)

  private def validator(nino: String, taxYear: String, savingsAccountId: String) =
    new Def1_RetrieveUkSavingsAccountAnnualSummaryValidator(nino, taxYear, savingsAccountId)(mockSharedAppConfig)

  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied" in new SetupConfig {
        validator(validNino, validTaxYear, validSavingsAccountId.toString).validateAndWrapResult() shouldBe
          Right(Def1_RetrieveUkSavingsAccountAnnualSummaryRequestData(parsedNino, parsedTaxYear, validSavingsAccountId))
      }
    }

    "return NinoFormatError error" when {
      "an invalid nino is supplied" in new SetupConfig {
        validator("A12344A", validTaxYear, validSavingsAccountId.toString).validateAndWrapResult() shouldBe
          Left(
            ErrorWrapper(correlationId, NinoFormatError)
          )
      }
    }

    "return TaxYearFormatError error" when {
      "an invalid tax year is supplied" in new SetupConfig {
        validator(validNino, "20178", validSavingsAccountId.toString).validateAndWrapResult() shouldBe
          Left(
            ErrorWrapper(correlationId, TaxYearFormatError)
          )
      }
    }

    "return RuleTaxYearNotSupportedError error" when {
      "a tax year that is not supported is supplied" in new SetupConfig {
        validator(validNino, "2018-19", validSavingsAccountId.toString).validateAndWrapResult() shouldBe
          Left(
            ErrorWrapper(correlationId, RuleTaxYearNotSupportedError)
          )
      }
    }

    "return NinoFormatError and TaxYearFormatError errors" when {
      "request supplied has invalid nino and tax year" in new SetupConfig {
        validator("A12344A", "20178", validSavingsAccountId.toString).validateAndWrapResult() shouldBe
          Left(
            ErrorWrapper(
              correlationId,
              BadRequestError,
              Some(List(NinoFormatError, TaxYearFormatError))
            )
          )
      }
    }

    "return NinoFormatError, TaxYearFormatError and SavingsAccountIdFormatError errors" when {
      "request supplied has invalid nino, tax year and savingsAccountId" in new SetupConfig {
        validator("A12344A", "20178", "ABCDE12345FG").validateAndWrapResult() shouldBe
          Left(
            ErrorWrapper(
              correlationId,
              BadRequestError,
              Some(List(NinoFormatError, SavingsAccountIdFormatError, TaxYearFormatError))
            )
          )
      }
    }
  }

}
