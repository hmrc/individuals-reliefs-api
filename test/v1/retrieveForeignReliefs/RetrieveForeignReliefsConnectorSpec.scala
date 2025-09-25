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

package v1.retrieveForeignReliefs

import shared.connectors.ConnectorSpec
import shared.models.domain.{Nino, TaxYear, Timestamp}
import shared.models.outcomes.ResponseWrapper
import v1.retrieveForeignReliefs.model.request.{Def1_RetrieveForeignReliefsRequestData, RetrieveForeignReliefsRequestData}
import v1.retrieveForeignReliefs.model.response.Def1_RetrieveForeignReliefsResponse
import uk.gov.hmrc.http.StringContextOps

import scala.concurrent.Future

class RetrieveForeignReliefsConnectorSpec extends ConnectorSpec {

  val nino: String = "ZG903729C"

  trait Test extends ConnectorTest {
    def taxYear: String

    val connector: RetrieveForeignReliefsConnector = new RetrieveForeignReliefsConnector(
      http = mockHttpClient,
      appConfig = mockSharedAppConfig
    )

    lazy val request: RetrieveForeignReliefsRequestData = Def1_RetrieveForeignReliefsRequestData(Nino(nino), TaxYear.fromMtd(taxYear))

    val response = Def1_RetrieveForeignReliefsResponse(submittedOn = Timestamp("2021-01-02T01:20:30.000Z"), None, None, None)

  }

  "retrieve" should {
    "return a result" when {
      "the downstream call is successful" in new IfsTest with Test {
        def taxYear: String = "2021-22"
        val outcome         = Right(ResponseWrapper(correlationId, response))

        willGet(url"$baseUrl/income-tax/reliefs/foreign/$nino/$taxYear").returns(Future.successful(outcome))

        await(connector.retrieve(request)) shouldBe outcome
      }

      "the downstream call is successful for a TYS tax year" in new IfsTest with Test {
        def taxYear: String = "2023-24"
        val outcome         = Right(ResponseWrapper(correlationId, response))

        willGet(url"$baseUrl/income-tax/reliefs/foreign/23-24/$nino").returns(Future.successful(outcome))

        await(connector.retrieve(request)) shouldBe outcome
      }
    }
  }

}
