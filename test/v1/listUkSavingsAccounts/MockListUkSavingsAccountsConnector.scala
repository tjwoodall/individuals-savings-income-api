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

package v1.listUkSavingsAccounts

import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import shared.connectors.DownstreamOutcome
import uk.gov.hmrc.http.HeaderCarrier
import v1.listUkSavingsAccounts.model.request.ListUkSavingsAccountsRequestData
import v1.listUkSavingsAccounts.model.response.ListUkSavingsAccountsResponse

import scala.concurrent.{ExecutionContext, Future}

trait MockListUkSavingsAccountsConnector extends TestSuite with MockFactory {

  val mockListUkSavingsAccountsConnector: ListUkSavingsAccountsConnector = mock[ListUkSavingsAccountsConnector]

  object MockListUkSavingsAccountsConnector {

    def listUkSavingsAccounts(
        requestData: ListUkSavingsAccountsRequestData): CallHandler[Future[DownstreamOutcome[ListUkSavingsAccountsResponse]]] = {
      (mockListUkSavingsAccountsConnector
        .listUkSavingsAccounts(_: ListUkSavingsAccountsRequestData)(_: HeaderCarrier, _: ExecutionContext, _: String))
        .expects(requestData, *, *, *)
    }

  }

}
