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

import cats.data.Validated
import cats.implicits.catsSyntaxTuple4Semigroupal
import play.api.libs.json.JsValue
import resolvers.ResolveSavingsAccountId
import shared.config.SharedAppConfig
import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers.{ResolveNino, ResolveNonEmptyJsonObject, ResolveTaxYearMinimum}
import shared.models.domain.TaxYear
import shared.models.errors.MtdError
import config.SavingsConfig
import v2.createAmendUkSavingsAnnualSummary.def1.Def1_CreateAmendUkSavingsAnnualRulesValidator.validateBusinessRules
import v2.createAmendUkSavingsAnnualSummary.def1.model.request.{
  Def1_CreateAmendUkSavingsAnnualSummaryRequestBody,
  Def1_CreateAmendUkSavingsAnnualSummaryRequestData
}
import v2.createAmendUkSavingsAnnualSummary.model.request._

class Def1_CreateAmendUkSavingsAnnualSummaryValidator(nino: String, taxYear: String, savingsAccountId: String, body: JsValue)(
    appConfig: SharedAppConfig,
    savingsConfig: SavingsConfig)
    extends Validator[CreateAmendUkSavingsAnnualSummaryRequestData] {
  private lazy val minimumTaxYear = savingsConfig.ukSavingsAccountAnnualSummaryMinimumTaxYear
  private lazy val resolveTaxYear = ResolveTaxYearMinimum(TaxYear.fromDownstreamInt(minimumTaxYear))
  private val resolveJson         = new ResolveNonEmptyJsonObject[Def1_CreateAmendUkSavingsAnnualSummaryRequestBody]()

  def validate: Validated[Seq[MtdError], CreateAmendUkSavingsAnnualSummaryRequestData] = {
    (
      ResolveNino(nino),
      resolveTaxYear(taxYear),
      ResolveSavingsAccountId(savingsAccountId),
      resolveJson(body)
    ).mapN(Def1_CreateAmendUkSavingsAnnualSummaryRequestData) andThen validateBusinessRules
  }

}
