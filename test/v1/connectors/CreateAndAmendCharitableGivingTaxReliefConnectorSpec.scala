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
import v1.mocks.MockHttpClient
import v1.models.outcomes.ResponseWrapper

import scala.concurrent.Future

class CreateAndAmendCharitableGivingTaxReliefConnectorSpec extends ConnectorSpec {

  val taxYear: String    = "2017-18"
  val nino: String       = "AA123456A"

  // input models

  class Test extends MockHttpClient with MockAppConfig {

    val connector: CreateAndAmendCharitableGivingTaxReliefConnector = new CreateAndAmendCharitableGivingTaxReliefConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    MockAppConfig.ifsBaseUrl returns baseUrl
    MockAppConfig.ifsToken returns "des-token"
    MockAppConfig.ifsEnvironment returns "des-environment"
    MockAppConfig.ifsEnvironmentHeaders returns Some(allowedDesHeaders)
  }

  "doConnector" must {

    //val request: AmendForeignReliefsRequest = AmendForeignReliefsRequest(Nino(nino), taxYear, body)

    "put a body and return 204 no body" in new Test {
      val outcome = Right(ResponseWrapper(correlationId, ()))

      implicit val hc: HeaderCarrier                = HeaderCarrier(otherHeaders = otherHeaders ++ Seq("Content-Type" -> "application/json"))
      val requiredHeadersPost: Seq[(String, String)] = requiredDesHeaders ++ Seq("Content-Type" -> "application/json")

      MockedHttpClient
        .post(
          url = s"$baseUrl/income-tax/nino/$nino/income-source/charity/annual/$taxYear",
          config = dummyDesHeaderCarrierConfig,
          body = body,
          requiredHeaders = requiredHeadersPost,
          excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
        )
        .returns(Future.successful(outcome))

      await(connector.createAmend(request)) shouldBe outcome
    }
  }

}
