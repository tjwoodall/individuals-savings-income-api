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

package models.errors

import play.api.http.Status.BAD_REQUEST

import shared.models.errors.MtdError

object AccountNameFormatError extends MtdError("FORMAT_ACCOUNT_NAME", "The provided account name is invalid", BAD_REQUEST)

object SavingsAccountIdFormatError
    extends MtdError("FORMAT_SAVINGS_ACCOUNT_ID", "The format of the supplied savings account ID is not valid", BAD_REQUEST)

object RuleMaximumSavingsAccountsLimitError
  extends MtdError("RULE_MAXIMUM_SAVINGS_ACCOUNTS_LIMIT", "The 1000 savings account limit exceeded", BAD_REQUEST)

object RuleDuplicateAccountNameError
  extends MtdError("RULE_DUPLICATE_ACCOUNT_NAME", "Duplicate account name given for supplied NINO", BAD_REQUEST)

object RuleOutsideAmendmentWindowError extends MtdError("RULE_OUTSIDE_AMENDMENT_WINDOW", "You are outside the amendment window", BAD_REQUEST)
