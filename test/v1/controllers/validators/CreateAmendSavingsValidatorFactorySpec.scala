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

import shared.config.MockAppConfig
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import play.api.libs.json.{JsValue, Json}
import support.UnitSpec
import v1.models.request.amendSavings.{CreateAmendSavingsRequestBody, CreateAmendSavingsRequestData}

class CreateAmendSavingsValidatorFactorySpec extends UnitSpec with MockAppConfig {

  private implicit val correlationId: String = "1234"

  private val validNino    = "AA123456A"
  private val validTaxYear = "2020-21"

  private val validValue   = 1000.12

  private val validRequestBodyJson: JsValue = Json.parse(
    s"""
       |{
       |  "securities": {
       |    "taxTakenOff": 100.0,
       |    "grossAmount": 1455.0,
       |    "netAmount": 123.22
       |  },
       |  "foreignInterest": [
       |    {
       |      "amountBeforeTax": 1232.22,
       |      "countryCode": "FRA",
       |      "taxTakenOff": 22.22,
       |      "specialWithholdingTax": 22.22,
       |      "taxableAmount": 2321.22,
       |      "foreignTaxCreditRelief": true
       |    }
       |  ]
       |}
""".stripMargin
  )

  private val oneBadValueFieldJson: JsValue = Json.parse(
    s"""
       |{
       |  "securities": {
       |    "taxTakenOff": $validValue,
       |    "grossAmount": $validValue,
       |    "netAmount": $validValue
       |  },
       |  "foreignInterest": [
       |    {
       |      "amountBeforeTax": $validValue,
       |      "countryCode": "DEU",
       |      "taxTakenOff": 1000.123,
       |      "specialWithholdingTax": $validValue,
       |      "taxableAmount": $validValue,
       |      "foreignTaxCreditRelief": true
       |    }
       |  ]
       |}
""".stripMargin
  )

  private val allBadValueFieldsJson: JsValue = Json.parse(
    s"""
       |{
       |  "securities": {
       |    "taxTakenOff": 1000.123,
       |    "grossAmount": 1000.123,
       |    "netAmount": 1000.123
       |  },
       |  "foreignInterest": [
       |    {
       |      "amountBeforeTax": 1000.123,
       |      "countryCode": "DEU",
       |      "taxTakenOff": 1000.123,
       |      "taxableAmount": 1000.123,
       |      "specialWithholdingTax": 1000.123,
       |      "foreignTaxCreditRelief": true
       |    }
       |  ]
       |}
""".stripMargin
  )

  private val emptyRequestBodyJson: JsValue = Json.parse("""{}""")

  private val nonsenseRequestBodyJson: JsValue = Json.parse("""{"field": "value"}""")

  private val nonValidRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |  "securities":
      |    {
      |      "taxTakenOff": true
      |    }
      |}
""".stripMargin
  )

  private val missingMandatoryFieldsJson: JsValue = Json.parse(
    """
      |{
      |  "foreignInterest":[{}]
      |}
""".stripMargin
  )

  private val emptyArrayJson: JsValue = Json.parse(
    """
      |{
      |  "foreignInterest": []
      |}
