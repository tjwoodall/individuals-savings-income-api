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

package v2.createAmendUkSavingsAnnualSummary.def1

import models.domain.SavingsAccountId
import play.api.libs.json.{JsValue, Json}
import shared.UnitSpec
import shared.config.MockAppConfig
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import config.MockSavingsConfig
import v2.createAmendUkSavingsAnnualSummary.def1.model.request._
import v2.createAmendUkSavingsAnnualSummary.model.request.CreateAmendUkSavingsAnnualSummaryRequestData

class Def1_CreateAmendUkSavingsAnnualSummaryValidatorSpec extends UnitSpec with MockAppConfig with MockSavingsConfig {

  private implicit val correlationId: String = "1234"

  private val validNino             = "AA123456A"
  private val validTaxYear          = "2020-21"
  private val validSavingsAccountId = "Abcdefgh1234567"

  private val validMtdRequestBodyJson: JsValue = Json.parse(
    s"""
       |{
       | "incomeSourceId": "ABCDE1234567890",
       | "taxedUkInterest": 31554452289.99,
       | "untaxedUkInterest": 91523009816.00
       |}
        """.stripMargin
  )

  private val oneBadValueFieldJson: JsValue = Json.parse(
    s"""
       |{
       | "incomeSourceId": "ABCDE1234567890",
       | "taxedUkInterest": 1000.123,
       | "untaxedUkInterest": 91523009816.00
       |}
""".stripMargin
  )

  private val allBadValueFieldsJson: JsValue = Json.parse(
    s"""
       |{
       | "incomeSourceId": "ABCDE1234567890",
       | "taxedUkInterest": 1000.123,
       | "untaxedUkInterest": 1000.123
       |}
""".stripMargin
  )

  private val emptyRequestBodyJson: JsValue = Json.parse("""{}""")

  private val nonsenseRequestBodyJson: JsValue = Json.parse("""{"field": "value"}""")

  private val nonValidRequestBodyJson: JsValue = Json.parse(
    """
      |{
      | "taxedUkInterest": true
      |}
""".stripMargin
  )

  private val missingMandatoryFieldsJson: JsValue = Json.parse(
    """
      |{
      |  "incomeSourceId":[{}]
      |}
""".stripMargin
  )

  private val emptyArrayJson: JsValue = Json.parse(
    """
      |{
      |  "taxedUkInterest": []
      |}
""".stripMargin
  )

  private val parsedNino            = Nino(validNino)
  private val parsedTaxYear         = TaxYear.fromMtd(validTaxYear)
  private val parsedSavngsAccountId = SavingsAccountId(validSavingsAccountId)
  private val parsedMtdBody         = validMtdRequestBodyJson.as[Def1_CreateAmendUkSavingsAnnualSummaryRequestBody]

  private def validator(nino: String, taxYear: String, savingsAccountId: String, body: JsValue) =
    new Def1_CreateAmendUkSavingsAnnualSummaryValidator(nino, taxYear, savingsAccountId, body)(mockAppConfig, mockSavingsConfig)

  "validator" should {
    "return the parsed domain object" when {
      "a valid request is supplied" in {
        val result = validator(validNino, validTaxYear, validSavingsAccountId, validMtdRequestBodyJson).validateAndWrapResult()
        result shouldBe Right(Def1_CreateAmendUkSavingsAnnualSummaryRequestData(parsedNino, parsedTaxYear, parsedSavngsAccountId, parsedMtdBody))
      }
    }

    "return NinoFormatError error" when {
      "an invalid nino is supplied" in {
        val result = validator("A12344A", validTaxYear, validSavingsAccountId, validMtdRequestBodyJson).validateAndWrapResult()
        result shouldBe Left(
          ErrorWrapper(correlationId, NinoFormatError)
        )
      }
    }

    "return TaxYearFormatError error" when {
      "an invalid tax year is supplied" in {
        val result = validator(validNino, "201718", validSavingsAccountId, validMtdRequestBodyJson).validateAndWrapResult()
        result shouldBe Left(
          ErrorWrapper(correlationId, TaxYearFormatError)
        )
      }
    }

    "return RuleTaxYearNotSupportedError error" when {
      "an out of range tax year is supplied" in {
        val result = validator(validNino, "2016-17", validSavingsAccountId, validMtdRequestBodyJson).validateAndWrapResult()
        result shouldBe Left(
          ErrorWrapper(correlationId, RuleTaxYearNotSupportedError)
        )
      }
    }

    "return RuleTaxYearRangeInvalidError error" when {
      "an invalid tax year range is supplied" in {
        val result = validator(validNino, "2017-19", validSavingsAccountId, validMtdRequestBodyJson).validateAndWrapResult()
        result shouldBe Left(
          ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError)
        )
      }
    }

    "return RuleIncorrectOrEmptyBodyError error" when {
      "an empty JSON mtdBody is submitted" in {
        val result: Either[ErrorWrapper, CreateAmendUkSavingsAnnualSummaryRequestData] =
          validator(validNino, validTaxYear, validSavingsAccountId, emptyRequestBodyJson).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError)
        )
      }

      "a non-empty JSON mtdBody is submitted without any expected fields" in {
        val result: Either[ErrorWrapper, CreateAmendUkSavingsAnnualSummaryRequestData] =
          validator(validNino, validTaxYear, validSavingsAccountId, nonsenseRequestBodyJson).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError)
        )
      }

      "the submitted request mtdBody is not in the correct format" in {
        val result: Either[ErrorWrapper, CreateAmendUkSavingsAnnualSummaryRequestData] =
          validator(validNino, validTaxYear, validSavingsAccountId, nonValidRequestBodyJson).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            RuleIncorrectOrEmptyBodyError.withPaths(
              Seq(
                "/taxedUkInterest"
              ))
          )
        )
      }

      "the submitted request mtdBody has missing mandatory fields" in {
        val result: Either[ErrorWrapper, CreateAmendUkSavingsAnnualSummaryRequestData] =
          validator(validNino, validTaxYear, validSavingsAccountId, missingMandatoryFieldsJson).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            RuleIncorrectOrEmptyBodyError
          )
        )
      }

      "the submitted request mtdBody contains empty objects" in {
        val result: Either[ErrorWrapper, CreateAmendUkSavingsAnnualSummaryRequestData] =
          validator(validNino, validTaxYear, validSavingsAccountId, emptyArrayJson).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            RuleIncorrectOrEmptyBodyError.withPath(
              "/taxedUkInterest"
            )
          ))
      }
    }

    "return multiple errors" when {
      "multiple invalid parameters are provided" in {
        val result = validator("not-a-nino", "2017-19", validSavingsAccountId, validMtdRequestBodyJson).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            BadRequestError,
            Some(List(NinoFormatError, RuleTaxYearRangeInvalidError))
          )
        )
      }
    }

    "return ValueFormatError error" when {
      "one field fails value validation" in {
        val result: Either[ErrorWrapper, CreateAmendUkSavingsAnnualSummaryRequestData] =
          validator(validNino, validTaxYear, validSavingsAccountId, oneBadValueFieldJson).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            ValueFormatError.copy(
              paths = Some(
                Seq(
                  "/taxedUkInterest"
                ))
            )
          ))
      }

      "multiple fields fails value validation" in {
        val result: Either[ErrorWrapper, CreateAmendUkSavingsAnnualSummaryRequestData] =
          validator(validNino, validTaxYear, validSavingsAccountId, allBadValueFieldsJson).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            ValueFormatError.copy(
              paths = Some(
                Seq(
                  "/taxedUkInterest",
                  "/untaxedUkInterest"
                ))
            )
          ))
      }
    }
  }

}
