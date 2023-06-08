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
import v1.models.response.retrievePensionsReliefs.PensionsReliefs
import v1.models.request.retrievePensionsReliefs.RetrievePensionsReliefsRequest
import v1.models.response.retrievePensionsReliefs.RetrievePensionsReliefsResponse

import scala.concurrent.Future

class RetrievePensionsReliefsConnectorSpec extends ConnectorSpec {

  val nino: String            = "AA123456A"
  val taxableEntityId: String = "AA123456A"

  trait Test {
    _: ConnectorTest =>

    def taxYear: TaxYear

    val connector: RetrievePensionsReliefsConnector = new RetrievePensionsReliefsConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    protected val request: RetrievePensionsReliefsRequest =
      RetrievePensionsReliefsRequest(
        nino = Nino(nino),
        taxYear = taxYear
      )

    val response = RetrievePensionsReliefsResponse(submittedOn = Timestamp("2021-01-02T01:20:30.000Z"), PensionsReliefs(None, None, None, None, None))
  }

  "RetrievePensionsReliefsConnector" when {
    "retrieving pensions reliefs" must {
      "return a valid response" in new DesTest with Test {

        val outcome = Right(ResponseWrapper(correlationId, response))

        def taxYear: TaxYear = TaxYear.fromMtd("2018-19")

        willGet(
          url = s"$baseUrl/income-tax/reliefs/pensions/$nino/2018-19"
        )
          .returns(Future.successful(outcome))

        await(connector.retrieve(request)) shouldBe outcome
      }
    }

    "retrievePensionsRelief called for a Tax Year Specific tax year" must {
      "return a 200 status for a success scenario" in
        new TysIfsTest with Test {

          val outcome = Right(ResponseWrapper(correlationId, response))

          def taxYear: TaxYear = TaxYear.fromMtd("2023-24")

          willGet(
            url = s"$baseUrl/income-tax/reliefs/pensions/23-24/$taxableEntityId"
          )
            .returns(Future.successful(outcome))

          await(connector.retrieve(request)) shouldBe outcome
        }
    }
  }

}
