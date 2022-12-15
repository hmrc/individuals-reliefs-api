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
import v1.models.request.deleteOtherReliefs.DeleteOtherReliefsRequest

import scala.concurrent.Future

class DeleteOtherReliefsConnectorSpec extends ConnectorSpec {

  val nino: String    = "AA123456A"
  val taxYear: String = "2019-20"

  trait Test { _: ConnectorTest =>

    val connector: DeleteOtherReliefsConnector = new DeleteOtherReliefsConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

  }

  "delete" should {
    val request = DeleteOtherReliefsRequest(Nino(nino), TaxYear.fromMtd(taxYear))

    "return a result" when {
      "the downstream call is successful" in new IfsTest with Test {
        val outcome = Right(ResponseWrapper(correlationId, ()))

        willDelete(
          url = s"$baseUrl/income-tax/reliefs/other/${request.nino}/$taxYear"
        )
          .returns(Future.successful(outcome))

        await(connector.delete(request)) shouldBe outcome
      }
    }
  }

}
