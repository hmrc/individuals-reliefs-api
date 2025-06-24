/*
 * Copyright 2024 HM Revenue & Customs
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

package v1.pensionReliefs.createAmend

import shared.connectors.{ConnectorSpec, DownstreamOutcome}
import shared.models.domain.{Nino, TaxYear}
import shared.models.outcomes.ResponseWrapper
import v1.pensionReliefs.createAmend.def1.model.request.{CreateAmendPensionsReliefsBody, Def1_CreateAmendPensionsReliefsRequestData, PensionReliefs}
import v1.pensionReliefs.createAmend.model.request.CreateAmendPensionsReliefsRequestData
import uk.gov.hmrc.http.StringContextOps

import scala.concurrent.Future

class CreateAmendPensionsReliefsConnectorSpec extends ConnectorSpec {

  val nino: String = "ZG903729C"

  val body: CreateAmendPensionsReliefsBody = CreateAmendPensionsReliefsBody(
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
      "return a 204 status for a success scenario enabled" in new IfsTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2019-20")

        val outcome = Right(ResponseWrapper(correlationId, ()))
        willPut(url = url"$baseUrl/income-tax/reliefs/pensions/$nino/${taxYear.asMtd}", body = body)
          .returns(Future.successful(outcome))

        val result: DownstreamOutcome[Unit] = await(connector.createOrAmendPensionsRelief(request))
        result shouldBe outcome
      }
    }
    "createOrAmendPensionsRelief called for a Tax Year Specific tax year" must {
      "return a 204 status for a success scenario" in new IfsTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2023-24")
        val outcome          = Right(ResponseWrapper(correlationId, ()))
        willPut(url = url"$baseUrl/income-tax/reliefs/pensions/${taxYear.asTysDownstream}/$nino", body = body)
          .returns(Future.successful(outcome))

        val result: DownstreamOutcome[Unit] = await(connector.createOrAmendPensionsRelief(request))
        result shouldBe outcome
      }
    }
  }

  trait Test { _: ConnectorTest =>
    def taxYear: TaxYear

    protected val connector: CreateAmendPensionsReliefsConnector = new CreateAmendPensionsReliefsConnector(
      http = mockHttpClient,
      appConfig = mockSharedAppConfig
    )

    protected val request: CreateAmendPensionsReliefsRequestData = Def1_CreateAmendPensionsReliefsRequestData(
      nino = Nino(nino),
      taxYear = taxYear,
      body = body
    )

  }

}
