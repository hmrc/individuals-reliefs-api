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

package v1.CreateAndAmendCharitableGivingReliefs

import api.connectors.{ConnectorSpec, DownstreamOutcome}
import api.models.domain.{Nino, TaxYear}
import api.models.outcomes.ResponseWrapper
import play.api.Configuration
import v1.CreateAndAmendCharitableGivingReliefs.def1.model.request._
import scala.concurrent.Future

class CreateAndAmendCharitableGivingTaxReliefsConnectorSpec extends ConnectorSpec {

  val taxYearMtd: String        = "2017-18"
  val taxYearDownstream: String = "2018"
  val nino: String              = "AA123456A"

  val nonUkCharitiesModel: Def1_NonUkCharities =
    Def1_NonUkCharities(
      charityNames = Some(Seq("non-UK charity 1", "non-UK charity 2")),
      totalAmount = 1000.12
    )

  val giftAidModel: Def1_GiftAidPayments =
    Def1_GiftAidPayments(
      nonUkCharities = Some(nonUkCharitiesModel),
      totalAmount = Some(1000.12),
      oneOffAmount = Some(1000.12),
      amountTreatedAsPreviousTaxYear = Some(1000.12),
      amountTreatedAsSpecifiedTaxYear = Some(1000.12)
    )

  val giftModel: Def1_Gifts =
    Def1_Gifts(
      nonUkCharities = Some(nonUkCharitiesModel),
      landAndBuildings = Some(1000.12),
      sharesOrSecurities = Some(1000.12)
    )

  val requestBody: Def1_CreateAndAmendCharitableGivingTaxReliefsBody =
    Def1_CreateAndAmendCharitableGivingTaxReliefsBody(
      giftAidPayments = Some(giftAidModel),
      gifts = Some(giftModel)
    )

  "CreateAndAmendCharitableGivingTaxReliefConnector" when {
    "createOrAmendCharitableGivingTaxRelief is called" must {
      "return 200 for a success scenario with desIf_Migration disabled" in new DesTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2019-20")

        MockedAppConfig.featureSwitches returns Configuration("desIf_Migration.enabled" -> false)

        willPost(url = s"$baseUrl/income-tax/nino/$nino/income-source/charity/annual/${taxYear.asDownstream}", body = requestBody)
          .returns(Future.successful(outcome))

        val result: DownstreamOutcome[Unit] = await(connector.createAmend(request))
        result shouldBe outcome
      }

      "return 200 for a success scenario with desIf_Migration enabled" in new IfsTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2019-20")

        MockedAppConfig.featureSwitches returns Configuration("desIf_Migration.enabled" -> true)

        willPost(url = s"$baseUrl/income-tax/nino/$nino/income-source/charity/annual/${taxYear.asDownstream}", body = requestBody)
          .returns(Future.successful(outcome))

        val result: DownstreamOutcome[Unit] = await(connector.createAmend(request))
        result shouldBe outcome
      }
    }

    "createOrAmendCharitableGivingTaxRelief is called for a TYS tax year" must {
      "return 200 for a success scenario" in new TysIfsTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2023-24")

        willPost(url = s"$baseUrl/income-tax/${taxYear.asTysDownstream}/$nino/income-source/charity/annual", body = requestBody)
          .returns(Future.successful(outcome))

        val result: DownstreamOutcome[Unit] = await(connector.createAmend(request))
        result shouldBe outcome
      }
    }

  }

  trait Test { _: ConnectorTest =>
    def taxYear: TaxYear

    protected val connector: CreateAndAmendCharitableGivingTaxReliefsConnector =
      new CreateAndAmendCharitableGivingTaxReliefsConnector(http = mockHttpClient, appConfig = mockAppConfig)

    protected val request: Def1_CreateAndAmendCharitableGivingTaxReliefsRequestData =
      Def1_CreateAndAmendCharitableGivingTaxReliefsRequestData(Nino(nino), taxYear, requestBody)

    protected val outcome = Right(ResponseWrapper(correlationId, ()))

  }

}
