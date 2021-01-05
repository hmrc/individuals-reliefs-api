/*
 * Copyright 2021 HM Revenue & Customs
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

package v1.connectors

import uk.gov.hmrc.domain.Nino
import v1.models.outcomes.ResponseWrapper
import v1.models.request.retrievePensionsReliefs.RetrievePensionsReliefsRequest
import v1.models.response.retrievePensionsReliefs.RetrievePensionsReliefsResponse
import mocks.MockAppConfig
import v1.mocks.MockHttpClient
import scala.concurrent.Future

class RetrievePensionsReliefsConnectorSpec extends ConnectorSpec {



  val taxYear = "2017-18"
  val nino = Nino("AA123456A")

  class Test extends MockHttpClient with MockAppConfig {
    val connector: RetrievePensionsReliefsConnector = new RetrievePensionsReliefsConnector(http = mockHttpClient, appConfig = mockAppConfig)

    val desRequestHeaders: Seq[(String, String)] = Seq("Environment" -> "des-environment", "Authorization" -> s"Bearer des-token")
    MockedAppConfig.desBaseUrl returns baseUrl
    MockedAppConfig.desToken returns "des-token"
    MockedAppConfig.desEnvironment returns "des-environment"
  }

  "RetrievePensionsReliefsConnector" when {
    "retrieving pensions reliefs" must {
      val request: RetrievePensionsReliefsRequest = RetrievePensionsReliefsRequest(nino, taxYear)

      "return a valid response" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, RetrievePensionsReliefsResponse))

        MockedHttpClient
          .get(
            url = s"$baseUrl/income-tax/reliefs/pensions/$nino/$taxYear",
            requiredHeaders = "Environment" -> "des-environment", "Authorization" -> s"Bearer des-token"
          )
          .returns(Future.successful(outcome))

        await(connector.retrieve(request)) shouldBe outcome
      }
    }
  }
}
