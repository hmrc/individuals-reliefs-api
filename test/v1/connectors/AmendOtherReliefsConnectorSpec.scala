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
import v1.models.request.amendOtherReliefs._

import scala.concurrent.Future

class AmendOtherReliefsConnectorSpec extends ConnectorSpec {

  "AmendOtherReliefsConnector" must {

    "return the expected response for a non-TYS request" when {
      "a valid request is made" in new IfsTest with Test {
        val request: AmendOtherReliefsRequest = AmendOtherReliefsRequest(Nino(nino), TaxYear.fromMtd("2017-18"), body)

        willPut(url = s"$baseUrl/income-tax/reliefs/other/$nino/$taxYear", body = body)
          .returns(Future.successful(outcome))

        await(connector.amend(request)) shouldBe outcome
      }
    }

    "return the expected response for a TYS request" when {
      "a valid request is made" in new TysIfsTest with Test {
        val request: AmendOtherReliefsRequest = AmendOtherReliefsRequest(Nino(nino), TaxYear.fromMtd("2023-24"), body)

        willPut(url = s"$baseUrl/income-tax/reliefs/other/23-24/$nino", body = body)
          .returns(Future.successful(outcome))

        await(connector.amend(request)) shouldBe outcome
      }
    }

  }

  trait Test {
    _: ConnectorTest =>

    val connector: AmendOtherReliefsConnector = new AmendOtherReliefsConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    val taxYear: String = "2017-18"
    val nino: String    = "AA123456A"

    val body: AmendOtherReliefsBody = AmendOtherReliefsBody(None, None, None, None, None, None, None)

    val outcome = Right(ResponseWrapper(correlationId, ()))

  }

}
