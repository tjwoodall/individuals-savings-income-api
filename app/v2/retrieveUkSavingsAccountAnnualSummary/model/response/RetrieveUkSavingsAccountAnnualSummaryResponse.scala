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

package v2.retrieveUkSavingsAccountAnnualSummary.model.response

import play.api.libs.json.{JsObject, Json, OWrites}
import shared.utils.JsonWritesUtil
import v2.retrieveUkSavingsAccountAnnualSummary.def1.model.response.Def1_RetrieveUkSavingsAccountAnnualSummaryResponse

trait RetrieveUkSavingsAccountAnnualSummaryResponse {
  val savingsInterestAnnualIncome: Seq[RetrieveUkSavingsAnnualIncomeItem]
}

object RetrieveUkSavingsAccountAnnualSummaryResponse extends JsonWritesUtil {

  implicit val writes: OWrites[RetrieveUkSavingsAccountAnnualSummaryResponse] = writesFrom {
    case a: Def1_RetrieveUkSavingsAccountAnnualSummaryResponse =>
      Json.toJson(a).as[JsObject]
  }

}

trait RetrieveUkSavingsAnnualIncomeItem
