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

package v2.createAmendSavings.def1.model.request

import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.{JsPath, OWrites, Reads}
import utils.JsonUtils
import v2.createAmendSavings.model.request.CreateAmendSavingsRequestBody

case class Def1_CreateAmendSavingsRequestBody(securities: Option[AmendSecurities], foreignInterest: Option[Seq[AmendForeignInterestItem]])
    extends CreateAmendSavingsRequestBody

object Def1_CreateAmendSavingsRequestBody extends JsonUtils {
  val empty: Def1_CreateAmendSavingsRequestBody = Def1_CreateAmendSavingsRequestBody(None, None)

  implicit val reads: Reads[Def1_CreateAmendSavingsRequestBody] = (
    (JsPath \ "securities").readNullable[AmendSecurities] and
      (JsPath \ "foreignInterest").readNullable[Seq[AmendForeignInterestItem]].mapEmptySeqToNone
  )(Def1_CreateAmendSavingsRequestBody.apply _)

  implicit val writes: OWrites[Def1_CreateAmendSavingsRequestBody] = (
    (JsPath \ "securities").writeNullable[AmendSecurities] and
      (JsPath \ "foreignInterest").writeNullable[Seq[AmendForeignInterestItem]]
  )(unlift(Def1_CreateAmendSavingsRequestBody.unapply))

}
