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

package api.models.errors

import play.api.http.Status._

object NinoFormatError            extends MtdError("FORMAT_NINO", "The provided NINO is invalid", BAD_REQUEST)
object TaxYearFormatError         extends MtdError("FORMAT_TAX_YEAR", "The provided tax year is invalid", BAD_REQUEST)
object EmploymentIdFormatError    extends MtdError("FORMAT_EMPLOYMENT_ID", "The provided employment ID is invalid", BAD_REQUEST)

object CountryCodeFormatError     extends MtdError("FORMAT_COUNTRY_CODE", "The format of the country code is invalid", BAD_REQUEST)
object CountryCodeRuleError   extends MtdError("RULE_COUNTRY_CODE", "The country code is not a valid ISO 3166-1 alpha-3 country code", BAD_REQUEST)
object DateFormatError         extends MtdError("FORMAT_DATE", "The field should be in the format YYYY-MM-DD", BAD_REQUEST)
object ValueFormatError       extends MtdError("FORMAT_VALUE", "The value must be between 0.00 and 99999999999.99", BAD_REQUEST)
object AccountNameFormatError extends MtdError("FORMAT_ACCOUNT_NAME", "The provided account name is invalid", BAD_REQUEST)

object SavingsAccountIdFormatError
    extends MtdError("FORMAT_SAVINGS_ACCOUNT_ID", "The format of the supplied savings account ID is not valid", BAD_REQUEST)

// Rule Errors

object RuleTaxYearNotSupportedError
    extends MtdError("RULE_TAX_YEAR_NOT_SUPPORTED", "The tax year specified does not lie within the supported range", BAD_REQUEST)

object RuleIncorrectOrEmptyBodyError
    extends MtdError("RULE_INCORRECT_OR_EMPTY_BODY_SUBMITTED", "An empty or non-matching body was submitted", BAD_REQUEST)

object RuleTaxYearRangeInvalidError
    extends MtdError("RULE_TAX_YEAR_RANGE_INVALID", "Tax year range invalid. A tax year range of one year is required", BAD_REQUEST)

object RuleMaximumSavingsAccountsLimitError
    extends MtdError("RULE_MAXIMUM_SAVINGS_ACCOUNTS_LIMIT", "The 1000 savings account limit exceeded", BAD_REQUEST)

object RuleDuplicateAccountNameError extends MtdError("RULE_DUPLICATE_ACCOUNT_NAME", "Duplicate account name given for supplied NINO", BAD_REQUEST)

object RuleRequestCannotBeFulfilledError extends MtdError("RULE_REQUEST_CANNOT_BE_FULFILLED", "Custom (will vary in production depending on the actual error)", 422)

//Stub errors
object RuleIncorrectGovTestScenarioError extends MtdError("RULE_INCORRECT_GOV_TEST_SCENARIO", "The Gov-Test-Scenario was not found", BAD_REQUEST)

// Standard Errors
object NotFoundError           extends MtdError("MATCHING_RESOURCE_NOT_FOUND", "Matching resource not found", NOT_FOUND)
object InternalError           extends MtdError("INTERNAL_SERVER_ERROR", "An internal server error occurred", INTERNAL_SERVER_ERROR)
object BadRequestError         extends MtdError("INVALID_REQUEST", "Invalid request", BAD_REQUEST)
object BVRError                extends MtdError("BUSINESS_ERROR", "Business validation error", BAD_REQUEST)
// Authorisation Errors
object ClientNotAuthenticatedError extends MtdError("CLIENT_OR_AGENT_NOT_AUTHORISED", "The client and/or agent is not authorised", UNAUTHORIZED)
object ClientNotAuthorisedError    extends MtdError("CLIENT_OR_AGENT_NOT_AUTHORISED", "The client and/or agent is not authorised", FORBIDDEN)
object InvalidBearerTokenError     extends MtdError("UNAUTHORIZED", "Bearer token is missing or not authorized", UNAUTHORIZED)

// Accept header Errors
object InvalidAcceptHeaderError extends MtdError("ACCEPT_HEADER_INVALID", "The accept header is missing or invalid", NOT_ACCEPTABLE)
object UnsupportedVersionError  extends MtdError("NOT_FOUND", "The requested resource could not be found", NOT_FOUND)
object InvalidBodyTypeError     extends MtdError("INVALID_BODY_TYPE", "Expecting text/json or application/json body", UNSUPPORTED_MEDIA_TYPE)
