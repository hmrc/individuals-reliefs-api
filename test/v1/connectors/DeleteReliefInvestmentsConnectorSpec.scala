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
import v1.models.request.deleteReliefInvestments.DeleteReliefInvestmentsRequest

import scala.concurrent.Future

class DeleteReliefInvestmentsConnectorSpec extends ConnectorSpec {

  val nino: String = "AA123456A"

  trait Test { _: ConnectorTest =>

    val connector: DeleteReliefInvestmentsConnector = new DeleteReliefInvestmentsConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    val taxYear: TaxYear

    val request: DeleteReliefInvestmentsRequest = DeleteReliefInvestmentsRequest(Nino(nino), taxYear)
  }

  "delete" should {

    "return a result" when {
      "the downstream call is successful" in new IfsTest with Test {
        lazy val taxYear = TaxYear.fromMtd("2019-20")
        val outcome      = Right(ResponseWrapper(correlationId, ()))

        willDelete(
          url = s"$baseUrl/income-tax/reliefs/investment/$nino/2019-20"
        ) returns Future.successful(outcome)

        await(connector.delete(request)) shouldBe outcome
      }
    }

    "return a result" when {
      "the downstream call is successful for TYS tax years" in new TysIfsTest with Test {
        lazy val taxYear = TaxYear.fromMtd("2023-24")
        val outcome      = Right(ResponseWrapper(correlationId, ()))

        willDelete(
          url = s"$baseUrl/income-tax/reliefs/investment/23-24/$nino"
        ) returns Future.successful(outcome)

        await(connector.delete(request)) shouldBe outcome
      }
    }
  }

}
