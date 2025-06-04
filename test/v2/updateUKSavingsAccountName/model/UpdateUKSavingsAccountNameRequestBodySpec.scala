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

package v2.updateUKSavingsAccountName.model

import play.api.libs.json.{JsError, JsObject, JsValue, Json}
import shared.utils.UnitSpec
import v2.updateUKSavingsAccountName.fixture.UpdateUKSavingsAccountNameFixtures.{requestBodyModel, validRequestJson}
import v2.updateUKSavingsAccountName.model.request.UpdateUKSavingsAccountNameRequestBody

class UpdateUKSavingsAccountNameRequestBodySpec extends UnitSpec {

  val downstreamJson: JsValue = Json.parse(
    """
      |{
      |   "incomeSourceName": "Shares savings account"
      |}
      """.stripMargin
  )

  "UpdateUKSavingsAccountNameRequestBody" when {
    "read from valid JSON" should {
      "produce the expected UpdateUKSavingsAccountNameRequestBody model" in {
        validRequestJson.as[UpdateUKSavingsAccountNameRequestBody] shouldBe requestBodyModel
      }
    }

    "read from invalid JSON" should {
      "produce a JsError" in {
        val invalidJson: JsObject = JsObject.empty

        invalidJson.validate[UpdateUKSavingsAccountNameRequestBody] shouldBe a[JsError]
      }
    }

    "written to JSON" should {
      "produce the expected JSON" in {
        Json.toJson(requestBodyModel) shouldBe downstreamJson
      }
    }
  }

}
