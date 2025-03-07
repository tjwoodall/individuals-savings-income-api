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

package v1.addUkSavingsAccount.def1.model.request

import play.api.libs.json.{Json, OWrites, Reads}
import shared.config.{ConfigFeatureSwitches, SharedAppConfig}
import v1.addUkSavingsAccount.model.request.AddUkSavingsAccountRequestBody

case class Def1_AddUkSavingsAccountRequestBody(accountName: String) extends AddUkSavingsAccountRequestBody

object Def1_AddUkSavingsAccountRequestBody {

  implicit val reads: Reads[Def1_AddUkSavingsAccountRequestBody] = Json.reads[Def1_AddUkSavingsAccountRequestBody]

  implicit def writes(implicit appConfig: SharedAppConfig): OWrites[Def1_AddUkSavingsAccountRequestBody] =
    (addUkSavingsRequestBody: Def1_AddUkSavingsAccountRequestBody) => {
      val incomeSourceType = if (ConfigFeatureSwitches().isEnabled("des_hip_migration_1393")) "09" else "interest-from-uk-banks"

      Json.obj(
        "incomeSourceType" -> incomeSourceType,
        "incomeSourceName" -> addUkSavingsRequestBody.accountName
      )
    }

}
