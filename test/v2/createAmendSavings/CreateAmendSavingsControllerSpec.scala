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

package v2.createAmendSavings

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import play.api.Configuration
import shared.config.MockSharedAppConfig
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import shared.models.auth.UserDetails
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import shared.routing.{Version, Version2}
import shared.services.MockMtdIdLookupService
import v2.createAmendSavings.def1.model.request.{AmendForeignInterestItem, AmendSecurities, Def1_CreateAmendSavingsRequestBody, Def1_CreateAmendSavingsRequestData}
import v2.createAmendSavings.model.request.CreateAmendSavingsRequestData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateAmendSavingsControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockCreateAmendSavingsService
    with MockMtdIdLookupService
    with MockCreateAmendSavingsValidatorFactory
    with MockSharedAppConfig {

  override val apiVersion: Version = Version2

  private val taxYear       = "2019-20"
  private val mtdId: String = "test-mtd-id"

  private val requestBodyJson: JsValue = Json.parse(
    """
      |{
      |  "securities":
      |      {
      |        "taxTakenOff": 100.11,
      |        "grossAmount": 100.22,
      |        "netAmount": 100.33
      |      },
      |  "foreignInterest":   [
      |     {
      |        "amountBeforeTax": 101.11,
      |        "countryCode": "FRA",
      |        "taxTakenOff": 102.22,
      |        "specialWithholdingTax": 103.33,
      |        "taxableAmount": 104.44,
      |        "foreignTaxCreditRelief": true
      |      },
      |      {
      |        "amountBeforeTax": 201.11,
      |        "countryCode": "DEU",
      |        "taxTakenOff": 202.22,
      |        "specialWithholdingTax": 203.33,
      |        "taxableAmount": 204.44,
      |        "foreignTaxCreditRelief": true
      |      }
      |   ]
      |}
    """.stripMargin
  )

  private val security: AmendSecurities = AmendSecurities(
    taxTakenOff = Some(100.11),
    grossAmount = 200.22,
    netAmount = Some(300.33)
  )

  private val foreignInterests: List[AmendForeignInterestItem] = List(
    AmendForeignInterestItem(
      amountBeforeTax = Some(101.11),
      countryCode = "FRA",
      taxTakenOff = Some(102.22),
      specialWithholdingTax = Some(103.33),
      taxableAmount = 104.44,
      foreignTaxCreditRelief = Some(false)
    ),
    AmendForeignInterestItem(
      amountBeforeTax = Some(201.11),
      countryCode = "DEU",
      taxTakenOff = Some(202.22),
      specialWithholdingTax = Some(203.33),
      taxableAmount = 204.44,
      foreignTaxCreditRelief = Some(false)
    )
  )

  private val amendSavingsRequestBody: Def1_CreateAmendSavingsRequestBody = Def1_CreateAmendSavingsRequestBody(
    securities = Some(security),
    foreignInterest = Some(foreignInterests)
  )

  private val requestData: CreateAmendSavingsRequestData = Def1_CreateAmendSavingsRequestData(
    nino = Nino(validNino),
    taxYear = TaxYear.fromMtd(taxYear),
    body = amendSavingsRequestBody
  )

  "CreateAmendSavingsController" should {
    "return OK" when {
      "the request received is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockCreateAmendSavingsService
          .createAmendSaving(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        runOkTest(expectedStatus = OK)
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))
        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockCreateAmendSavingsService
          .createAmendSaving(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTest(RuleTaxYearNotSupportedError)
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] {

    val controller = new CreateAmendSavingsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockCreateAmendSavingsValidatorFactory,
      service = mockCreateAmendSavingsService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )(appConfig = mockSharedAppConfig, ec = global)

    MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedSharedAppConfig
      .endpointAllowsSupportingAgents(controller.endpointName)
      .anyNumberOfTimes() returns true

    protected def callController(): Future[Result] = controller.createAmendSavings(validNino, taxYear)(fakePostRequest(requestBodyJson))

    def event(auditResponse: AuditResponse, requestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "CreateAmendSavingsIncome",
        transactionName = "create-amend-savings-income",
        detail = GenericAuditDetail(
          UserDetails(mtdId, "Individual", None),
          "2.0",
          Map("nino" -> validNino, "taxYear" -> taxYear),
          requestBody,
          correlationId,
          auditResponse
        )
      )

  }

}
