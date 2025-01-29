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

package v2.createAmendUkSavingsAnnualSummary.def1.model.request

import models.domain.SavingsAccountId
import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.{JsPath, OWrites, Reads}
import v2.createAmendUkSavingsAnnualSummary.model.request.CreateAmendUkSavingsAnnualSummaryRequestBody

case class Def1_CreateAmendUkSavingsAnnualSummaryRequestBody(taxedUkInterest: Option[BigDecimal], untaxedUkInterest: Option[BigDecimal])
    extends CreateAmendUkSavingsAnnualSummaryRequestBody {

  def asDownstreamRequestBody(savingsAccountId: SavingsAccountId): Def1_DownstreamCreateAmendUkSavingsAnnualSummaryRequestBody = {
    Def1_DownstreamCreateAmendUkSavingsAnnualSummaryRequestBody(
      incomeSourceId = savingsAccountId.toString,
      taxedUkInterest = taxedUkInterest,
      untaxedUkInterest = untaxedUkInterest
    )
  }

}

object Def1_CreateAmendUkSavingsAnnualSummaryRequestBody {

  implicit val reads: Reads[Def1_CreateAmendUkSavingsAnnualSummaryRequestBody] = (
    (JsPath \ "taxedUkInterest").readNullable[BigDecimal] and
      (JsPath \ "untaxedUkInterest").readNullable[BigDecimal]
  )(Def1_CreateAmendUkSavingsAnnualSummaryRequestBody.apply _)

  implicit val writes: OWrites[Def1_CreateAmendUkSavingsAnnualSummaryRequestBody] = (
    (JsPath \ "taxedUkInterest").writeNullable[BigDecimal] and
      (JsPath \ "untaxedUkInterest").writeNullable[BigDecimal]
  )(unlift(Def1_CreateAmendUkSavingsAnnualSummaryRequestBody.unapply))

}
