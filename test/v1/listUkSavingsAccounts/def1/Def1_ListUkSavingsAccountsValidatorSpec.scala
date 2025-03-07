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

package v1.listUkSavingsAccounts.def1

import models.domain.SavingsAccountId
import models.errors.SavingsAccountIdFormatError
import shared.models.domain.Nino
import shared.models.errors.{BadRequestError, ErrorWrapper, NinoFormatError}
import shared.utils.UnitSpec
import v1.listUkSavingsAccounts.def1.model.request.Def1_ListUkSavingsAccountsRequestData

class Def1_ListUkSavingsAccountsValidatorSpec extends UnitSpec {

  private implicit val correlationId: String = "1234"

  private val validNino             = "AA123456A"
  private val validSavingsAccountId = SavingsAccountId("SAVKB2UVwUTBQGJ")
  private val parsedNino            = Nino(validNino)

  private def validator(nino: String, savingsAccountId: Option[String]) = new Def1_ListUkSavingsAccountsValidator(nino, savingsAccountId)

  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied" in {
        val result = validator(validNino, Some(validSavingsAccountId.toString)).validateAndWrapResult()
        result shouldBe Right(Def1_ListUkSavingsAccountsRequestData(parsedNino, Some(validSavingsAccountId)))
      }
    }

    "return NinoFormatError error" when {
      "an invalid nino is supplied" in {
        val result = validator("A12344A", Some(validSavingsAccountId.toString)).validateAndWrapResult()
        result shouldBe Left(
          ErrorWrapper(correlationId, NinoFormatError)
        )
      }
    }

    "return SavingsAccountIdFormatError error" when {
      "an invalid tax year is supplied" in {
        val result = validator(validNino, Some("invalid")).validateAndWrapResult()
        result shouldBe Left(
          ErrorWrapper(correlationId, SavingsAccountIdFormatError)
        )
      }
    }

    "return multiple errors" when {
      "request supplied has multiple errors" in {
        val result = validator("not-a-nino", Some("invalid")).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            BadRequestError,
            Some(List(NinoFormatError, SavingsAccountIdFormatError))
          )
        )
      }
    }
  }

}
