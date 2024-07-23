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

package v1.otherReliefs.amend

import api.connectors.ConnectorSpec
import api.models.domain.{Nino, TaxYear}
import api.models.outcomes.ResponseWrapper
import v1.otherReliefs.amend.def1.model.request.{Def1_AmendOtherReliefsRequestBody, Def1_AmendOtherReliefsRequestData}

import scala.concurrent.Future

class AmendOtherReliefsConnectorSpec extends ConnectorSpec {

  "AmendOtherReliefsConnector" should {

    "return the expected response for a non-TYS request" when {
      "a valid request is made" in new IfsTest with Test {
        val request: Def1_AmendOtherReliefsRequestData = Def1_AmendOtherReliefsRequestData(Nino(nino), TaxYear.fromMtd("2017-18"), body)

        willPut(url = s"$baseUrl/income-tax/reliefs/other/$nino/2017-18", body = body)
          .returns(Future.successful(outcome))

        await(connector.amend(request)) shouldBe outcome
      }
    }

    "return the expected response for a TYS request" when {
      "a valid request is made" in new TysIfsTest with Test {
        val request: Def1_AmendOtherReliefsRequestData = Def1_AmendOtherReliefsRequestData(Nino(nino), TaxYear.fromMtd("2023-24"), body)

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

    val nino: String = "AA123456A"

    val body: Def1_AmendOtherReliefsRequestBody = Def1_AmendOtherReliefsRequestBody(None, None, None, None, None, None, None)

    protected val outcome: Right[Nothing, ResponseWrapper[Unit]] = Right(ResponseWrapper(correlationId, ()))

  }

}
