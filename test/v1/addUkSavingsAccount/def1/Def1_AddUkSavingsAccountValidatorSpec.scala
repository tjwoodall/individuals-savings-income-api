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

package v1.addUkSavingsAccount.def1

import models.errors.AccountNameFormatError
import play.api.libs.json.{JsObject, JsValue, Json}
import shared.models.domain.Nino
import shared.models.errors.{ErrorWrapper, NinoFormatError, RuleIncorrectOrEmptyBodyError}
import shared.utils.UnitSpec
import v1.addUkSavingsAccount.def1.model.request.{Def1_AddUkSavingsAccountRequestBody, Def1_AddUkSavingsAccountRequestData}
import v1.addUkSavingsAccount.model.request.AddUkSavingsAccountRequestData

class Def1_AddUkSavingsAccountValidatorSpec extends UnitSpec {

  implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"
  private val validNino              = "AA123456A"

  private val validRequestBodyJson = Json.parse(
    """
      |{
      |  "accountName": "Shares savings account"
      |}
    """.stripMargin
  )

  private val nonsenseRequestBodyJson = Json.parse("""{"field": "value"}""")

  private val invalidFieldTypeRequestBodyJson = Json.parse(
    """
      |{
      |  "accountName": []
      |}
  """.stripMargin
  )

  private val invalidValueRequestBodyJson = Json.parse(
    """
      |{
      |  "accountName": ";"
      |}
    """.stripMargin
  )

  private val parsedNino                             = Nino(validNino)
  private val parsedRequestBody                      = Def1_AddUkSavingsAccountRequestBody("Shares savings account")
  private def validator(nino: String, body: JsValue) = new Def1_AddUkSavingsAccountValidator(nino, body)

  "AddUkSavingsAccountValidator" when {
    "running a validation" should {
      "return no errors" when {
        "a valid request is supplied" in {
          val result: Either[ErrorWrapper, AddUkSavingsAccountRequestData] = validator(validNino, validRequestBodyJson).validateAndWrapResult()
          result shouldBe Right(Def1_AddUkSavingsAccountRequestData(parsedNino, parsedRequestBody))
        }
      }

      "return NinoFormatError" when {
        "an invalid nino is supplied" in {
          val result: Either[ErrorWrapper, AddUkSavingsAccountRequestData] =
            validator("invalid", validRequestBodyJson).validateAndWrapResult()

          result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
        }
      }

      "return RuleIncorrectOrEmptyBodyError" when {
        "an empty JSON body is submitted" in {
          val result: Either[ErrorWrapper, AddUkSavingsAccountRequestData] =
            validator(validNino, JsObject.empty).validateAndWrapResult()

          result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError))
        }

        "a non-empty JSON body is submitted without the mandatory account name field" in {
          val result: Either[ErrorWrapper, AddUkSavingsAccountRequestData] =
            validator(validNino, nonsenseRequestBodyJson).validateAndWrapResult()

          result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/accountName")))

        }

        "account name is supplied with the wrong data type" in {
          val result: Either[ErrorWrapper, AddUkSavingsAccountRequestData] =
            validator(validNino, invalidFieldTypeRequestBodyJson).validateAndWrapResult()

          result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/accountName")))
        }
      }

      "return AccountNameFormatError" when {
        "an invalid account name is supplied" in {
          val result: Either[ErrorWrapper, AddUkSavingsAccountRequestData] =
            validator(validNino, invalidValueRequestBodyJson).validateAndWrapResult()

          result shouldBe Left(ErrorWrapper(correlationId, AccountNameFormatError.withPath("/accountName")))

        }
      }
    }
  }

}
