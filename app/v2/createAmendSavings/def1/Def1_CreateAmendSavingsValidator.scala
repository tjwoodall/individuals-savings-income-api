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

package v2.createAmendSavings.def1

import cats.data.Validated
import cats.implicits.catsSyntaxTuple3Semigroupal
import play.api.libs.json.JsValue
import shared.config.SharedAppConfig
import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers.*
import shared.models.domain.TaxYear
import shared.models.errors.MtdError
import v2.createAmendSavings.def1.Def1_CreateAmendSavingsRulesValidator.validateBusinessRules
import v2.createAmendSavings.def1.model.request.{Def1_CreateAmendSavingsRequestBody, Def1_CreateAmendSavingsRequestData}
import v2.createAmendSavings.model.request.CreateAmendSavingsRequestData

class Def1_CreateAmendSavingsValidator(nino: String, taxYear: String, body: JsValue)(appConfig: SharedAppConfig)
    extends Validator[CreateAmendSavingsRequestData] {

  private lazy val minimumTaxYear = appConfig.minimumPermittedTaxYear
  private lazy val resolveTaxYear = ResolveTaxYearMinimum(TaxYear.ending(minimumTaxYear))
  private val resolveJson         = new ResolveNonEmptyJsonObject[Def1_CreateAmendSavingsRequestBody]()

  def validate: Validated[Seq[MtdError], CreateAmendSavingsRequestData] =
    (
      ResolveNino(nino),
      resolveTaxYear(taxYear),
      resolveJson(body)
    ).mapN(Def1_CreateAmendSavingsRequestData.apply) andThen validateBusinessRules

}
