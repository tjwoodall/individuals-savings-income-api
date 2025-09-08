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

package v2.createAmendSavings

import play.api.libs.json.{JsValue, Json}
import shared.config.MockSharedAppConfig
import shared.controllers.validators.Validator
import shared.utils.UnitSpec
import v2.createAmendSavings.def1.Def1_CreateAmendSavingsValidator
import v2.createAmendSavings.model.request.CreateAmendSavingsRequestData

class CreateAmendSavingsValidatorFactorySpec extends UnitSpec with MockSharedAppConfig {
  private val validNino    = "AA123456A"
  private val validTaxYear = "2020-21"

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

  private val validatorFactory = new CreateAmendSavingsValidatorFactory(mockSharedAppConfig)

  "validator()" when {
    "given any tax year" should {
      "return the Validator for schema definition 1" in {
        val requestBody = validRequestBodyJson
        val result: Validator[CreateAmendSavingsRequestData] =
          validatorFactory.validator(validNino, validTaxYear, requestBody)
        result shouldBe a[Def1_CreateAmendSavingsValidator]
      }
    }
  }

}
