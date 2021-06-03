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
import uk.gov.hmrc.http.HeaderCarrier
import v1.models.domain.Nino
import v1.mocks.MockHttpClient
import v1.models.outcomes.ResponseWrapper
import v1.models.request.amendOtherReliefs.{AmendOtherReliefsBody, _}

import scala.concurrent.Future

class AmendOtherReliefsConnectorSpec extends ConnectorSpec {

  val taxYear: String = "2017-18"
  val nino: String  = "AA123456A"

  val body: AmendOtherReliefsBody = AmendOtherReliefsBody(

    Some(NonDeductibleLoanInterest(
      Some("myref"),
      763.00)),
    Some(PayrollGiving(
      Some("myref"),
      154.00)),
    Some(QualifyingDistributionRedemptionOfSharesAndSecurities(
      Some("myref"),
      222.22)),
    Some(Seq(MaintenancePayments(
      Some("myref"),
      Some("Hilda"),
      Some("2000-01-01"),
      222.22))),
    Some(Seq(PostCessationTradeReliefAndCertainOtherLosses(
      Some("myref"),
      Some("ACME Inc"),
      Some("2019-08-10"),
      Some("Widgets Manufacturer"),
      Some("AB12412/A12"),
      222.22))),
    Some(AnnualPaymentsMade(
      Some("myref"),
      763.00)),
    Some(Seq(QualifyingLoanInterestPayments(
      Some("myref"),
      Some("Maurice"),
      763.00)))
  )

  class Test extends MockHttpClient with MockAppConfig {

    val connector: AmendOtherReliefsConnector = new AmendOtherReliefsConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    MockAppConfig.ifsBaseUrl returns baseUrl
    MockAppConfig.ifsToken returns "ifs-token"
    MockAppConfig.ifsEnvironment returns "ifs-environment"
    MockAppConfig.ifsEnvironmentHeaders returns Some(allowedIfsHeaders)
  }

  "doConnector" must {

    val request: AmendOtherReliefsRequest = AmendOtherReliefsRequest(Nino(nino), taxYear, body)

    "put a body and return 204 no body" in new Test {
      val outcome = Right(ResponseWrapper(correlationId, ()))

      implicit val hc: HeaderCarrier = HeaderCarrier(otherHeaders = otherHeaders ++ Seq("Content-Type" -> "application/json"))
      val requiredHeadersPut: Seq[(String, String)] = requiredIfsHeaders ++ Seq("Content-Type" -> "application/json")

      MockedHttpClient
        .put(
          url = s"$baseUrl/income-tax/reliefs/other/$nino/$taxYear",
          config = dummyIfsHeaderCarrierConfig,
          body = body,
          requiredHeaders = requiredHeadersPut,
          excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
        ).returns(Future.successful(outcome))

      await(connector.amend(request)) shouldBe outcome
    }
  }
}
