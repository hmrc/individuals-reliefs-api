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

package v2.otherReliefs.delete

import shared.connectors.ConnectorSpec
import shared.models.domain.{Nino, TaxYear}
import shared.models.outcomes.ResponseWrapper
import v2.otherReliefs.delete.def1.Def1_DeleteOtherReliefsRequestData
import uk.gov.hmrc.http.StringContextOps

import scala.concurrent.Future

class DeleteOtherReliefsConnectorSpec extends ConnectorSpec {

  val nino: String = "ZG903729C"

  trait Test extends ConnectorTest {

    def taxYear: String

    val connector: DeleteOtherReliefsConnector = new DeleteOtherReliefsConnector(
      http = mockHttpClient,
      appConfig = mockSharedAppConfig
    )

    lazy val request: Def1_DeleteOtherReliefsRequestData = Def1_DeleteOtherReliefsRequestData(Nino(nino), TaxYear.fromMtd(taxYear))
  }

  "DeleteOtherReliefsConnector" should {
    "return the expected response for a non-TYS request" when {
      "a valid request is made" in new IfsTest with Test {

        def taxYear: String = "2020-21"
        val outcome         = Right(ResponseWrapper(correlationId, ()))

        willDelete(url = url"$baseUrl/income-tax/reliefs/other/$nino/2020-21")
          .returns(Future.successful(outcome))

        await(connector.delete(request)) shouldBe outcome
      }

      "return the expected response for a TYS request" when {
        "a valid request is made" in new IfsTest with Test {

          def taxYear: String = "2023-24"
          val outcome         = Right(ResponseWrapper(correlationId, ()))

          willDelete(url = url"$baseUrl/income-tax/reliefs/other/23-24/$nino")
            .returns(Future.successful(outcome))

          await(connector.delete(request)) shouldBe outcome
        }
      }
    }
  }

}
