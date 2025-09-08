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

package v2.listUkSavingsAccounts.def1.model.response

import play.api.libs.json.*
import utils.JsonUtils
import v2.listUkSavingsAccounts.model.response.ListUkSavingsAccountsResponse

case class Def1_ListUkSavingsAccountsResponse(savingsAccounts: Option[Seq[Def1_UkSavingsAccount]]) extends ListUkSavingsAccountsResponse

object Def1_ListUkSavingsAccountsResponse extends JsonUtils {

  implicit val writes: OWrites[Def1_ListUkSavingsAccountsResponse] = Json.writes[Def1_ListUkSavingsAccountsResponse]

  implicit val reads: Reads[Def1_ListUkSavingsAccountsResponse] = {
    case JsObject(fields) if fields.size == 1 && fields.contains("bbsi") =>
      fields
        .get("bbsi")
        .map(arr =>
          arr.validate(
            JsPath
              .readNullable[Seq[Def1_UkSavingsAccount]]
              .mapEmptySeqToNone
              .map(Def1_ListUkSavingsAccountsResponse(_))))
        .getOrElse(JsError("Unexpected JSON format"))

    case _ =>
      JsError("Unexpected JSON format")
  }

}
