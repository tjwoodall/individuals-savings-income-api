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

package v1.listUkSavingsAccounts.def1.model.response

import play.api.libs.json.{JsError, Json}
import shared.utils.UnitSpec

class Def1_UkSavingsAccountSpec extends UnitSpec {

  val validUkSavingsAccountFromDESJson = Json.parse(
    """
      |{
      |   "incomeSourceId": "SAVKB2UVwUTBQGJ",
      |   "incomeSourceName": "Shares savings account"
      |}
    """.stripMargin
  )

  val validNoOptUkSavingsAccountFromDESJson = Json.parse(
    """
      |{
      |   "incomeSourceId": "SAVKB2UVwUTBQGJ"
      |}
    """.stripMargin
  )

  val invalidUkSavingsAccountFromDESJson = Json.parse(
    """
      |{
      |   "incomeSourceName": "Shares savings account"
      |}
    """.stripMargin
  )

  val validUkSavigsAccountFromMTDJson = Json.parse(
    """
      |{
      |   "savingsAccountId": "SAVKB2UVwUTBQGJ",
      |   "accountName": "Shares savings account"
      |}
    """.stripMargin
  )

  val validNoOptUkSavingsAccountFromMTDJson = Json.parse(
    """
      |{
      |   "savingsAccountId": "SAVKB2UVwUTBQGJ"
      |}
    """.stripMargin
  )

  val emptyJson = Json.parse("{}")

  "Def1_UkSavingsAccount" should {
    "return a valid Def1_UkSavingsAccount model " when {
      "a valid uk savings account json from DES is supplied" in {
        validUkSavingsAccountFromDESJson.as[Def1_UkSavingsAccount] shouldBe
          Def1_UkSavingsAccount("SAVKB2UVwUTBQGJ", Some("Shares savings account"))
      }

      "a valid uk savings account json with mandatory fields only from DES is supplied" in {
        validNoOptUkSavingsAccountFromDESJson.as[Def1_UkSavingsAccount] shouldBe
          Def1_UkSavingsAccount("SAVKB2UVwUTBQGJ",None)
      }
    }

    "return a JsError" when {
      "an invalid uk savings account json from DES is supplied" in {
        invalidUkSavingsAccountFromDESJson.validate[Def1_UkSavingsAccount] shouldBe a[JsError]
      }
    }

    "return a JsError" when {
      "an empty json from DES is supplied" in {
        emptyJson.validate[Def1_UkSavingsAccount] shouldBe a[JsError]
      }
    }

    "return a valid MTD uk savings json" when {
      "a valid Def1_UkSavingsAccount model is supplier" in {
        Json.toJson(Def1_UkSavingsAccount("SAVKB2UVwUTBQGJ", Some("Shares savings account"))) shouldBe
          validUkSavigsAccountFromMTDJson
      }
      
      "a valid Def1_UkSavingsAccount model with mandatory fields only is supplier" in {
        Json.toJson(Def1_UkSavingsAccount("SAVKB2UVwUTBQGJ",None)) shouldBe
          validNoOptUkSavingsAccountFromMTDJson
      }
    }
  }

}
