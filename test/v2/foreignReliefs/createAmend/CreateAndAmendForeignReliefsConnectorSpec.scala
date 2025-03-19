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

package v2.foreignReliefs.createAmend

import shared.connectors.ConnectorSpec
import shared.models.domain.{Nino, TaxYear}
import shared.models.outcomes.ResponseWrapper
import v2.foreignReliefs.createAmend.CreateAndAmendForeignReliefsFixtures.requestBodyModel
import v2.foreignReliefs.createAmend.def1.model.request.Def1_CreateAndAmendForeignReliefsRequestData
import uk.gov.hmrc.http.StringContextOps

import scala.concurrent.Future

class CreateAndAmendForeignReliefsConnectorSpec extends ConnectorSpec {

  val nino = "ZG903729C"

  "CreateAndAmendForeignReliefsConnector" must {

    "return the expected response for a non-TYS request" when {
      "a valid request is made" in new IfsTest with Test {
        val taxYear = "2021-22"
        val outcome = Right(ResponseWrapper(correlationId, ()))

        willPut(
          url = url"$baseUrl/income-tax/reliefs/foreign/$nino/2021-22",
          body = requestBodyModel
        )
          .returns(Future.successful(outcome))

        await(connector.createAndAmend(request)) shouldBe outcome
      }
    }

    "return the expected response for a TYS request" when {
      "a valid request is made" in new IfsTest with Test {
        val taxYear = "2023-24"
        val outcome = Right(ResponseWrapper(correlationId, ()))

        willPut(
          url = url"$baseUrl/income-tax/reliefs/foreign/23-24/$nino",
          body = requestBodyModel
        )
          .returns(Future.successful(outcome))

        await(connector.createAndAmend(request)) shouldBe outcome
      }
    }
  }

  trait Test { _: ConnectorTest =>

    val taxYear: String

    val connector: CreateAndAmendForeignReliefsConnector = new CreateAndAmendForeignReliefsConnector(
      http = mockHttpClient,
      appConfig = mockSharedAppConfig
    )

    lazy val request: Def1_CreateAndAmendForeignReliefsRequestData =
      def1.model.request.Def1_CreateAndAmendForeignReliefsRequestData(Nino(nino), TaxYear.fromMtd(taxYear), requestBodyModel)

  }

}
