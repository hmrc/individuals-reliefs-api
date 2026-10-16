/*
 * Copyright 2026 HM Revenue & Customs
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

package api.definition

import api.config.Deprecation.NotDeprecated
import api.config.MockAppConfig
import api.definition.APIAccessType.{CONTROLLED, PUBLIC}
import api.definition.APIStatus.{ALPHA, BETA}
import api.routing.*
import api.utils.UnitSpec
import cats.implicits.catsSyntaxValidatedId

class ApiDefinitionFactorySpec extends UnitSpec {

  "calling definition" when {
    List((PUBLIC, false), (CONTROLLED, true)).foreach { (accessType, controlledAccessEnabled) =>
      s"the controlled access flag is set to $controlledAccessEnabled" should {
        s"return a valid Definition case class with the access type set to $accessType" in new Test {
          MockedAppConfig.apiGatewayContext returns "individuals/reliefs"

          setupMockConfig(Version2)
          MockedAppConfig.apiStatus(Version2) returns "BETA"
          MockedAppConfig.endpointsEnabled(Version2) returns true

          setupMockConfig(Version3)
          MockedAppConfig.apiStatus(Version3) returns "BETA"
          MockedAppConfig.endpointsEnabled(Version3) returns true

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

  "buildAPIStatus" when {
    "the 'apiStatus' parameter is present and valid" should {

      "return the expected status" in new Test {
        setupMockConfig(Version9)
        MockedAppConfig.apiStatus(Version9) returns "BETA"

        val result: APIStatus = checkBuildApiStatus(Version9)
        result shouldBe BETA
      }

    }

    "the 'apiStatus' parameter is present but invalid" should {
      "default to alpha" in new Test {
        setupMockConfig(Version9)
        MockedAppConfig.apiStatus(Version9) returns "not-a-status"

        checkBuildApiStatus(Version9) shouldBe ALPHA
      }
    }

    "the 'deprecatedOn' parameter is missing for a deprecated version" should {
      "throw an exception" in new Test {
        MockedAppConfig.apiStatus(Version9) returns "DEPRECATED"

        MockedAppConfig
          .deprecationFor(Version9)
          .returns("deprecatedOn date is required for a deprecated version".invalid)
          .anyNumberOfTimes()

        val exception: Exception = intercept[Exception] {
          checkBuildApiStatus(Version9)
        }

        val exceptionMessage: String = exception.getMessage
        exceptionMessage shouldBe "deprecatedOn date is required for a deprecated version"
      }
    }
  }

  class Test extends MockAppConfig {
    protected val apiDefinitionFactory: ApiDefinitionFactory = new ApiDefinitionFactory(mockAppConfig)

    def checkBuildApiStatus(version: Version): APIStatus = apiDefinitionFactory.buildAPIStatus(version)

    protected def setupMockConfig(version: Version): Unit = {
      MockedAppConfig
        .deprecationFor(version)
        .returns(NotDeprecated.valid)
        .anyNumberOfTimes()
    }

  }

}
