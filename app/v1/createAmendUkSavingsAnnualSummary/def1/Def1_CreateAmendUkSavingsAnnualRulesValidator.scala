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

package v1.createAmendUkSavingsAnnualSummary.def1

import cats.data.Validated
import cats.implicits.toFoldableOps
import shared.controllers.validators.RulesValidator
import shared.controllers.validators.resolvers.ResolveParsedNumber
import shared.models.errors.MtdError
import v1.createAmendUkSavingsAnnualSummary.def1.model.request.Def1_CreateAmendUkSavingsAnnualSummaryRequestData

object Def1_CreateAmendUkSavingsAnnualRulesValidator extends RulesValidator[Def1_CreateAmendUkSavingsAnnualSummaryRequestData] {
  private val resolveParsedNumber = ResolveParsedNumber()

  def validateBusinessRules(
      parsed: Def1_CreateAmendUkSavingsAnnualSummaryRequestData): Validated[Seq[MtdError], Def1_CreateAmendUkSavingsAnnualSummaryRequestData] = {
    import parsed._

    val validatedMandatoryDecimalNumbers = List(
      (mtdBody.taxedUkInterest, s"/taxedUkInterest"),
      (mtdBody.untaxedUkInterest, s"/untaxedUkInterest")
    ).traverse_ { case (value, path) =>
      resolveParsedNumber(value, path)
    }

    combine(
      validatedMandatoryDecimalNumbers
    ).onSuccess(parsed)
  }

}
