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

package config

import org.scalamock.handlers.{CallHandler, CallHandler0}
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import play.api.Configuration

trait MockSavingsConfig extends TestSuite with MockFactory {

  implicit val mockSavingsConfig: SavingsConfig = mock[SavingsConfig]

  implicit val mockSavingsFeatureSwitches: SavingsFeatureSwitches = mock[SavingsFeatureSwitches]

  object MockedSavingsConfig {

    def isDesIf_MigrationEnabled: CallHandler[Boolean] =
      (() => mockSavingsFeatureSwitches.isDesIf_MigrationEnabled).expects()

    def featureSwitchConfig: CallHandler0[Configuration]      = (() => mockSavingsConfig.featureSwitchConfig: Configuration).expects()
    def featureSwitches: CallHandler0[SavingsFeatureSwitches] = (() => mockSavingsConfig.featureSwitches: SavingsFeatureSwitches).expects()

    def minimumPermittedTaxYear: CallHandler[Int] = (() => mockSavingsConfig.minimumPermittedTaxYear).expects()

    def ukSavingsAccountAnnualSummaryMinimumTaxYear: CallHandler[Int] =
      (() => mockSavingsConfig.ukSavingsAccountAnnualSummaryMinimumTaxYear).expects()

  }

}
