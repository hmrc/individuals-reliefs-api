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

package v1.deleteForeignReliefs

import shared.connectors.ConnectorSpec
import shared.models.domain.{Nino, TaxYear}
import shared.models.outcomes.ResponseWrapper
import v1.deleteForeignReliefs.model.Def1_DeleteForeignReliefsRequestData
import uk.gov.hmrc.http.StringContextOps

import scala.concurrent.Future

class DeleteForeignReliefsConnectorSpec extends ConnectorSpec {

  private val nino = Nino("ZG903729C")

  "DeleteForeignReliefsConnector" should {

    "return the expected response for a non-TYS request" when {
      "a valid request is made" in new IfsTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2019-20")
        private val outcome  = Right(ResponseWrapper(correlationId, ()))

        willDelete(
          url = url"$baseUrl/income-tax/reliefs/foreign/$nino/2019-20"
        )
          .returns(Future.successful(outcome))

        await(connector.delete(request)) shouldBe outcome
      }
    }

    "return the expected response for a TYS request" when {
      "a valid request is made" in new IfsTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2023-24")
        private val outcome  = Right(ResponseWrapper(correlationId, ()))

        willDelete(
          url = url"$baseUrl/income-tax/reliefs/foreign/23-24/$nino"
        ).returns(Future.successful(outcome))

        await(connector.delete(request)) shouldBe outcome
      }
    }
  }

  trait Test extends ConnectorTest {

    def taxYear: TaxYear
    val request: Def1_DeleteForeignReliefsRequestData = Def1_DeleteForeignReliefsRequestData(nino, taxYear)

    val connector: DeleteForeignReliefsConnector = new DeleteForeignReliefsConnector(
      http = mockHttpClient,
      appConfig = mockSharedAppConfig
    )

  }

}
