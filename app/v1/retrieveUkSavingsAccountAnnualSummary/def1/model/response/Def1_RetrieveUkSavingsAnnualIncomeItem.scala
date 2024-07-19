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

package v1.retrieveUkSavingsAccountAnnualSummary.def1.model.response

import play.api.libs.json.{Json, Reads}
import v1.retrieveUkSavingsAccountAnnualSummary.model.response.RetrieveUkSavingsAnnualIncomeItem

case class Def1_RetrieveUkSavingsAnnualIncomeItem(incomeSourceId: String, taxedUkInterest: Option[BigDecimal], untaxedUkInterest: Option[BigDecimal])
    extends RetrieveUkSavingsAnnualIncomeItem

object Def1_RetrieveUkSavingsAnnualIncomeItem {
  implicit val reads: Reads[Def1_RetrieveUkSavingsAnnualIncomeItem] = Json.reads[Def1_RetrieveUkSavingsAnnualIncomeItem]
}
