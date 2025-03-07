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

package v2.deleteSavings

import shared.config.SharedAppConfig
import shared.controllers.validators.Validator
import config.SavingsConfig
import v2.deleteSavings.def1.Def1_DeleteSavingsValidator
import v2.deleteSavings.model.request.DeleteSavingsRequestData

import javax.inject._

class DeleteSavingsValidatorFactory @Inject() (appConfig: SharedAppConfig, savingsConfig: SavingsConfig) {

  def validator(nino: String, taxYear: String): Validator[DeleteSavingsRequestData] =
    new Def1_DeleteSavingsValidator(nino, taxYear)(appConfig, savingsConfig)

}
