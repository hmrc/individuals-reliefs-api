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
import v1.models.request.retrieveOtherReliefs.RetrieveOtherReliefsRequest
import v1.models.response.retrieveOtherReliefs.RetrieveOtherReliefsResponse

import scala.concurrent.Future

class RetrieveOtherReliefsConnectorSpec extends ConnectorSpec {

  val nino: String = "AA123456A"

  trait Test { _: ConnectorTest =>

    val connector: RetrieveOtherReliefsConnector = new RetrieveOtherReliefsConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    val taxYear: TaxYear

    val request: RetrieveOtherReliefsRequest = RetrieveOtherReliefsRequest(Nino(nino), taxYear)

    val response = RetrieveOtherReliefsResponse(submittedOn = "2022-01-01", None, None, None, None, None, None, None)
  }

  "retrieve" should {
    "return a result" when {

      "the downstream call is successful" in new IfsTest with Test {
        lazy val taxYear: TaxYear = TaxYear.fromMtd("2017-18")

        val outcome               = Right(ResponseWrapper(correlationId, response))

        willGet(
          url = s"$baseUrl/income-tax/reliefs/other/$nino/2017-18"
        )
          .returns(Future.successful(outcome))

        await(connector.retrieve(request)) shouldBe outcome
      }

      "the downstream call is successful (TYS)" in new TysIfsTest with Test {
        lazy val taxYear: TaxYear = TaxYear.fromMtd("2023-24")

        val outcome               = Right(ResponseWrapper(correlationId, response))

        willGet(
          url = s"$baseUrl/income-tax/reliefs/other/23-24/$nino"
        )
          .returns(Future.successful(outcome))

        await(connector.retrieve(request)) shouldBe outcome
      }
    }
  }

}
