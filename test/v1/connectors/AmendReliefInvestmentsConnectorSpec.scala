/*
 * Copyright 2021 HM Revenue & Customs
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
import uk.gov.hmrc.domain.Nino
import v1.mocks.MockHttpClient
import v1.models.outcomes.ResponseWrapper
import v1.models.request.amendReliefInvestments.{AmendReliefInvestmentsBody, AmendReliefInvestmentsRequest, CommunityInvestmentItem, EisSubscriptionsItem, SeedEnterpriseInvestmentItem, SocialEnterpriseInvestmentItem, VctSubscriptionsItem}

import scala.concurrent.Future

class AmendReliefInvestmentsConnectorSpec extends ConnectorSpec {

  val taxYear = "2017-18"
  val nino = Nino("AA123456A")
  val body = AmendReliefInvestmentsBody(
    Some(Seq(VctSubscriptionsItem(
      "VCTREF",
      Some("VCT Fund X"),
      Some("2018-04-16"),
      Some(BigDecimal(23312.00)),
      BigDecimal(1334.00)
    ))),
    Some(Seq(EisSubscriptionsItem(
      "XTAL",
      Some("EIS Fund X"),
      true,
      Some("2020-12-12"),
      Some(BigDecimal(23312.00)),
      BigDecimal(43432.00)
    ))),
    Some(Seq(CommunityInvestmentItem(
      "CIREF",
      Some("CI X"),
      Some("2020-12-12"),
      Some(BigDecimal(6442.00)),
      BigDecimal(2344.00)
    ))),
    Some(Seq(SeedEnterpriseInvestmentItem(
      "123412/1A",
      Some("Company Inc"),
      Some("2020-12-12"),
      Some(BigDecimal(123123.22)),
      BigDecimal(3432.00)
    ))),
    Some(Seq(SocialEnterpriseInvestmentItem(
      "123412/1A",
      Some("SE Inc"),
      Some("2020-12-12"),
      Some(BigDecimal(123123.22)),
      BigDecimal(3432.00)
    )))
  )

  class Test extends MockHttpClient with MockAppConfig {
    val connector: AmendReliefInvestmentsConnector = new AmendReliefInvestmentsConnector(http = mockHttpClient, appConfig = mockAppConfig)

    val desRequestHeaders: Seq[(String, String)] = Seq("Environment" -> "des-environment", "Authorization" -> s"Bearer des-token")
    MockedAppConfig.desBaseUrl returns baseUrl
    MockedAppConfig.desToken returns "des-token"
    MockedAppConfig.desEnvironment returns "des-environment"
  }

  "doConnector" must {
    val request = AmendReliefInvestmentsRequest(nino, taxYear, body)

    "put a body and return 204 no body" in new Test {
      val outcome = Right(ResponseWrapper(correlationId, ()))

      MockedHttpClient
        .put(
          url = s"$baseUrl/income-tax/reliefs/investment/$nino/$taxYear",
          body = body,
          requiredHeaders ="Environment" -> "des-environment", "Authorization" -> s"Bearer des-token"
        )
        .returns(Future.successful(outcome))

      await(connector.amend(request)) shouldBe outcome
    }
  }
}
