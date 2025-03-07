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

package v2.retrieveUkSavingsAccountAnnualSummary

import cats.data.EitherT
import models.errors.SavingsAccountIdFormatError
import shared.controllers.{EndpointLogContext, RequestContext}
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import shared.services.{BaseService, ServiceOutcome}
import v2.retrieveUkSavingsAccountAnnualSummary.model.request.RetrieveUkSavingsAccountAnnualSummaryRequestData
import v2.retrieveUkSavingsAccountAnnualSummary.model.response.RetrieveUkSavingsAccountAnnualSummaryResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveUkSavingsAccountAnnualSummaryService @Inject() (connector: RetrieveUkSavingsAccountAnnualSummaryConnector) extends BaseService {

  def retrieveUkSavingsAccountAnnualSummary(request: RetrieveUkSavingsAccountAnnualSummaryRequestData)(implicit
      ctx: RequestContext,
      ec: ExecutionContext): Future[ServiceOutcome[RetrieveUkSavingsAccountAnnualSummaryResponse]] = {

    val result = for {
      downstreamResponseWrapper <- EitherT(connector.retrieveUkSavingsAccountAnnualSummary(request)).leftMap(mapDownstreamErrors(downstreamErrorMap))
      mtdResponseWrapper        <- EitherT.fromEither[Future](validateDownstreamResponse(downstreamResponseWrapper))
    } yield mtdResponseWrapper

    result.value
  }

  def validateDownstreamResponse(
      downstreamResponseWrapper: ResponseWrapper[RetrieveUkSavingsAccountAnnualSummaryResponse]
  )(implicit logContext: EndpointLogContext): ServiceOutcome[RetrieveUkSavingsAccountAnnualSummaryResponse] = {
    import downstreamResponseWrapper._

    responseData.savingsInterestAnnualIncome match {
      case Nil =>
        Left(ErrorWrapper(correlationId, NotFoundError, None))
      case _ +: Nil =>
        Right(ResponseWrapper(correlationId, responseData))
      case _ =>
        logger.info(s"[${logContext.controllerName}] [${logContext.endpointName}] - More than one matching account found")
        Left(ErrorWrapper(correlationId, InternalError, None))
    }
  }

  private val downstreamErrorMap: Map[String, MtdError] = {
    val errors = Map(
      "INVALID_NINO"            -> NinoFormatError,
      "INVALID_TYPE"            -> InternalError,
      "INVALID_TAXYEAR"         -> TaxYearFormatError, // remove once DES to IFS migration complete
      "INVALID_INCOME_SOURCE"   -> SavingsAccountIdFormatError,
      "NOT_FOUND_PERIOD"        -> NotFoundError,
      "NOT_FOUND_INCOME_SOURCE" -> NotFoundError,
      "SERVER_ERROR"            -> InternalError,
      "SERVICE_UNAVAILABLE"     -> InternalError
    )

    val extraTysErrors = Map(
      "INVALID_TAX_YEAR"             -> TaxYearFormatError,
      "INVALID_INCOMESOURCE_ID"      -> SavingsAccountIdFormatError,
      "INVALID_INCOMESOURCE_TYPE"    -> InternalError,
      "SUBMISSION_PERIOD_NOT_FOUND"  -> NotFoundError,
      "INCOME_DATA_SOURCE_NOT_FOUND" -> NotFoundError,
      "TAX_YEAR_NOT_SUPPORTED"       -> RuleTaxYearNotSupportedError
    )

    errors ++ extraTysErrors
  }

}
