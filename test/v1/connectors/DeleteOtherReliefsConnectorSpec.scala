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

import api.models.domain.{Nino, TaxYear}
import api.models.outcomes.ResponseWrapper
import v1.models.request.deleteOtherReliefs.DeleteOtherReliefsRequest

import scala.concurrent.Future

class DeleteOtherReliefsConnectorSpec extends ConnectorSpec {

  val nino: String = "AA123456A"

  trait Test { _: ConnectorTest =>

    def taxYear: String

    val connector: DeleteOtherReliefsConnector = new DeleteOtherReliefsConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    lazy val request: DeleteOtherReliefsRequest = DeleteOtherReliefsRequest(Nino(nino), TaxYear.fromMtd(taxYear))
  }

  "DeleteOtherReliefsConnector" should {
    "return the expected response for a non-TYS request" when {
      "a valid request is made" in new IfsTest with Test {

        def taxYear: String = "2020-21"
        val outcome         = Right(ResponseWrapper(correlationId, ()))

        willDelete(url = s"$baseUrl/income-tax/reliefs/other/$nino/2020-21")
          .returns(Future.successful(outcome))

        await(connector.delete(request)) shouldBe outcome
      }

      "return the expected response for a TYS request" when {
        "a valid request is made" in new TysIfsTest with Test {

          def taxYear: String = "2023-24"
          val outcome         = Right(ResponseWrapper(correlationId, ()))

          willDelete(url = s"$baseUrl/income-tax/reliefs/other/23-24/$nino")
            .returns(Future.successful(outcome))

          await(connector.delete(request)) shouldBe outcome
        }
      }
    }
  }

}
