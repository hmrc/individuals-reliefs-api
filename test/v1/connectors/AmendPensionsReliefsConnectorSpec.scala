/*
 * Copyright 2022 HM Revenue & Customs
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
import uk.gov.hmrc.http.HeaderCarrier
import v1.models.domain.Nino
import v1.mocks.MockHttpClient
import v1.models.outcomes.ResponseWrapper
import v1.models.request.amendPensionsReliefs._

import scala.concurrent.Future

class AmendPensionsReliefsConnectorSpec extends ConnectorSpec {

  val taxYear: String = "2019-20"
  val nino: String    = "AA123456A"

  val body: AmendPensionsReliefsBody = AmendPensionsReliefsBody(
    PensionReliefs(
      Some(1999.99),
      Some(1999.99),
      Some(1999.99),
      Some(1999.99),
      Some(1999.99)
    )
  )

  class Test extends MockHttpClient with MockAppConfig {

    val connector: AmendPensionsReliefsConnector = new AmendPensionsReliefsConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    MockAppConfig.desBaseUrl returns baseUrl
    MockAppConfig.desToken returns "des-token"
    MockAppConfig.desEnvironment returns "des-environment"
    MockAppConfig.desEnvironmentHeaders returns Some(allowedDesHeaders)
  }

  "connector" must {
    val request: AmendPensionsReliefsRequest = AmendPensionsReliefsRequest(Nino(nino), taxYear, body)

    "put a body and return 204 no body" in new Test {
      val outcome = Right(ResponseWrapper(correlationId, ()))

      implicit val hc: HeaderCarrier                = HeaderCarrier(otherHeaders = otherHeaders ++ Seq("Content-Type" -> "application/json"))
      val requiredHeadersPut: Seq[(String, String)] = requiredDesHeaders ++ Seq("Content-Type" -> "application/json")

      MockedHttpClient
        .put(
          url = s"$baseUrl/income-tax/reliefs/pensions/$nino/$taxYear",
          config = dummyDesHeaderCarrierConfig,
          body = body,
          requiredHeaders = requiredHeadersPut,
          excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
        )
        .returns(Future.successful(outcome))

      await(connector.amend(request)) shouldBe outcome
    }
  }

}
