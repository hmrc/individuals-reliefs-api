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

import mocks.MockAppConfig
import v1.models.domain.Nino
import v1.mocks.MockHttpClient
import v1.models.outcomes.ResponseWrapper
import v1.models.request.deleteForeignReliefs.DeleteForeignReliefsRequest

import scala.concurrent.Future

class DeleteForeignReliefsConnectorSpec extends ConnectorSpec {

  val nino: String = "AA123456A"
  val taxYear: String = "2019-20"

  class Test extends MockHttpClient with MockAppConfig {

    val connector: DeleteForeignReliefsConnector = new DeleteForeignReliefsConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    MockAppConfig.ifsBaseUrl returns baseUrl
    MockAppConfig.ifsToken returns "ifs-token"
    MockAppConfig.ifsEnvironment returns "ifs-environment"
    MockAppConfig.ifsEnvironmentHeaders returns Some(allowedIfsHeaders)
  }

  "delete" should {

    val request: DeleteForeignReliefsRequest = DeleteForeignReliefsRequest(Nino(nino), taxYear)

    "return a result" when {
      "the downstream call is successful" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, ()))

        MockedHttpClient.
          delete(
            url = s"$baseUrl/income-tax/reliefs/foreign/${request.nino.nino}/${request.taxYear}",
            config = dummyIfsHeaderCarrierConfig,
            requiredHeaders = requiredIfsHeaders,
            excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
          ).returns(Future.successful(outcome))

        await(connector.delete(request)) shouldBe outcome
      }
    }
  }
}
