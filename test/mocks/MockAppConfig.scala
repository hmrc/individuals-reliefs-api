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

package mocks

import config.{AppConfig, BasicAuthDownstreamConfig, ConfidenceLevelConfig, DownstreamConfig}
import org.scalamock.handlers.{CallHandler, CallHandler0}
import org.scalamock.scalatest.MockFactory
import play.api.Configuration
import routing.Version

trait MockAppConfig extends MockFactory {

  val mockAppConfig: AppConfig = mock[AppConfig]

  object MockedAppConfig {
    def desDownstreamConfig: CallHandler0[DownstreamConfig]    = (() => mockAppConfig.desDownstreamConfig: DownstreamConfig).expects()
    def ifsDownstreamConfig: CallHandler0[DownstreamConfig]    = (() => mockAppConfig.ifsDownstreamConfig: DownstreamConfig).expects()
    def tysIfsDownstreamConfig: CallHandler0[DownstreamConfig] = (() => mockAppConfig.tysIfsDownstreamConfig: DownstreamConfig).expects()
    def hipDownstreamConfig: CallHandler[BasicAuthDownstreamConfig] = (() => mockAppConfig.hipDownstreamConfig: BasicAuthDownstreamConfig).expects()

    // MTD IF Lookup Config
    def mtdIdBaseUrl: CallHandler[String] = (() => mockAppConfig.mtdIdBaseUrl).expects()

    // API Config
    def featureSwitchConfig: CallHandler[Configuration]          = (() => mockAppConfig.featureSwitches: Configuration).expects()
    def apiGatewayContext: CallHandler[String]                   = (() => mockAppConfig.apiGatewayContext).expects()
    def apiStatus(version: Version): CallHandler[String]         = (mockAppConfig.apiStatus(_: Version)).expects(version)
    def endpointsEnabled(version: Version): CallHandler[Boolean] = (mockAppConfig.endpointsEnabled(_: Version)).expects(version)

    def confidenceLevelCheckEnabled: CallHandler[ConfidenceLevelConfig] =
      (() => mockAppConfig.confidenceLevelConfig).expects()

  }

}
