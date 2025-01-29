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

package v2.addUkSavingsAccount.def1

import cats.data.Validated
import cats.implicits._
import play.api.libs.json.JsValue
import resolvers.ResolveAccountName
import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers.{ResolveNino, ResolveNonEmptyJsonObject}
import shared.models.errors.MtdError
import v2.addUkSavingsAccount.def1.model.request.{Def1_AddUkSavingsAccountRequestBody, Def1_AddUkSavingsAccountRequestData}
import v2.addUkSavingsAccount.model.request.AddUkSavingsAccountRequestData

class Def1_AddUkSavingsAccountValidator(nino: String, body: JsValue) extends Validator[AddUkSavingsAccountRequestData] {

  private val resolveJson = new ResolveNonEmptyJsonObject[Def1_AddUkSavingsAccountRequestBody]()

  def validate: Validated[Seq[MtdError], AddUkSavingsAccountRequestData] = (
    ResolveNino(nino),
    resolveJson(body)
  ).mapN(Def1_AddUkSavingsAccountRequestData) andThen validateBusinessRules

  private def validateBusinessRules(parsed: Def1_AddUkSavingsAccountRequestData): Validated[Seq[MtdError], Def1_AddUkSavingsAccountRequestData] = {
    import parsed.body._
    List((accountName, "/accountName")).traverse_ { case (value, path) => ResolveAccountName(value, path) }.map(_ => parsed)

  }

}
