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
import uk.gov.hmrc.http.HeaderCarrier
import v1.models.domain.Nino
import v1.mocks.MockHttpClient
import v1.models.outcomes.ResponseWrapper
import v1.models.request.amendForeignReliefs._

import scala.concurrent.Future

class AmendForeignReliefsConnectorSpec extends ConnectorSpec {

  val taxYear: String    = "2017-18"
  val nino: String       = "AA123456A"
  val amount: BigDecimal = 1234.56

  val body: AmendForeignReliefsBody = AmendForeignReliefsBody(
    foreignTaxCreditRelief = Some(
      ForeignTaxCreditRelief(
        amount = amount
      )),
    foreignIncomeTaxCreditRelief = Some(
      Seq(
        ForeignIncomeTaxCreditRelief(
          countryCode = "FRA",
          foreignTaxPaid = Some(amount),
          taxableAmount = amount,
          employmentLumpSum = true
        ))),
    foreignTaxForFtcrNotClaimed = Some(
      ForeignTaxForFtcrNotClaimed(
        amount = amount
      ))
  )

  class Test extends MockHttpClient with MockAppConfig {

    val connector: AmendForeignReliefsConnector = new AmendForeignReliefsConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    MockAppConfig.ifsBaseUrl returns baseUrl
    MockAppConfig.ifsToken returns "ifs-token"
    MockAppConfig.ifsEnvironment returns "ifs-environment"
    MockAppConfig.ifsEnvironmentHeaders returns Some(allowedIfsHeaders)
  }

  "doConnector" must {

    val request: AmendForeignReliefsRequest = AmendForeignReliefsRequest(Nino(nino), taxYear, body)

    "put a body and return 204 no body" in new Test {
      val outcome = Right(ResponseWrapper(correlationId, ()))

      implicit val hc: HeaderCarrier                = HeaderCarrier(otherHeaders = otherHeaders ++ Seq("Content-Type" -> "application/json"))
      val requiredHeadersPut: Seq[(String, String)] = requiredIfsHeaders ++ Seq("Content-Type" -> "application/json")

      MockedHttpClient
        .put(
          url = s"$baseUrl/income-tax/reliefs/foreign/$nino/$taxYear",
          config = dummyIfsHeaderCarrierConfig,
          body = body,
          requiredHeaders = requiredHeadersPut,
          excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
        )
        .returns(Future.successful(outcome))

      await(connector.amend(request)) shouldBe outcome
    }
  }

}
