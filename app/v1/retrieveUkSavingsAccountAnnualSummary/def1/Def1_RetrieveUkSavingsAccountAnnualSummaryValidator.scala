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

package v1.retrieveUkSavingsAccountAnnualSummary.def1

import cats.data.Validated
import cats.implicits.catsSyntaxTuple3Semigroupal
import resolvers.ResolveSavingsAccountId
import shared.config.SharedAppConfig
import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers.{ResolveNino, ResolveTaxYearMinimum}
import shared.models.domain.TaxYear
import shared.models.errors.MtdError
import v1.retrieveUkSavingsAccountAnnualSummary.def1.model.request.Def1_RetrieveUkSavingsAccountAnnualSummaryRequestData
import v1.retrieveUkSavingsAccountAnnualSummary.model.request.RetrieveUkSavingsAccountAnnualSummaryRequestData

class Def1_RetrieveUkSavingsAccountAnnualSummaryValidator(nino: String, taxYear: String, savingsAccountId: String)(appConfig: SharedAppConfig)
    extends Validator[RetrieveUkSavingsAccountAnnualSummaryRequestData] {
  private lazy val minimumTaxYear = appConfig.minimumPermittedTaxYear
  private lazy val resolveTaxYear = ResolveTaxYearMinimum(TaxYear.ending(minimumTaxYear))

  def validate: Validated[Seq[MtdError], RetrieveUkSavingsAccountAnnualSummaryRequestData] =
    (
      ResolveNino(nino),
      resolveTaxYear(taxYear),
      ResolveSavingsAccountId(savingsAccountId)
    ).mapN(Def1_RetrieveUkSavingsAccountAnnualSummaryRequestData.apply)

}
