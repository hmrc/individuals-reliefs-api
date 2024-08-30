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

import com.typesafe.config.Config
import play.api.{ConfigLoader, Configuration}
import routing.Version
import uk.gov.hmrc.auth.core.ConfidenceLevel
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}

trait AppConfig {
  // API name
  def appName: String

  // MTD ID LookupConfig
  def mtdIdBaseUrl: String

  def desDownstreamConfig: DownstreamConfig
  def ifsDownstreamConfig: DownstreamConfig
  def tysIfsDownstreamConfig: DownstreamConfig
  def hipDownstreamConfig: BasicAuthDownstreamConfig
  // API Config
  def apiGatewayContext: String

  def apiStatus(version: Version): String

  def featureSwitches: Configuration

  def endpointsEnabled(version: Version): Boolean

  def confidenceLevelConfig: ConfidenceLevelConfig

}

@Singleton
class AppConfigImpl @Inject() (config: ServicesConfig, configuration: Configuration) extends AppConfig {
  // API name
  def appName: String = config.getString("appName")

  // MTD ID Lookup Config
  val mtdIdBaseUrl: String = config.baseUrl("mtd-id-lookup")

  private def serviceKeyFor(serviceName: String) = s"microservice.services.$serviceName"

  protected def downstreamConfig(serviceName: String): DownstreamConfig = {
    val baseUrl = config.baseUrl(serviceName)

    val serviceKey = serviceKeyFor(serviceName)

    val env                = config.getString(s"$serviceKey.env")
    val token              = config.getString(s"$serviceKey.token")
    val environmentHeaders = configuration.getOptional[Seq[String]](s"$serviceKey.environmentHeaders")

    DownstreamConfig(baseUrl, env, token, environmentHeaders)
  }

  protected def basicAuthDownstreamConfig(serviceName: String): BasicAuthDownstreamConfig = {
    val baseUrl = config.baseUrl(serviceName)

    val serviceKey = serviceKeyFor(serviceName)

    val env                = config.getString(s"$serviceKey.env")
    val clientId           = config.getString(s"$serviceKey.clientId")
    val clientSecret       = config.getString(s"$serviceKey.clientSecret")
    val environmentHeaders = configuration.getOptional[Seq[String]](s"$serviceKey.environmentHeaders")

    BasicAuthDownstreamConfig(baseUrl, env, clientId, clientSecret, environmentHeaders)
  }

  def desDownstreamConfig: DownstreamConfig          = downstreamConfig("des")
  def ifsDownstreamConfig: DownstreamConfig          = downstreamConfig("ifs")
  def tysIfsDownstreamConfig: DownstreamConfig       = downstreamConfig("tys-ifs")
  def hipDownstreamConfig: BasicAuthDownstreamConfig = basicAuthDownstreamConfig("hip")
  // API Config
  val apiGatewayContext: String = config.getString("api.gateway.context")

  def apiStatus(version: Version): String = config.getString(s"api.${version.name}.status")

  def featureSwitches: Configuration = configuration.getOptional[Configuration](s"feature-switch").getOrElse(Configuration.empty)

  def endpointsEnabled(version: Version): Boolean = config.getBoolean(s"api.${version.name}.endpoints.enabled")

  val confidenceLevelConfig: ConfidenceLevelConfig = configuration.get[ConfidenceLevelConfig](s"api.confidence-level-check")

}

case class ConfidenceLevelConfig(confidenceLevel: ConfidenceLevel, definitionEnabled: Boolean, authValidationEnabled: Boolean)

object ConfidenceLevelConfig {

  implicit val configLoader: ConfigLoader[ConfidenceLevelConfig] = (rootConfig: Config, path: String) => {
    val config = rootConfig.getConfig(path)
    ConfidenceLevelConfig(
      confidenceLevel = ConfidenceLevel.fromInt(config.getInt("confidence-level")).getOrElse(ConfidenceLevel.L200),
      definitionEnabled = config.getBoolean("definition.enabled"),
      authValidationEnabled = config.getBoolean("auth-validation.enabled")
    )
  }

}
