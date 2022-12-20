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

import v1.models.domain.Nino
import v1.models.outcomes.ResponseWrapper
import v1.models.request.TaxYear
import v1.models.request.retrieveReliefInvestments.RetrieveReliefInvestmentsRequest
import v1.models.response.retrieveReliefInvestments.RetrieveReliefInvestmentsResponse

import scala.concurrent.Future

class RetrieveReliefInvestmentsConnectorSpec extends ConnectorSpec {

  val taxYear: String = "2017-18"
  val nino: String    = "AA123456A"

  trait Test { _: ConnectorTest =>

    val connector: RetrieveReliefInvestmentsConnector = new RetrieveReliefInvestmentsConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    val response = RetrieveReliefInvestmentsResponse("2021-01-01", None, None , None, None, None)
  }

  "RetrieveReliefInvestmentsConnector" when {
    "retrieving relief investments" must {
      val request: RetrieveReliefInvestmentsRequest = RetrieveReliefInvestmentsRequest(Nino(nino), TaxYear.fromMtd(taxYear))

      "return a valid response" in new IfsTest with Test {
        val outcome = Right(ResponseWrapper(correlationId, RetrieveReliefInvestmentsResponse))

        willGet(
          url = s"$baseUrl/income-tax/reliefs/investment/$nino/$taxYear"
        )
          .returns(Future.successful(outcome))

        await(connector.retrieve(request)) shouldBe outcome
      }
    }
  }

}
