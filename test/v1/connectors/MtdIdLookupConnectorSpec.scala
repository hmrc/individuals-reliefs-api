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

import mocks.MockAppConfig
import v1.mocks.MockHttpClient
import v1.models.errors.DownstreamError

import scala.concurrent.Future

class MtdIdLookupConnectorSpec extends ConnectorSpec {

  class Test extends MockHttpClient with MockAppConfig {
    val connector = new MtdIdLookupConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )
    MockAppConfig.mtdIdBaseUrl returns baseUrl
  }

  val nino = "test-nino"
  val mtdId = "test-mtdId"

  "getMtdId" should {
    "return an MtdId" when {
      "the http client returns a mtd id" in new Test {
        MockedHttpClient.get[MtdIdLookupOutcome](
          url = s"$baseUrl/mtd-identifier-lookup/nino/$nino",
          config = dummyDesHeaderCarrierConfig
        ).returns(Future.successful(Right(mtdId)))

        val result: MtdIdLookupOutcome = await(connector.getMtdId(nino))
        result shouldBe Right(mtdId)
      }
    }

    "return a DownstreamError" when {
      "the http client returns a DownstreamError" in new Test {
        MockedHttpClient.get[MtdIdLookupOutcome](
          url = s"$baseUrl/mtd-identifier-lookup/nino/$nino",
          config = dummyDesHeaderCarrierConfig
        ).returns(Future.successful(Left(DownstreamError)))

        val result: MtdIdLookupOutcome = await(connector.getMtdId(nino))
        result shouldBe Left(DownstreamError)
      }

      "getMtdId" should {
        "return an MtdId" when {
          "the http client returns a mtd id" in new Test {
            MockedHttpClient.get[MtdIdLookupOutcome](
              url = s"$baseUrl/mtd-identifier-lookup/nino/$nino",
              config = dummyIfsHeaderCarrierConfig
            ).returns(Future.successful(Right(mtdId)))

            val result: MtdIdLookupOutcome = await(connector.getMtdId(nino))
            result shouldBe Right(mtdId)
          }
        }

        "return a DownstreamError" when {
          "the http client returns a DownstreamError" in new Test {
            MockedHttpClient.get[MtdIdLookupOutcome](
              url = s"$baseUrl/mtd-identifier-lookup/nino/$nino",
              config = dummyIfsHeaderCarrierConfig
            ).returns(Future.successful(Left(DownstreamError)))

            val result: MtdIdLookupOutcome = await(connector.getMtdId(nino))
            result shouldBe Left(DownstreamError)
          }
        }
      }
    }
  }
}
