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

package v1.listUkSavingsAccounts

import models.domain.SavingsAccountId
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import shared.config.MockAppConfig
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.models.domain.Nino
import shared.models.errors.{ErrorWrapper, NinoFormatError, NotFoundError}
import shared.models.outcomes.ResponseWrapper
import v1.listUkSavingsAccounts.def1.model.request.Def1_ListUkSavingsAccountsRequestData
import v1.listUkSavingsAccounts.def1.model.response.{Def1_ListUkSavingsAccountsResponse, Def1_UkSavingsAccount}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ListUkSavingsAccountsControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockListUkSavingsAccountsService
    with MockListUkSavingsAccountsValidatorFactory
    with MockAppConfig {

  val nino: String                       = "AA123456A"
  val savingsAccountId: SavingsAccountId = SavingsAccountId("SAVKB2UVwUTBQGJ")

  val requestData: Def1_ListUkSavingsAccountsRequestData = Def1_ListUkSavingsAccountsRequestData(
    nino = Nino(nino),
    savingsAccountId = Some(savingsAccountId)
  )

  val validListUkSavingsAccountResponse: Def1_ListUkSavingsAccountsResponse[Def1_UkSavingsAccount] = Def1_ListUkSavingsAccountsResponse(
    Some(
      Seq(
        Def1_UkSavingsAccount("000000000000001", "Bank Account 1"),
        Def1_UkSavingsAccount("000000000000002", "Bank Account 2"),
        Def1_UkSavingsAccount("000000000000003", "Bank Account 3")
      )
    )
  )

  private val mtdResponse: JsValue = Json.parse(s"""|{
      | "savingsAccounts":
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
      |        "savingsAccountId": "000000000000003",
      |        "accountName": "Bank Account 3"
      |    }
      | ]
      |}""".stripMargin)

  "listUkSavingsAccounts" should {
    "return OK" when {
      "happy path" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockListUkSavingsAccountsService
          .listUkSavingsAccounts(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, validListUkSavingsAccountResponse))))

        runOkTest(
          expectedStatus = OK,
          maybeExpectedResponseBody = Some(mtdResponse)
        )
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returningErrors(Seq(ErrorWrapper(correlationId, NinoFormatError).error)))

        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockListUkSavingsAccountsService
          .listUkSavingsAccounts(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, NotFoundError))))

        runErrorTest(NotFoundError)
      }
    }
  }

  trait Test extends ControllerTest {

    val controller = new ListUkSavingsAccountsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockListUkSavingsAccountValidator,
      service = mockListUkSavingsAccountsService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.listUkSavingsAccounts(nino, Some(savingsAccountId.toString))(fakeGetRequest)
  }

}
