/*
 * Copyright 2025 HM Revenue & Customs
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

package v3.otherReliefs.retrieve

import shared.connectors.ConnectorSpec
import shared.models.domain.{Nino, TaxYear, Timestamp}
import shared.models.outcomes.ResponseWrapper
import v3.otherReliefs.retrieve.def1.model.request.Def1_RetrieveOtherReliefsRequestData
import v3.otherReliefs.retrieve.def1.model.response.Def1_RetrieveOtherReliefsResponse
import v3.otherReliefs.retrieve.model.request.RetrieveOtherReliefsRequestData
import uk.gov.hmrc.http.StringContextOps

import scala.concurrent.Future

class RetrieveOtherReliefsConnectorSpec extends ConnectorSpec {

  val nino: String = "ZG903729C"

  trait Test { _: ConnectorTest =>

    val connector: RetrieveOtherReliefsConnector = new RetrieveOtherReliefsConnector(
      http = mockHttpClient,
      appConfig = mockSharedAppConfig
    )

    val taxYear: TaxYear

    val request: RetrieveOtherReliefsRequestData = Def1_RetrieveOtherReliefsRequestData(Nino(nino), taxYear)

    val response: Def1_RetrieveOtherReliefsResponse =
      Def1_RetrieveOtherReliefsResponse(submittedOn = Timestamp("2021-01-02T01:20:30.000Z"), None, None, None, None, None, None, None)

  }

  "RetrieveOtherReliefsConnector" should {
    "return the expected response for a non-TYS request" when {
      "a valid request is made" in new IfsTest with Test {
        lazy val taxYear: TaxYear = TaxYear.fromMtd("2017-18")

        val outcome = Right(ResponseWrapper(correlationId, response))

        willGet(
          url = url"$baseUrl/income-tax/reliefs/other/$nino/2017-18"
        )
          .returns(Future.successful(outcome))

        await(connector.retrieve(request)) shouldBe outcome
      }
    }

    "return the expected response for a TYS request" when {
      "a valid request is made" in new IfsTest with Test {
        lazy val taxYear: TaxYear = TaxYear.fromMtd("2023-24")

        val outcome = Right(ResponseWrapper(correlationId, response))

        willGet(
          url = url"$baseUrl/income-tax/reliefs/other/23-24/$nino"
        )
          .returns(Future.successful(outcome))

        await(connector.retrieve(request)) shouldBe outcome
      }
    }
  }

}
