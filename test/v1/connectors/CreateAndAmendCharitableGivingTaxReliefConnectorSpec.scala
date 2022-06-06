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
import v1.mocks.MockHttpClient
import v1.models.domain.Nino
import v1.models.outcomes.ResponseWrapper
import v1.models.request.DesTaxYear
import v1.models.request.createAndAmendCharitableGivingTaxRelief.{CreateAndAmendCharitableGivingTaxReliefBody, CreateAndAmendCharitableGivingTaxReliefRequest, GiftAidPayments, Gifts, NonUkCharities}

import scala.concurrent.Future

class CreateAndAmendCharitableGivingTaxReliefConnectorSpec extends ConnectorSpec {

  val taxYearMtd: String        = "2017-18"
  val taxYearDownstream: String = "2018"
  val nino: String              = "AA123456A"

  val nonUkCharitiesModel: NonUkCharities =
    NonUkCharities(
      charityNames = Some(Seq("non-UK charity 1", "non-UK charity 2")),
      totalAmount = 1000.12
    )

  val giftAidModel: GiftAidPayments =
    GiftAidPayments(
      nonUkCharities = Some(nonUkCharitiesModel),
      totalAmount = Some(1000.12),
      oneOffAmount = Some(1000.12),
      amountTreatedAsPreviousTaxYear = Some(1000.12),
      amountTreatedAsSpecifiedTaxYear = Some(1000.12)
    )

  val giftModel: Gifts =
    Gifts(
      nonUkCharities = Some(nonUkCharitiesModel),
      landAndBuildings = Some(1000.12),
      sharesOrSecurities = Some(1000.12)
    )

  val requestBody: CreateAndAmendCharitableGivingTaxReliefBody =
    CreateAndAmendCharitableGivingTaxReliefBody(
      giftAidPayments = Some(giftAidModel),
      gifts = Some(giftModel)
    )

  class Test extends MockHttpClient with MockAppConfig {

    val connector: CreateAndAmendCharitableGivingTaxReliefConnector = new CreateAndAmendCharitableGivingTaxReliefConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    MockAppConfig.desBaseUrl returns baseUrl
    MockAppConfig.desToken returns "des-token"
    MockAppConfig.desEnvironment returns "des-environment"
    MockAppConfig.desEnvironmentHeaders returns Some(allowedDesHeaders)
  }

  "doConnector" must {

    val request: CreateAndAmendCharitableGivingTaxReliefRequest = CreateAndAmendCharitableGivingTaxReliefRequest(Nino(nino), DesTaxYear.fromMtd(taxYearMtd), requestBody)

    "return 200 for a success scenario" in new Test {
      val outcome = Right(ResponseWrapper(correlationId, ()))

      implicit val hc: HeaderCarrier                = HeaderCarrier(otherHeaders = otherHeaders ++ Seq("Content-Type" -> "application/json"))
      val requiredHeadersPost: Seq[(String, String)] = requiredDesHeaders ++ Seq("Content-Type" -> "application/json")

      MockedHttpClient
        .post(
          url = s"$baseUrl/income-tax/nino/$nino/income-source/charity/annual/$taxYearDownstream",
          config = dummyDesHeaderCarrierConfig,
          body = requestBody,
          requiredHeaders = requiredHeadersPost,
          excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
        )
        .returns(Future.successful(outcome))

      await(connector.createAmend(request)) shouldBe outcome
    }
  }

}