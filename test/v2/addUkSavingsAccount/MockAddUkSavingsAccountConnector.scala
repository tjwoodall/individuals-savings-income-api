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

package v2.addUkSavingsAccount

import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import shared.connectors.DownstreamOutcome
import uk.gov.hmrc.http.HeaderCarrier
import v2.addUkSavingsAccount.model.request.AddUkSavingsAccountRequestData
import v2.addUkSavingsAccount.model.response.AddUkSavingsAccountResponse

import scala.concurrent.{ExecutionContext, Future}

trait MockAddUkSavingsAccountConnector extends TestSuite with MockFactory {

  val mockAddUkSavingsAccountConnector: AddUkSavingsAccountConnector = mock[AddUkSavingsAccountConnector]

  object MockAddUkSavingsAccountConnector {

    def addSavings(requestData: AddUkSavingsAccountRequestData): CallHandler[Future[DownstreamOutcome[AddUkSavingsAccountResponse]]] = {
      (mockAddUkSavingsAccountConnector
        .addSavings(_: AddUkSavingsAccountRequestData)(_: HeaderCarrier, _: ExecutionContext, _: String))
        .expects(requestData, *, *, *)
    }

  }

}
