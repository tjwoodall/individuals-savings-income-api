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

package v1.addUkSavingsAccount

import models.errors.{RuleDuplicateAccountNameError, RuleMaximumSavingsAccountsLimitError}
import shared.controllers.EndpointLogContext
import shared.models.domain.Nino
import shared.models.errors.*
import shared.models.outcomes.ResponseWrapper
import shared.services.ServiceSpec
import v1.addUkSavingsAccount.def1.model.request.{Def1_AddUkSavingsAccountRequestBody, Def1_AddUkSavingsAccountRequestData}
import v1.addUkSavingsAccount.def1.model.response.Def1_AddUkSavingsAccountResponse

import scala.concurrent.Future

class AddUkSavingsAccountServiceSpec extends ServiceSpec {

  private val nino = "AA112233A"

  val addUkSavingsAccountRequestBody: Def1_AddUkSavingsAccountRequestBody =
    Def1_AddUkSavingsAccountRequestBody(accountName = "Shares savings account")

  val addUkSavingsAccountRequest: Def1_AddUkSavingsAccountRequestData = Def1_AddUkSavingsAccountRequestData(
    nino = Nino(nino),
    body = addUkSavingsAccountRequestBody
  )

  val addUkSavingsAccountResponse: Def1_AddUkSavingsAccountResponse = Def1_AddUkSavingsAccountResponse(
    savingsAccountId = "SAVKB2UVwUTBQGJ"
  )

  trait Test extends MockAddUkSavingsAccountConnector {
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service: AddUkSavingsAccountService = new AddUkSavingsAccountService(
      connector = mockAddUkSavingsAccountConnector
    )

  }

  "AddUkSavingsAccountService" when {
    "addSavings" should {
      "return a 200 status for a success scenario" when {
        "valid request is supplied" in new Test {
          private val outcome = Right(ResponseWrapper(correlationId, addUkSavingsAccountResponse))

          MockAddUkSavingsAccountConnector
            .addSavings(addUkSavingsAccountRequest)
            .returns(Future.successful(outcome))

          await(service.addSavings(addUkSavingsAccountRequest)) shouldBe outcome
        }
      }

      "map errors according to spec" when {
        def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
          s"a code $downstreamErrorCode error is returned from the service" in new Test {

            MockAddUkSavingsAccountConnector
              .addSavings(addUkSavingsAccountRequest)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

            await(service.addSavings(addUkSavingsAccountRequest)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val desErrors = List(
          ("INVALID_IDVALUE", NinoFormatError),
          ("MAX_ACCOUNTS_REACHED", RuleMaximumSavingsAccountsLimitError),
          ("ALREADY_EXISTS", RuleDuplicateAccountNameError),
          ("INVALID_IDTYPE", InternalError),
          ("INVALID_PAYLOAD", InternalError),
          ("SERVER_ERROR", InternalError),
          ("SERVICE_UNAVAILABLE", InternalError)
        )

        val hipErrors = List(
          ("1215", NinoFormatError),
          ("1011", RuleMaximumSavingsAccountsLimitError),
          ("1214", RuleDuplicateAccountNameError),
          ("1000", InternalError)
        )

        (desErrors ++ hipErrors).foreach(args => serviceError.tupled(args))
      }
    }
  }

}
