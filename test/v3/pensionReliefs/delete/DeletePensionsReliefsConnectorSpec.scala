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

package v3.pensionReliefs.delete

import shared.connectors.{ConnectorSpec, DownstreamOutcome}
import shared.models.domain.{Nino, TaxYear}
import shared.models.outcomes.ResponseWrapper
import v3.pensionReliefs.delete.def1.model.request.Def1_DeletePensionsReliefsRequestData
import v3.pensionReliefs.delete.model.request.DeletePensionsReliefsRequestData
import uk.gov.hmrc.http.StringContextOps

import scala.concurrent.Future

class DeletePensionsReliefsConnectorSpec extends ConnectorSpec {

  private val nino = Nino("AA123456A")

  trait Test extends ConnectorTest {
    def taxYear: TaxYear

    val connector: DeletePensionsReliefsConnector =
      new DeletePensionsReliefsConnector(
        http = mockHttpClient,
        appConfig = mockSharedAppConfig
      )

    protected val request: DeletePensionsReliefsRequestData =
      Def1_DeletePensionsReliefsRequestData(nino = nino, taxYear = taxYear)

  }

  "DeletePensionsReliefConnector" when {
    "deletePensionsRelief called" must {
      "return a 204 status for a success scenario" in
        new DesTest with Test {
          def taxYear: TaxYear = TaxYear.fromMtd("2019-20")

          val outcome: Right[Nothing, ResponseWrapper[Unit]] = Right(ResponseWrapper(correlationId, ()))

          willDelete(
            url = url"$baseUrl/income-tax/reliefs/pensions/$nino/2019-20"
          )
            .returns(Future.successful(outcome))

          val result: DownstreamOutcome[Unit] = await(connector.deletePensionsReliefs(request))
          result shouldBe outcome
        }
    }

    "deletePensionsRelief called for a Tax Year Specific tax year" must {
      "return a 204 status for a success scenario" in
        new IfsTest with Test {
          def taxYear: TaxYear = TaxYear.fromMtd("2023-24")

          val outcome: Right[Nothing, ResponseWrapper[Unit]] = Right(ResponseWrapper(correlationId, ()))

          willDelete(
            url = url"$baseUrl/income-tax/reliefs/pensions/23-24/$nino"
          )
            .returns(Future.successful(outcome))

          val result: DownstreamOutcome[Unit] = await(connector.deletePensionsReliefs(request))
          result shouldBe outcome
        }
    }
  }

}
