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

package v1.retrieveSavings

import play.api.mvc.Result
import play.api.Configuration
import shared.config.MockSharedAppConfig
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.models.domain.{Nino, TaxYear, Timestamp}
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import v1.retrieveSavings.def1.model.RetrieveSavingsControllerFixture
import v1.retrieveSavings.def1.model.request.Def1_RetrieveSavingsRequestData
import v1.retrieveSavings.def1.model.response.{Def1_RetrieveSavingsResponse, ForeignInterestItem, Securities}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveSavingsControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockRetrieveSavingsService
    with MockRetrieveSavingsValidatorFactory
    with MockSharedAppConfig {

  val nino: String    = "AA123456A"
  private val taxYear = "2019-20"

  private val requestData: Def1_RetrieveSavingsRequestData = Def1_RetrieveSavingsRequestData(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(taxYear)
  )

  private val fullSecuritiesItemsModel = Securities(
    taxTakenOff = Some(100.0),
    grossAmount = 1455.0,
    netAmount = Some(123.22)
  )

  private val fullForeignInterestsModel = ForeignInterestItem(
    amountBeforeTax = Some(1232.22),
    countryCode = "DEU",
    taxTakenOff = Some(22.22),
    specialWithholdingTax = Some(22.22),
    taxableAmount = 2321.22,
    foreignTaxCreditRelief = Some(true)
  )

  private val retrieveSavingsResponseModel = Def1_RetrieveSavingsResponse(
    submittedOn = Timestamp("2019-04-04T01:01:01.000Z"),
    securities = Some(fullSecuritiesItemsModel),
    foreignInterest = Some(Seq(fullForeignInterestsModel))
  )

  private val mtdResponse = RetrieveSavingsControllerFixture.mtdRetrieveSavingsResponse

  "RetrieveSavingsController" should {
    "return a successful response with status 200 (OK)" when {
      "given a valid request" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrieveSavingsService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, retrieveSavingsResponseModel))))

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

        MockRetrieveSavingsService
          .retrieve(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTest(RuleTaxYearNotSupportedError)

      }
    }
  }

  trait Test extends ControllerTest {

    val controller = new RetrieveSavingsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockRetrieveSavingsValidator,
      service = mockRetrieveSavingsService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedSharedAppConfig
      .endpointAllowsSupportingAgents(controller.endpointName)
      .anyNumberOfTimes() returns true

    protected def callController(): Future[Result] = controller.retrieveSaving(nino, taxYear)(fakeGetRequest)
  }

}
