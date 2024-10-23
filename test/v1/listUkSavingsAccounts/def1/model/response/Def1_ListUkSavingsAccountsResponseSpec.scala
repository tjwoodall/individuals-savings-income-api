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
import shared.UnitSpec

class Def1_ListUkSavingsAccountsResponseSpec extends UnitSpec {

  private val ukSavingsAccountsDesJs = Json.parse(
    """
      |[
      |  {
      |    "incomeSourceId": "000000000000001",
      |    "incomeSourceName": "Bank Account 1",
      |    "identifier": "AA111111A",
      |    "incomeSourceType": "interest-from-uk-banks"
      |  },
      |  {
      |    "incomeSourceId": "000000000000002",
      |    "incomeSourceName": "Bank Account 2",
      |    "identifier": "AA111111A",
      |    "incomeSourceType": "interest-from-uk-banks"
      |  },
      |  {
      |    "incomeSourceId": "000000000000003"
      |  }
      |]
    """.stripMargin
  )

  private val ukSavingsAccountsIfsJs = Json.parse(
    """
      |{
      |  "bbsi": [
      |      {
      |        "incomeSourceId": "000000000000001",
      |        "incomeSourceName": "Bank Account 1",
      |        "startDate": "2024-08-22",
      |        "endDate": "2024-08-22"
      |      },
      |      {
      |        "incomeSourceId": "000000000000002",
      |        "incomeSourceName": "Bank Account 2",
      |        "startDate": "2024-08-22",
      |        "endDate": "2024-08-22"
      |      },
      |      {
      |        "incomeSourceId": "000000000000003"
      |      }
      |  ]
      |}
    """.stripMargin
  )

  private val ukSavingsAccountsNotBbsiIfsJs = Json.parse(
    """
      |{
      |  "not_bbsi": [
      |      {
      |        "incomeSourceId": "000000000000003"
      |      }
      |  ]
      |}
    """.stripMargin
  )

  private val ukSavingsAccountsMtdJs = Json.parse(
    """
      |{
      |  "savingsAccounts":
      |  [
      |    {
      |        "savingsAccountId": "000000000000001",
      |        "accountName": "Bank Account 1"
      |    },
      |    {
      |        "savingsAccountId": "000000000000002",
      |        "accountName": "Bank Account 2"
      |    },
      |    {
      |        "savingsAccountId": "000000000000003"
      |    }
      |  ]
      |}
    """.stripMargin
  )

  private val emptyJson = Json.parse("""[]""")

  private val listUkSavingsAccountResponse = Def1_ListUkSavingsAccountsResponse(
    Some(
      List(
        Def1_UkSavingsAccount("000000000000001", Some("Bank Account 1")),
        Def1_UkSavingsAccount("000000000000002", Some("Bank Account 2")),
        Def1_UkSavingsAccount("000000000000003", None)
      )
    )
  )

  "ListUkSavingsAccountsResponse.reads" should {
    "return a parsed ListUkSavingsAccountsResponse" when {
      "given a valid JSON document from DES" in {
        val result = ukSavingsAccountsDesJs.as[Def1_ListUkSavingsAccountsResponse[Def1_UkSavingsAccount]]
        result shouldBe listUkSavingsAccountResponse
      }

      "given a valid JSON document from IFS" in {
        val result = ukSavingsAccountsIfsJs.as[Def1_ListUkSavingsAccountsResponse[Def1_UkSavingsAccount]]
        result shouldBe listUkSavingsAccountResponse
      }
    }

    "return a JSON error" when {
      "given an IFS response with an object not called bbsi" in {
        val result = ukSavingsAccountsNotBbsiIfsJs.validate[Def1_ListUkSavingsAccountsResponse[Def1_UkSavingsAccount]]
        result shouldBe a[JsError]
      }
    }

    "return a valid list uk savings account response MTD json " when {
      "given a valid UkSavingAccountListResponse" in {
        val result = Json.toJson(listUkSavingsAccountResponse)
        result shouldBe ukSavingsAccountsMtdJs
      }
    }

    "return a valid empty ListUkSavingsAccountsResponse model " when {
      "given a valid empty uk savings account list json from DES" in {
        val result = emptyJson.as[Def1_ListUkSavingsAccountsResponse[Def1_UkSavingsAccount]]
        result shouldBe Def1_ListUkSavingsAccountsResponse(None)
      }
    }
  }

}
