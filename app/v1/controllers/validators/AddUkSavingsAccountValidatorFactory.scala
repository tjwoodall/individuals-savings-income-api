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

package v1.controllers.validators

import cats.data.Validated
import cats.implicits.{catsSyntaxTuple2Semigroupal, toFoldableOps}
import play.api.libs.json.JsValue
import resolvers.ResolveAccountName
import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers.{ResolveNino, ResolveNonEmptyJsonObject}
import shared.models.errors.MtdError
import v1.models.request.addUkSavingsAccount.{AddUkSavingsAccountRequestBody, AddUkSavingsAccountRequestData}

import javax.inject.Inject

class AddUkSavingsAccountValidatorFactory @Inject() {

  private val resolveJson = new ResolveNonEmptyJsonObject[AddUkSavingsAccountRequestBody]()

  def validator(nino: String, body: JsValue): Validator[AddUkSavingsAccountRequestData] = new Validator[AddUkSavingsAccountRequestData] {

    def validate: Validated[Seq[MtdError], AddUkSavingsAccountRequestData] = (
      ResolveNino(nino),
      resolveJson(body)
    ).mapN(AddUkSavingsAccountRequestData) andThen validateBusinessRules

  }

  private def validateBusinessRules(parsed: AddUkSavingsAccountRequestData): Validated[Seq[MtdError], AddUkSavingsAccountRequestData] = {
    import parsed.body.accountName

    List((accountName, "/accountName")).traverse_ { case (value, path) => ResolveAccountName(value, path) }.map(_ => parsed)

  }

}
