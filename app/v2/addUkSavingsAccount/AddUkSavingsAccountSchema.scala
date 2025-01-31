/*
 * Copyright 2024 HM Revenue & Customs
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

package v2.addUkSavingsAccount

import play.api.libs.json.Reads
import shared.schema.DownstreamReadable
import v2.addUkSavingsAccount.def1.model.response.Def1_AddUkSavingsAccountResponse
import v2.addUkSavingsAccount.model.response.AddUkSavingsAccountResponse

sealed trait AddUkSavingsAccountSchema extends DownstreamReadable[AddUkSavingsAccountResponse]

object AddUkSavingsAccountSchema {

  case object Def1 extends AddUkSavingsAccountSchema {
    type DownstreamResp = Def1_AddUkSavingsAccountResponse
    val connectorReads: Reads[DownstreamResp] = Def1_AddUkSavingsAccountResponse.reads
  }

}
