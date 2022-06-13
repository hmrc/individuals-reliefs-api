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

package v1.services

import v1.controllers.EndpointLogContext
import v1.mocks.connectors.MockRetrieveCharitableGivingReliefConnector
import v1.models.domain.Nino
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.TaxYear
import v1.models.request.retrieveCharitableGivingTaxRelief.RetrieveCharitableGivingReliefRequest
import v1.models.response.retrieveCharitableGivingTaxRelief._

import scala.concurrent.Future

class RetrieveCharitableGivingReliefServiceSpec extends ServiceSpec {

  private val nino    = "AA123456A"
  private val taxYear = "2017-18"

  private val request = RetrieveCharitableGivingReliefRequest(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(taxYear)
  )

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

  trait Test extends MockRetrieveCharitableGivingReliefConnector {
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new RetrieveCharitableGivingReliefService(
      connector = mockConnector
    )

  }

  "RetrieveCharitableGivingReliefService" when {
    "service call successful" should {
      "return correct result for a success" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, response))

        MockRetrieveCharitableGivingReliefConnector
          .retrieve(request)
          .returns(Future.successful(outcome))

        await(service.retrieve(request)) shouldBe outcome
      }
    }

    "unsuccessful" should {
      "map errors according to spec" when {
        def serviceError(desErrorCode: String, error: MtdError): Unit =
          s"a $desErrorCode error is returned from the service" in new Test {

            MockRetrieveCharitableGivingReliefConnector
              .retrieve(request)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode(desErrorCode))))))

            await(service.retrieve(request)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val input = Seq(
          ("INVALID_NINO", NinoFormatError),
          ("INVALID_TYPE", DownstreamError),
          ("INVALID_TAXYEAR", TaxYearFormatError),
          ("INVALID_INCOME_SOURCE", DownstreamError),
          ("NOT_FOUND_PERIOD", NotFoundError),
          ("NOT_FOUND_INCOME_SOURCE", NotFoundError),
          ("SERVER_ERROR", DownstreamError),
          ("SERVICE_UNAVAILABLE", DownstreamError)
        )

        input.foreach(args => (serviceError _).tupled(args))
      }
    }
  }

}
