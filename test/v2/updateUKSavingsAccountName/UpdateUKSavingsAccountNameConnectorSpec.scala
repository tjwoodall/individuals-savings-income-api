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

package v2.updateUKSavingsAccountName

import shared.connectors.{ConnectorSpec, DownstreamOutcome}
import models.domain.SavingsAccountId
import shared.models.domain.Nino
import shared.models.errors.{DownstreamErrorCode, DownstreamErrors}
import shared.models.outcomes.ResponseWrapper
import v2.updateUKSavingsAccountName.fixture.UpdateUKSavingsAccountNameFixtures.requestBodyModel
import v2.updateUKSavingsAccountName.model.request.UpdateUKSavingsAccountNameRequest

import scala.concurrent.Future

class UpdateUKSavingsAccountNameConnectorSpec extends ConnectorSpec {

  private val nino: Nino                 = Nino("AA123456A")
  val savingsAccountId: SavingsAccountId = SavingsAccountId("SAVKB2UVwUTBQGJ")

  "UpdateUKSavingsAccountNameConnector" should {
    "return a 204 (NO_CONTENT) status for a success scenario" when {
      "the downstream call is successful" in new HipTest with Test {
        val outcome: Right[Nothing, ResponseWrapper[Unit]] = Right(ResponseWrapper(correlationId, ()))

        willPut(
          url = s"$baseUrl/itsd/income-sources/$nino/non-business/$savingsAccountId",
          body = requestBodyModel
        ).returns(Future.successful(outcome))

        val result: DownstreamOutcome[Unit] = await(connector.update(request))

        result shouldBe outcome
      }
    }

    "return an error" when {
      "downstream returns an error" in new HipTest with Test {
        val downstreamErrorResponse: DownstreamErrors = DownstreamErrors.single(DownstreamErrorCode("SOME_ERROR"))
        val errorOutcome: Left[ResponseWrapper[DownstreamErrors], Nothing] =
          Left(ResponseWrapper(correlationId, downstreamErrorResponse))

        willPut(
          url = s"$baseUrl/itsd/income-sources/$nino/non-business/$savingsAccountId",
          body = requestBodyModel
        ).returns(Future.successful(errorOutcome))

        val result: DownstreamOutcome[Unit] = await(connector.update(request))

        result shouldBe errorOutcome
      }
    }
  }

  private trait Test { _: ConnectorTest =>

    protected val connector: UpdateUKSavingsAccountNameConnector =
      new UpdateUKSavingsAccountNameConnector(http = mockHttpClient, appConfig = mockSharedAppConfig)

    protected val request: UpdateUKSavingsAccountNameRequest = UpdateUKSavingsAccountNameRequest(
      nino = nino,
      savingsAccountId = savingsAccountId,
      body = requestBodyModel
    )
  }

}
