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

import play.api.http.Status.BAD_REQUEST

import shared.models.errors.MtdError

object AccountNameFormatErrorNew extends MtdError("FORMAT_ACCOUNT_NAME", "The provided account name is invalid", BAD_REQUEST)

object SavingsAccountIdFormatErrorNew
    extends MtdError("FORMAT_SAVINGS_ACCOUNT_ID", "The format of the supplied savings account ID is not valid", BAD_REQUEST)

object DateFormatErrorNew extends MtdError("FORMAT_DATE", "The field should be in the format YYYY-MM-DD", BAD_REQUEST)
