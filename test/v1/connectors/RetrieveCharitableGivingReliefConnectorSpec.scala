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
import v1.models.request.retrieveCharitableGivingTaxRelief.RetrieveCharitableGivingReliefRequest
import v1.models.response.retrieveCharitableGivingTaxRelief._

import scala.concurrent.Future

class RetrieveCharitableGivingReliefConnectorSpec extends ConnectorSpec {

  private val nino              = "AA123456A"


  private val nonUkCharitiesModel = NonUkCharities(
    charityNames = Some(Seq("non-UK charity 1", "non-UK charity 2")),
    totalAmount = 1000.10
  )

  private val giftAidPaymentsModel = GiftAidPayments(
    nonUkCharities = Some(nonUkCharitiesModel),
    totalAmount = Some(1000.11),
    oneOffAmount = Some(1000.12),
    amountTreatedAsPreviousTaxYear = Some(1000.13),
    amountTreatedAsSpecifiedTaxYear = Some(1000.14)
  )

  private val giftsModel = Gifts(
    nonUkCharities = Some(nonUkCharitiesModel),
    landAndBuildings = Some(1000.15),
    sharesOrSecurities = Some(1000.16)
  )

  private val response = RetrieveCharitableGivingReliefResponse(
    giftAidPayments = Some(giftAidPaymentsModel),
    gifts = Some(giftsModel)
  )

  trait Test { _: ConnectorTest =>

    def taxYear: TaxYear

    val connector: RetrieveCharitableGivingReliefConnector = new RetrieveCharitableGivingReliefConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    protected val request = RetrieveCharitableGivingReliefRequest(
      nino = Nino(nino),
      taxYear = taxYear
    )

  }

  "RetrieveCharitableGivingReliefConnector" when {
    "retrieve" must {
      "return a 200 status for a success scenario" in new DesTest with Test {
        val outcome = Right(ResponseWrapper(correlationId, response))
        def taxYear = TaxYear.fromMtd("2018-19")

        willGet(
          url = s"$baseUrl/income-tax/nino/$nino/income-source/charity/annual/${taxYear.asDownstream}"
        )
          .returns(Future.successful(outcome))

        await(connector.retrieve(request)) shouldBe outcome
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

}
