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

package v1.retrieveCharitableGivingReliefs

import api.connectors.{ConnectorSpec, DownstreamOutcome}
import api.models.domain.{Nino, TaxYear}
import api.models.outcomes.ResponseWrapper
import play.api.Configuration
import v1.retrieveCharitableGivingReliefs.def1.model.response.{Def1_GiftAidPayments, Def1_Gifts, Def1_NonUkCharities}
import v1.retrieveCharitableGivingReliefs.model.request.Def1_RetrieveCharitableGivingReliefsRequestData
import v1.retrieveCharitableGivingReliefs.model.response.{Def1_RetrieveCharitableGivingReliefsResponse, RetrieveCharitableGivingReliefsResponse}

import scala.concurrent.Future

class RetrieveCharitableGivingReliefsConnectorSpec extends ConnectorSpec {

  private val nino = "AA123456A"

  private val nonUkCharitiesModel = Def1_NonUkCharities(
    charityNames = Some(Seq("non-UK charity 1", "non-UK charity 2")),
    totalAmount = 1000.10
  )

  private val giftAidPaymentsModel = Def1_GiftAidPayments(
    nonUkCharities = Some(nonUkCharitiesModel),
    totalAmount = Some(1000.11),
    oneOffAmount = Some(1000.12),
    amountTreatedAsPreviousTaxYear = Some(1000.13),
    amountTreatedAsSpecifiedTaxYear = Some(1000.14)
  )

  private val giftsModel = Def1_Gifts(
    nonUkCharities = Some(nonUkCharitiesModel),
    landAndBuildings = Some(1000.15),
    sharesOrSecurities = Some(1000.16)
  )

  private val response = Def1_RetrieveCharitableGivingReliefsResponse(
    giftAidPayments = Some(giftAidPaymentsModel),
    gifts = Some(giftsModel)
  )

  "RetrieveCharitableGivingReliefConnector" when {
    "retrieve" must {
      "return a 200 status for a success scenario with desIf_Migration disabled" in new DesTest with Test {
        MockedAppConfig.featureSwitchConfig returns Configuration("desIf_Migration.enabled" -> false)

        val outcome = Right(ResponseWrapper(correlationId, response))
        def taxYear = TaxYear.fromMtd("2018-19")

        willGet(url = s"$baseUrl/income-tax/nino/$nino/income-source/charity/annual/${taxYear.asDownstream}")
          .returns(Future.successful(outcome))

        val result: DownstreamOutcome[RetrieveCharitableGivingReliefsResponse] = await(connector.retrieve(request))
        result shouldBe outcome
      }

      "return a 200 status for a success scenario with desIf_Migration enabled" in new IfsTest with Test {
        MockedAppConfig.featureSwitchConfig returns Configuration("desIf_Migration.enabled" -> true)

        val outcome = Right(ResponseWrapper(correlationId, response))

        def taxYear = TaxYear.fromMtd("2018-19")

        willGet(url = s"$baseUrl/income-tax/nino/$nino/income-source/charity/annual/${taxYear.asDownstream}")
          .returns(Future.successful(outcome))

        val result: DownstreamOutcome[RetrieveCharitableGivingReliefsResponse] = await(connector.retrieve(request))
        result shouldBe outcome
      }
    }

    "retrieveCharitableGivingRelief is called for a TaxYearSpecific tax year" must {
      "return a 200 for success scenario" in {
        new TysIfsTest with Test {
          def taxYear: TaxYear = TaxYear.fromMtd("2023-24")

          val outcome = Right(ResponseWrapper(correlationId, response))

          willGet(s"$baseUrl/income-tax/${taxYear.asTysDownstream}/$nino/income-source/charity/annual")
            .returns(Future.successful(outcome))

          await(connector.retrieve(request)) shouldBe outcome
        }
      }
    }
  }

  trait Test {
    _: ConnectorTest =>

    protected def taxYear: TaxYear

    val connector: RetrieveCharitableGivingReliefsConnector =
      new RetrieveCharitableGivingReliefsConnector(
        http = mockHttpClient,
        appConfig = mockAppConfig
      )

    protected val request: Def1_RetrieveCharitableGivingReliefsRequestData =
      Def1_RetrieveCharitableGivingReliefsRequestData(
        nino = Nino(nino),
        taxYear = taxYear
      )

  }

}
