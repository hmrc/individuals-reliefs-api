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

import api.connectors.{ConnectorSpec, DownstreamOutcome}
import api.models.domain.{Nino, TaxYear}
import api.models.outcomes.ResponseWrapper
import v1.models.request.deletePensionsReliefs.DeletePensionsReliefsRequestData

import scala.concurrent.Future

class DeletePensionsReliefsConnectorSpec extends ConnectorSpec {

  private val nino = Nino("AA123456A")

  trait Test { _: ConnectorTest =>
    def taxYear: TaxYear

    val connector: DeletePensionsReliefsConnector =
      new DeletePensionsReliefsConnector(
        http = mockHttpClient,
        appConfig = mockAppConfig
      )

    protected val request: DeletePensionsReliefsRequestData =
      DeletePensionsReliefsRequestData(nino = nino, taxYear = taxYear)

  }

  "DeletePensionsReliefConnector" when {
    "deletePensionsRelief called" must {
      "return a 204 status for a success scenario" in
        new DesTest with Test {
          def taxYear: TaxYear = TaxYear.fromMtd("2019-20")

          val outcome: Right[Nothing, ResponseWrapper[Unit]] = Right(ResponseWrapper(correlationId, ()))

          willDelete(
            url = s"$baseUrl/income-tax/reliefs/pensions/$nino/2019-20"
          )
            .returns(Future.successful(outcome))

          val result: DownstreamOutcome[Unit] = await(connector.deletePensionsReliefs(request))
          result shouldBe outcome
        }
    }

    "deletePensionsRelief called for a Tax Year Specific tax year" must {
      "return a 204 status for a success scenario" in
        new TysIfsTest with Test {
          def taxYear: TaxYear = TaxYear.fromMtd("2023-24")

          val outcome: Right[Nothing, ResponseWrapper[Unit]] = Right(ResponseWrapper(correlationId, ()))

          willDelete(
            url = s"$baseUrl/income-tax/reliefs/pensions/23-24/$nino"
          )
            .returns(Future.successful(outcome))

          val result: DownstreamOutcome[Unit] = await(connector.deletePensionsReliefs(request))
          result shouldBe outcome
        }
    }
  }

}
