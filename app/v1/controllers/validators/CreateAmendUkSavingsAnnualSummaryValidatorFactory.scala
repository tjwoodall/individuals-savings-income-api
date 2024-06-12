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

import api.controllers.validators.resolvers.ResolveSavingsAccountId
import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers.{ResolveNino, ResolveNonEmptyJsonObject, ResolveTaxYearMinimum}
import shared.models.domain.TaxYear
import shared.models.errors.MtdError
import cats.data.Validated
import cats.implicits._
import play.api.libs.json.JsValue
import v1.controllers.validators.CreateAmendUkSavingsAnnualRulesValidator.validateBusinessRules
import v1.models.request.createAmendUkSavingsAnnualSummary.{CreateAmendUkSavingsAnnualSummaryBody, CreateAmendUkSavingsAnnualSummaryRequestData}

import javax.inject.{Inject, Singleton}

@Singleton
class CreateAmendUkSavingsAnnualSummaryValidatorFactory @Inject() {
  private lazy val minimumTaxYear = 2019
  private lazy val resolveTaxYear = ResolveTaxYearMinimum(TaxYear.fromDownstreamInt(minimumTaxYear))
  private val resolveJson         = new ResolveNonEmptyJsonObject[CreateAmendUkSavingsAnnualSummaryBody]()

  def validator(nino: String, taxYear: String, savingsAccountId: String, body: JsValue): Validator[CreateAmendUkSavingsAnnualSummaryRequestData] =
    new Validator[CreateAmendUkSavingsAnnualSummaryRequestData] {

      def validate: Validated[Seq[MtdError], CreateAmendUkSavingsAnnualSummaryRequestData] =
        (
          ResolveNino(nino),
          resolveTaxYear(taxYear),
          ResolveSavingsAccountId(savingsAccountId),
          resolveJson(body)
        ).mapN(CreateAmendUkSavingsAnnualSummaryRequestData) andThen validateBusinessRules

    }

}
