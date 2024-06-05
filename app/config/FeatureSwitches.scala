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

package config

import com.google.inject.ImplementedBy
import play.api.Configuration

import javax.inject.{Inject, Singleton}

@ImplementedBy(classOf[FeatureSwitchesImpl])
trait FeatureSwitches {

  protected val featureSwitchConfig: Configuration

  def isVersionEnabled(version: String): Boolean
  def isPassDeleteIntentEnabled: Boolean

  def isDesIf_MigrationEnabled: Boolean

  def isEnabled(feature: String): Boolean = isConfigTrue(feature + ".enabled")

  private def isConfigTrue(key: String): Boolean = featureSwitchConfig.getOptional[Boolean](key).getOrElse(true)
}

@Singleton
class FeatureSwitchesImpl(protected val featureSwitchConfig: Configuration) extends FeatureSwitches {

  @Inject
  def this(appConfig: AppConfig) = this(appConfig.featureSwitches)

  private val versionRegex = """(\d)\.\d""".r

  def isVersionEnabled(version: String): Boolean = {
    val maybeVersion: Option[String] =
      version match {
        case versionRegex(v) => Some(v)
        case _               => None
      }

    val enabled = for {
      validVersion <- maybeVersion
      enabled      <- featureSwitchConfig.getOptional[Boolean](s"version-$validVersion.enabled")
    } yield enabled

    enabled.getOrElse(false)
  }

  val isPassDeleteIntentEnabled: Boolean = isEnabled("passDeleteIntentHeader")
  val isDesIf_MigrationEnabled: Boolean = isEnabled("desIf_Migration")

}

object FeatureSwitches {
  def apply(configuration: Configuration): FeatureSwitches = new FeatureSwitchesImpl(configuration)
}
