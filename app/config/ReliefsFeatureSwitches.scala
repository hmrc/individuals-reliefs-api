/*
 * Copyright 2024 HM Revenue & Customs
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

import play.api.Configuration
import shared.config.{FeatureSwitches, SharedAppConfig}

case class ReliefsFeatureSwitches(protected val featureSwitchConfig: Configuration) extends FeatureSwitches {

  val isDesHipMigration1656Enabled: Boolean = isEnabled("des_hip_migration_1656")
  val isPassDeleteIntentEnabled: Boolean    = isEnabled("isPassDeleteIntentEnabled")

}

object ReliefsFeatureSwitches {
  def apply()(implicit appConfig: SharedAppConfig): ReliefsFeatureSwitches = ReliefsFeatureSwitches(appConfig.featureSwitchConfig)
}
