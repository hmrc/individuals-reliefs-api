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

import v1.fixtures.RetrieveReliefInvestmentsFixtures.responseModel
import v1.models.domain.Nino
import v1.models.outcomes.ResponseWrapper
import v1.models.request.TaxYear
import v1.models.request.retrieveReliefInvestments.RetrieveReliefInvestmentsRequest

import scala.concurrent.Future

class RetrieveReliefInvestmentsConnectorSpec extends ConnectorSpec {

  trait Test { _: ConnectorTest =>

    val taxYear: String

    val connector: RetrieveReliefInvestmentsConnector = new RetrieveReliefInvestmentsConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    lazy val request: RetrieveReliefInvestmentsRequest = RetrieveReliefInvestmentsRequest(Nino("AA123456A"), TaxYear.fromMtd(taxYear))
  }

  "RetrieveReliefInvestmentsConnector" when {
    "retrieving relief investments" must {

      "return a valid response" in new IfsTest with Test {
        val taxYear: String = "2017-18"
        val outcome         = Right(ResponseWrapper(correlationId, responseModel))

        willGet(url = s"$baseUrl/income-tax/reliefs/investment/AA123456A/2017-18")
          .returns(Future.successful(outcome))

        await(connector.retrieve(request)) shouldBe outcome
      }

      "return a valid response for a Tax Year Specific (TYS) tax year" in new TysIfsTest with Test {
        val taxYear: String = "2023-24"
        val outcome         = Right(ResponseWrapper(correlationId, responseModel))

        willGet(url = s"$baseUrl/income-tax/reliefs/investment/23-24/AA123456A")
          .returns(Future.successful(outcome))

        await(connector.retrieve(request)) shouldBe outcome
      }
    }
  }

}
