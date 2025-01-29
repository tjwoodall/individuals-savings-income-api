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

package v2.createAmendSavings.def1

import cats.data.Validated
import cats.data.Validated.Valid
import cats.implicits.toFoldableOps
import shared.controllers.validators.RulesValidator
import shared.controllers.validators.resolvers.{ResolveParsedCountryCode, ResolveParsedNumber}
import shared.models.errors.MtdError
import v2.createAmendSavings.def1.model.request.{
  AmendForeignInterestItem,
  AmendSecurities,
  Def1_CreateAmendSavingsRequestBody,
  Def1_CreateAmendSavingsRequestData
}

object Def1_CreateAmendSavingsRulesValidator extends RulesValidator[Def1_CreateAmendSavingsRequestData] {
  private val resolveParsedNumber = ResolveParsedNumber()

  def validateBusinessRules(parsed: Def1_CreateAmendSavingsRequestData): Validated[Seq[MtdError], Def1_CreateAmendSavingsRequestData] = {
    import parsed._

    combine(
      validateSecuritiesSequence(body),
      validateForeignSequence(body)
    ).onSuccess(parsed)
  }

  private def validateSecuritiesSequence(requestBody: Def1_CreateAmendSavingsRequestBody): Validated[Seq[MtdError], Unit] = {
    requestBody.securities.fold[Validated[Seq[MtdError], Unit]](Valid(())) { securities =>
      validateSecurity(securities)
    }
  }

  private def validateForeignSequence(requestBody: Def1_CreateAmendSavingsRequestBody): Validated[Seq[MtdError], Unit] = {
    requestBody.foreignInterest.fold[Validated[Seq[MtdError], Unit]](Valid(())) { foreignInterest =>
      foreignInterest.zipWithIndex.traverse_ { case (foreignInterest, index) =>
        validateForeignInterests(foreignInterest, index)
      }
    }
  }

  private def validateSecurity(security: AmendSecurities): Validated[Seq[MtdError], Unit] = {
    import security._

    val validatedMandatoryDecimalNumbers = List(
      (grossAmount, s"/securities/grossAmount")
    ).traverse_ { case (value, path) =>
      resolveParsedNumber(value, path)
    }

    val validatedOptionalDecimalNumbers = List(
      (taxTakenOff, s"/securities/taxTakenOff"),
      (netAmount, s"/securities/netAmount")
    ).traverse_ { case (value, path) =>
      resolveParsedNumber(value, path)
    }

    combine(
      validatedMandatoryDecimalNumbers,
      validatedOptionalDecimalNumbers
    )
  }

  private def validateForeignInterests(foreignInterest: AmendForeignInterestItem, index: Int): Validated[Seq[MtdError], Unit] = {
    import foreignInterest._

    val validatedMandatoryDecimalNumbers = List(
      (taxableAmount, s"/foreignInterest/$index/taxableAmount")
    ).traverse_ { case (value, path) =>
      resolveParsedNumber(value, path)
    }

    val validatedOptionalDecimalNumbers = List(
      (amountBeforeTax, s"/foreignInterest/$index/amountBeforeTax"),
      (taxTakenOff, s"/foreignInterest/$index/taxTakenOff"),
      (specialWithholdingTax, s"/foreignInterest/$index/specialWithholdingTax")
    ).traverse_ { case (value, path) =>
      resolveParsedNumber(value, path)
    }

    val validatedCountryCode = List(
      (countryCode, s"/foreignInterest/$index/countryCode")
    ).traverse_ { case (value, path) =>
      ResolveParsedCountryCode(value, path)
    }

    combine(
      validatedCountryCode,
      validatedMandatoryDecimalNumbers,
      validatedOptionalDecimalNumbers
    )
  }

}
