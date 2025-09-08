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

package v2.retrieveSavings.def1.model.response

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.*
import shared.models.domain.Timestamp
import utils.JsonUtils
import v2.retrieveSavings.model.response.RetrieveSavingsResponse

case class Def1_RetrieveSavingsResponse(submittedOn: Timestamp, securities: Option[Securities], foreignInterest: Option[Seq[ForeignInterestItem]])
    extends RetrieveSavingsResponse

object Def1_RetrieveSavingsResponse extends JsonUtils {

  implicit val reads: Reads[Def1_RetrieveSavingsResponse] = (
    (JsPath \ "submittedOn").read[Timestamp] and
      (JsPath \ "securities").readNullable[Securities] and
      (JsPath \ "foreignInterest").readNullable[Seq[ForeignInterestItem]].mapEmptySeqToNone
  )(Def1_RetrieveSavingsResponse.apply)

  implicit val writes: OWrites[Def1_RetrieveSavingsResponse] = Json.writes[Def1_RetrieveSavingsResponse]

}
