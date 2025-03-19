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

package v2.reliefInvestments.delete

import shared.connectors.ConnectorSpec
import shared.models.domain.{Nino, TaxYear}
import shared.models.outcomes.ResponseWrapper
import v2.reliefInvestments.delete.def1.Def1_DeleteReliefInvestmentsRequestData
import v2.reliefInvestments.delete.model.DeleteReliefInvestmentsRequestData
import uk.gov.hmrc.http.StringContextOps

import scala.concurrent.Future

class DeleteReliefInvestmentsConnectorSpec extends ConnectorSpec {

  val nino: String = "ZG903729C"

  trait Test { _: ConnectorTest =>

    val connector: DeleteReliefInvestmentsConnector = new DeleteReliefInvestmentsConnector(
      http = mockHttpClient,
      appConfig = mockSharedAppConfig
    )

    val taxYear: TaxYear

    val request: DeleteReliefInvestmentsRequestData = Def1_DeleteReliefInvestmentsRequestData(Nino(nino), taxYear)
  }

  "delete" should {

    "return a result" when {
      "the downstream call is successful" in new IfsTest with Test {
        lazy val taxYear = TaxYear.fromMtd("2019-20")
        val outcome      = Right(ResponseWrapper(correlationId, ()))

        willDelete(
          url = url"$baseUrl/income-tax/reliefs/investment/$nino/2019-20"
        ) returns Future.successful(outcome)

        await(connector.delete(request)) shouldBe outcome
      }
    }

    "return a result" when {
      "the downstream call is successful for TYS tax years" in new IfsTest with Test {
        lazy val taxYear = TaxYear.fromMtd("2023-24")
        val outcome      = Right(ResponseWrapper(correlationId, ()))

        willDelete(
          url = url"$baseUrl/income-tax/reliefs/investment/23-24/$nino"
        ) returns Future.successful(outcome)

        await(connector.delete(request)) shouldBe outcome
      }
    }
  }

}
