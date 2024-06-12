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

package v1.controllers.requestParsers.validators


import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import cats.implicits.catsSyntaxTuple3Semigroupal
import config.AppConfig
import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers.{ResolveNino, ResolveTaxYearMinimum}
import shared.models.domain.TaxYear
import shared.models.errors.{MtdError, SavingsAccountIdFormatError}
import v1.models.request.retrieveUkSavingsAnnualSummary.RetrieveUkSavingsAnnualSummaryRequestData

import javax.inject.{Inject, Singleton}

@Singleton
class RetrieveUkSavingsAccountValidatorFactory @Inject()(implicit appConfig: AppConfig) {

  private lazy val minimumTaxYear = appConfig.minimumPermittedTaxYear
  private lazy val resolveTaxYear = ResolveTaxYearMinimum(TaxYear.fromDownstreamInt(minimumTaxYear))

  def validator(nino: String, taxYear: String, savingsAccountId: String): Validator[RetrieveUkSavingsAnnualSummaryRequestData] =
    new Validator[RetrieveUkSavingsAnnualSummaryRequestData] {

      def validate: Validated[Seq[MtdError], RetrieveUkSavingsAnnualSummaryRequestData] =
        (
          ResolveNino(nino),
          resolveTaxYear(taxYear),
          resolveSavingsAccountId(savingsAccountId)
          ).mapN(RetrieveUkSavingsAnnualSummaryRequestData)
    }

  private def resolveSavingsAccountId(id: String): Validated[Seq[MtdError], String] = {
    val regex = "^[A-Za-z0-9]{15}$"
        if (id.matches(regex)) Valid(id) else Invalid(Seq[MtdError](SavingsAccountIdFormatError))
  }

}
