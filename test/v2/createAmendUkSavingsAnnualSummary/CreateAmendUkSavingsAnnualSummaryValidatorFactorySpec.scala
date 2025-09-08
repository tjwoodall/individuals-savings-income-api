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

package v2.createAmendUkSavingsAnnualSummary

import play.api.libs.json.{JsValue, Json}
import shared.config.MockSharedAppConfig
import shared.controllers.validators.Validator
import shared.utils.UnitSpec
import v2.createAmendUkSavingsAnnualSummary.def1.Def1_CreateAmendUkSavingsAnnualSummaryValidator
import v2.createAmendUkSavingsAnnualSummary.model.request.CreateAmendUkSavingsAnnualSummaryRequestData

class CreateAmendUkSavingsAnnualSummaryValidatorFactorySpec extends UnitSpec with MockSharedAppConfig {
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

  val validatorFactory = new CreateAmendUkSavingsAnnualSummaryValidatorFactory(mockSharedAppConfig)

  "validator()" when {
    "given any tax year" should {
      "return the Validator for schema definition 1" in {
        val result: Validator[CreateAmendUkSavingsAnnualSummaryRequestData] =
          validatorFactory.validator(validNino, validTaxYear, validSavingsAccountId, validMtdRequestBodyJson)
        result shouldBe a[Def1_CreateAmendUkSavingsAnnualSummaryValidator]
      }

    }
  }

}
