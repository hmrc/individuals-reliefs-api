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
import v1.models.request.retrievePensionsReliefs.RetrievePensionsReliefsRequest
import v1.models.response.retrievePensionsReliefs.RetrievePensionsReliefsResponse

import scala.concurrent.Future

class RetrievePensionsReliefsConnectorSpec extends ConnectorSpec {

  val nino: String = "AA123456A"
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
        taxYear = taxYear,
      )
  }

  "RetrievePensionsReliefsConnector" when {
    "retrieving pensions reliefs" must {
      "return a valid response" in new DesTest with Test {

        val outcome = Right(ResponseWrapper(correlationId, RetrievePensionsReliefsResponse))

        def taxYear: TaxYear = TaxYear.fromMtd("2018-19")

        willGet(
          url = s"$baseUrl/income-tax/reliefs/pensions/$nino/${taxYear.asDownstream}"
        )
          .returns(Future.successful(outcome))

        await(connector.retrieve(request)) shouldBe outcome
      }
    }

    "retrievePensionsRelief called for a Tax Year Specific tax year" must {
      "return a 200 status for a success scenario" in
        new TysIfsTest with Test {

          val outcome = Right(ResponseWrapper(correlationId, RetrievePensionsReliefsResponse))

          def taxYear: TaxYear = TaxYear.fromMtd("2023-24")

          willGet(
            url = s"$baseUrl/income-tax/reliefs/pensions/${taxYear.asTysDownstream}/$taxableEntityId"
          )
            .returns(Future.successful(outcome))

          await(connector.retrieve(request)) shouldBe outcome
        }
    }
  }
}
