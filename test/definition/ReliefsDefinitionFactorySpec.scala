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

package definition

import api.config.Deprecation.NotDeprecated
import api.config.MockAppConfig
import api.definition.APIAccessType.{CONTROLLED, PUBLIC}
import api.definition.*
import api.definition.APIStatus.BETA
import api.mocks.MockHttpClient
import api.routing.*
import api.utils.UnitSpec
import cats.implicits.catsSyntaxValidatedId

class ReliefsDefinitionFactorySpec extends UnitSpec with MockAppConfig with MockHttpClient {

  "calling definition" when {
    List((PUBLIC, false), (CONTROLLED, true)).foreach { (accessType, controlledAccessEnabled) =>
      s"the controlled access flag is set to $controlledAccessEnabled" should {
        s"return a valid Definition case class with the access type set to $accessType" in new Test {
          MockedAppConfig.apiStatus(Version2) returns "BETA"
          MockedAppConfig.endpointsEnabled(Version2) returns true
          MockedAppConfig.deprecationFor(Version2).returns(NotDeprecated.valid)

          MockedAppConfig.apiStatus(Version3) returns "BETA"
          MockedAppConfig.endpointsEnabled(Version3) returns true
          MockedAppConfig.deprecationFor(Version3).returns(NotDeprecated.valid)

          MockedAppConfig.controlledAccessEnabled.returns(controlledAccessEnabled).twice()

          apiDefinitionFactory.definition shouldBe
            Definition(
              api = APIDefinition(
                name = "Individuals Reliefs (MTD)",
                description = "An API for providing individual relief data",
                context = "individuals/reliefs",
                categories = Seq("INCOME_TAX_MTD"),
                versions = Seq(
                  APIVersion(
                    version = Version2,
                    status = BETA,
                    access = accessType,
                    endpointsEnabled = true
                  ),
                  APIVersion(
                    version = Version3,
                    status = BETA,
                    access = accessType,
                    endpointsEnabled = true
                  )
                ),
                requiresTrust = None
              )
            )
        }
      }
    }
  }

  trait Test extends MockAppConfig {
    MockedAppConfig.apiGatewayContext.anyNumberOfTimes() returns "individuals/reliefs"

    val apiDefinitionFactory = new ReliefsDefinitionFactory(mockAppConfig)
  }

}
