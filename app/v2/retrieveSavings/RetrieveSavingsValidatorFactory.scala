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

package v2.retrieveSavings

import shared.config.SharedAppConfig
import shared.controllers.validators.Validator
import v2.retrieveSavings.def1.Def1_RetrieveSavingsValidator
import v2.retrieveSavings.model.request.RetrieveSavingsRequestData

import javax.inject.{Inject, Singleton}

@Singleton
class RetrieveSavingsValidatorFactory @Inject() (appConfig: SharedAppConfig) {

  def validator(nino: String, taxYear: String): Validator[RetrieveSavingsRequestData] =
    new Def1_RetrieveSavingsValidator(nino, taxYear)(appConfig)

}
