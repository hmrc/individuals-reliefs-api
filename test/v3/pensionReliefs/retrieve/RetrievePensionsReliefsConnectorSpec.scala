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

package v3.pensionReliefs.retrieve

import play.api.Configuration
import shared.connectors.ConnectorSpec
import shared.models.domain.{Nino, TaxYear, Timestamp}
import shared.models.outcomes.ResponseWrapper
import v3.pensionReliefs.retrieve.def1.model.request.Def1_RetrievePensionsReliefsRequestData
import v3.pensionReliefs.retrieve.def1.model.response.{Def1_RetrievePensionsReliefsResponse, PensionsReliefs}
import v3.pensionReliefs.retrieve.model.request.RetrievePensionsReliefsRequestData
import uk.gov.hmrc.http.StringContextOps

import scala.concurrent.Future

class RetrievePensionsReliefsConnectorSpec extends ConnectorSpec {

  val nino: String            = "ZG903729C"
  val taxableEntityId: String = "ZG903729C"

  trait Test extends ConnectorTest {

    def taxYear: TaxYear

    val connector: RetrievePensionsReliefsConnector = new RetrievePensionsReliefsConnector(
      http = mockHttpClient,
      appConfig = mockSharedAppConfig
    )

    protected val request: RetrievePensionsReliefsRequestData =
      Def1_RetrievePensionsReliefsRequestData(
        nino = Nino(nino),
        taxYear = taxYear
      )

    val response =
      Def1_RetrievePensionsReliefsResponse(submittedOn = Timestamp("2021-01-02T01:20:30.000Z"), PensionsReliefs(None, None, None, None, None))

  }

  "RetrievePensionsReliefsConnector" when {
    "given a non-TYS request" when {
      "DES is not migrated to HIP" must {
        "return a success response " in new DesTest with Test {
          MockedSharedAppConfig.featureSwitchConfig returns Configuration("des_hip_migration_1656.enabled" -> false)

          def taxYear: TaxYear = TaxYear.fromMtd("2018-19")

          val outcome = Right(ResponseWrapper(correlationId, response))

          willGet(
            url = url"$baseUrl/income-tax/reliefs/pensions/$nino/2018-19"
          )
            .returns(Future.successful(outcome))

          await(connector.retrieve(request)) shouldBe outcome
        }
      }

      "DES is migrated to HIP" must {
        "return a success response" in new HipTest with Test {
          MockedSharedAppConfig.featureSwitchConfig returns Configuration("des_hip_migration_1656.enabled" -> true)

          def taxYear: TaxYear = TaxYear.fromMtd("2018-19")

          val outcome = Right(ResponseWrapper(correlationId, response))

          willGet(
            url = url"$baseUrl/itsa/income-tax/v1/reliefs/pensions/$nino/2018-19"
          )
            .returns(Future.successful(outcome))

          await(connector.retrieve(request)) shouldBe outcome
        }
      }
    }

    "retrievePensionsRelief called for a Tax Year Specific tax year" must {
      "return a 200 status for a success scenario" in
        new IfsTest with Test {

          val outcome = Right(ResponseWrapper(correlationId, response))

          def taxYear: TaxYear = TaxYear.fromMtd("2023-24")

          willGet(
            url = url"$baseUrl/income-tax/reliefs/pensions/23-24/$taxableEntityId"
          )
            .returns(Future.successful(outcome))

          await(connector.retrieve(request)) shouldBe outcome
        }
    }
  }

}