""".stripMargin
  )

  private val parsedNino    = Nino(validNino)
  private val parsedTaxYear = TaxYear.fromMtd(validTaxYear)
  private val parsedBody    = validRequestBodyJson.as[CreateAmendSavingsRequestBody]

  val validatorFactory = new CreateAmendSavingsValidatorFactory

  private def validator(nino: String, taxYear: String, body: JsValue) =
    validatorFactory.validator(nino, taxYear, body)

  "validator" should {
    "return the parsed domain object" when {
      "a valid request is supplied" in {
        val result = validator(validNino, validTaxYear, validRequestBodyJson).validateAndWrapResult()
        result shouldBe Right(CreateAmendSavingsRequestData(parsedNino, parsedTaxYear, parsedBody))
      }
    }

    "return NinoFormatError error" when {
      "an invalid nino is supplied" in {
        val result = validator("A12344A", validTaxYear, validRequestBodyJson).validateAndWrapResult()
        result shouldBe Left(
          ErrorWrapper(correlationId, NinoFormatError)
        )
      }
    }

    "return TaxYearFormatError error" when {
      "an invalid tax year is supplied" in {
        val result = validator(validNino, "201718", validRequestBodyJson).validateAndWrapResult()
        result shouldBe Left(
          ErrorWrapper(correlationId, TaxYearFormatError)
        )
      }
    }

    "return RuleTaxYearNotSupportedError error" when {
      "an out of range tax year is supplied" in {
        val result = validator(validNino, "2016-17", validRequestBodyJson).validateAndWrapResult()
        result shouldBe Left(
          ErrorWrapper(correlationId, RuleTaxYearNotSupportedError)
        )
      }
    }

    "return RuleTaxYearRangeInvalidError error" when {
      "an invalid tax year range is supplied" in {
        val result = validator(validNino, "2017-19", validRequestBodyJson).validateAndWrapResult()
        result shouldBe Left(
          ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError)
        )
      }
    }

    "return RuleIncorrectOrEmptyBodyError error" when {
      "an empty JSON body is submitted" in {
        val result: Either[ErrorWrapper, CreateAmendSavingsRequestData] =
          validator(validNino, validTaxYear, emptyRequestBodyJson).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError)
        )
      }

      "a non-empty JSON body is submitted without any expected fields" in {
        val result: Either[ErrorWrapper, CreateAmendSavingsRequestData] =
          validator(validNino, validTaxYear, nonsenseRequestBodyJson).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError)
        )
      }

      "the submitted request body is not in the correct format" in {
        val result: Either[ErrorWrapper, CreateAmendSavingsRequestData] =
          validator(validNino, validTaxYear, nonValidRequestBodyJson).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            RuleIncorrectOrEmptyBodyError.withPaths(Seq(
              "/securities/grossAmount",
              "/securities/taxTakenOff"
            ))
          )
        )
      }

      "the submitted request body has missing mandatory fields" in {
        val result: Either[ErrorWrapper, CreateAmendSavingsRequestData] =
          validator(validNino, validTaxYear, missingMandatoryFieldsJson).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            RuleIncorrectOrEmptyBodyError.withPaths(Seq(
              "/foreignInterest/0/countryCode",
              "/foreignInterest/0/taxableAmount"
            ))
          )
        )
      }

      "the submitted request body contains empty objects" in {
        val result: Either[ErrorWrapper, CreateAmendSavingsRequestData] =
          validator(validNino, validTaxYear, emptyArrayJson).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            RuleIncorrectOrEmptyBodyError
          ))
      }
    }

    "return multiple errors" when {
      "multiple invalid parameters are provided" in {
        val result = validator("not-a-nino", "2017-19", validRequestBodyJson).validateAndWrapResult()

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
        val result: Either[ErrorWrapper, CreateAmendSavingsRequestData] =
          validator(validNino, validTaxYear, oneBadValueFieldJson).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            ValueFormatError.copy(
              paths = Some(Seq(
                "/foreignInterest/0/taxTakenOff"
              ))
            )
          ))
      }

      "multiple fields fails value validation" in {
        val result: Either[ErrorWrapper, CreateAmendSavingsRequestData] =
          validator(validNino, validTaxYear, allBadValueFieldsJson).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            ValueFormatError.copy(
              paths = Some(Seq(
                "/securities/grossAmount",
                "/securities/taxTakenOff",
                "/securities/netAmount",
                "/foreignInterest/0/taxableAmount",
                "/foreignInterest/0/amountBeforeTax",
                "/foreignInterest/0/taxTakenOff",
                "/foreignInterest/0/specialWithholdingTax"
              ))
            )
          ))
      }
    }
  }
}
