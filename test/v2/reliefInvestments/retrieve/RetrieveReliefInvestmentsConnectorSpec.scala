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

package v2.reliefInvestments.retrieve

import shared.connectors.ConnectorSpec
import shared.models.domain.{Nino, TaxYear}
import shared.models.outcomes.ResponseWrapper
import v2.fixtures.RetrieveReliefInvestmentsFixtures.responseModel
import v2.reliefInvestments.retrieve.def1.model.request.Def1_RetrieveReliefInvestmentsRequestData
import v2.reliefInvestments.retrieve.model.request.RetrieveReliefInvestmentsRequestData

import scala.concurrent.Future

class RetrieveReliefInvestmentsConnectorSpec extends ConnectorSpec {

  trait Test { _: ConnectorTest =>

    val taxYear: String

    val connector: RetrieveReliefInvestmentsConnector = new RetrieveReliefInvestmentsConnector(
      http = mockHttpClient,
      appConfig = mockSharedAppConfig
    )

    lazy val request: RetrieveReliefInvestmentsRequestData = Def1_RetrieveReliefInvestmentsRequestData(Nino("AA123456A"), TaxYear.fromMtd(taxYear))
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

      "return a valid response for a Tax Year Specific (TYS) tax year" in new IfsTest with Test {
        val taxYear: String = "2023-24"
        val outcome         = Right(ResponseWrapper(correlationId, responseModel))

        willGet(url = s"$baseUrl/income-tax/reliefs/investment/23-24/AA123456A")
          .returns(Future.successful(outcome))

        await(connector.retrieve(request)) shouldBe outcome
      }
    }
  }

}
