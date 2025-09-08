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

import play.api.libs.functional.syntax.*
import play.api.libs.json.*

case class Def1_UkSavingsAccount(savingsAccountId: String, accountName: Option[String])

object Def1_UkSavingsAccount {

  implicit val writes: OWrites[Def1_UkSavingsAccount] = Json.writes[Def1_UkSavingsAccount]

  implicit val reads: Reads[Def1_UkSavingsAccount] = (
    (JsPath \ "incomeSourceId").read[String] and
      (JsPath \ "incomeSourceName").readNullable[String]
  )(Def1_UkSavingsAccount.apply)

}
