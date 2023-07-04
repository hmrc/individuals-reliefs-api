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

  def isVersionEnabled(version: String): Boolean

  def isCharitableGivingRoutingEnabled: Boolean
  def isTaxYearSpecificApiEnabled: Boolean
  def isPassDeleteIntentEnabled: Boolean
}

@Singleton
class FeatureSwitchesImpl(featureSwitchConfig: Configuration) extends FeatureSwitches {

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

  val isCharitableGivingRoutingEnabled: Boolean = isEnabled("charitable-giving-endpoints.enabled")
  val isTaxYearSpecificApiEnabled: Boolean      = isEnabled("tys-api.enabled")
  val isPassDeleteIntentEnabled: Boolean        = isEnabled("passDeleteIntentHeader.enabled")

  private def isEnabled(key: String): Boolean = featureSwitchConfig.getOptional[Boolean](key).getOrElse(true)
}

object FeatureSwitches {
  def apply(configuration: Configuration): FeatureSwitches = new FeatureSwitchesImpl(configuration)
}
