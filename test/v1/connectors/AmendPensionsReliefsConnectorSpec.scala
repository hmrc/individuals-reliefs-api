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
import v1.models.request.amendPensionsReliefs._

import scala.concurrent.Future

class AmendPensionsReliefsConnectorSpec extends ConnectorSpec {

  val nino: String = "AA123456A"

  val body: AmendPensionsReliefsBody = AmendPensionsReliefsBody(
    PensionReliefs(
      Some(1999.99),
      Some(1999.99),
      Some(1999.99),
      Some(1999.99),
      Some(1999.99)
    )
  )

  "AmendPensionsReliefsConnector" when {
    "createOrAmendPensionsRelief called" must {
      "return a 204 status for a success scenario" in new DesTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2019-20")
        val outcome          = Right(ResponseWrapper(correlationId, ()))
        willPut(url = s"$baseUrl/income-tax/reliefs/pensions/$nino/${taxYear.asMtd}", body = body)
          .returns(Future.successful(outcome))

        val result: DownstreamOutcome[Unit] = await(connector.createOrAmendPensionsRelief(request))
        result shouldBe outcome
      }
    }
    "createOrAmendPensionsRelief called for a Tax Year Specific tax year" must {
      "return a 204 status for a success scenario" in new TysIfsTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2023-24")
        val outcome          = Right(ResponseWrapper(correlationId, ()))
        willPut(url = s"$baseUrl/income-tax/reliefs/pensions/${taxYear.asTysDownstream}/$nino", body = body)
          .returns(Future.successful(outcome))

        val result: DownstreamOutcome[Unit] = await(connector.createOrAmendPensionsRelief(request))
        result shouldBe outcome
      }
    }
  }

  trait Test { _: ConnectorTest =>
    def taxYear: TaxYear

    protected val connector: AmendPensionsReliefsConnector = new AmendPensionsReliefsConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    protected val request: AmendPensionsReliefsRequest = AmendPensionsReliefsRequest(
      nino = Nino(nino),
      taxYear = taxYear,
      body = body
    )

  }

}
