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

package v1.connectors

import api.connectors.ConnectorSpec
import api.models.domain.{Nino, TaxYear, Timestamp}
import api.models.outcomes.ResponseWrapper
import v1.models.request.retrieveForeignReliefs.RetrieveForeignReliefsRequest
import v1.models.response.retrieveForeignReliefs.RetrieveForeignReliefsResponse

import scala.concurrent.Future

class RetrieveForeignReliefsConnectorSpec extends ConnectorSpec {

  val nino: String = "AA123456A"

  trait Test {
    _: ConnectorTest =>
    def taxYear: String

    val connector: RetrieveForeignReliefsConnector = new RetrieveForeignReliefsConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    lazy val request: RetrieveForeignReliefsRequest = new RetrieveForeignReliefsRequest(Nino(nino), TaxYear.fromMtd(taxYear))

    val response = RetrieveForeignReliefsResponse(submittedOn = Timestamp("2021-01-02T01:20:30.000Z"), None, None, None)

  }

  "retrieve" should {
    "return a result" when {
      "the downstream call is successful" in new IfsTest with Test {
        def taxYear: String = "2021-22"
        val outcome         = Right(ResponseWrapper(correlationId, response))

        willGet(s"$baseUrl/income-tax/reliefs/foreign/$nino/$taxYear").returns(Future.successful(outcome))

        await(connector.retrieve(request)) shouldBe outcome
      }

      "the downstream call is successful for a TYS tax year" in new TysIfsTest with Test {
        def taxYear: String = "2023-24"
        val outcome         = Right(ResponseWrapper(correlationId, response))

        willGet(s"$baseUrl/income-tax/reliefs/foreign/23-24/$nino").returns(Future.successful(outcome))

        await(connector.retrieve(request)) shouldBe outcome
      }
    }
  }

}
