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

package v1.services

import api.controllers.EndpointLogContext
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.ServiceSpec
import v1.mocks.connectors.MockCreateAndAmendCharitableGivingTaxReliefConnector
import v1.models.request.createAndAmendCharitableGivingTaxRelief._

import scala.concurrent.Future

class CreateAndAmendCharitableGivingTaxReliefServiceSpec extends ServiceSpec {
  private val nino: String    = "AA123456A"
  private val taxYear: String = "2017-18"

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

  val requestData: CreateAndAmendCharitableGivingTaxReliefRequest =
    CreateAndAmendCharitableGivingTaxReliefRequest(Nino(nino), TaxYear.fromMtd(taxYear), requestBody)

  trait Test extends MockCreateAndAmendCharitableGivingTaxReliefConnector {
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new CreateAndAmendCharitableGivingTaxReliefService(
      connector = mockConnector
    )

  }

  "service" when {
    "service call successful" must {
      "return mapped result" in new Test {
        MockCreateAndAmendCharitableGivingTaxReliefConnector
          .createAmend(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        await(service.amend(requestData)) shouldBe Right(ResponseWrapper(correlationId, ()))
      }
    }

    "unsuccessful" must {
      "map errors according to spec" when {

        def serviceError(desErrorCode: String, error: MtdError): Unit =
          s"a $desErrorCode error is returned from the service" in new Test {
            MockCreateAndAmendCharitableGivingTaxReliefConnector
              .createAmend(requestData)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(desErrorCode))))))

            await(service.amend(requestData)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val errors = Seq(
          ("INVALID_NINO", NinoFormatError),
          ("INVALID_TYPE", InternalError),
          ("INVALID_TAXYEAR", TaxYearFormatError),
          ("INVALID_PAYLOAD", InternalError),
          ("NOT_FOUND_INCOME_SOURCE", NotFoundError),
          ("MISSING_CHARITIES_NAME_GIFT_AID", RuleGiftAidNonUkAmountWithoutNamesError),
          ("MISSING_GIFT_AID_AMOUNT", InternalError),
          ("MISSING_CHARITIES_NAME_INVESTMENT", RuleGiftsNonUkAmountWithoutNamesError),
          ("MISSING_INVESTMENT_AMOUNT", InternalError),
          ("INVALID_ACCOUNTING_PERIOD", RuleTaxYearNotSupportedError),
          ("SERVER_ERROR", InternalError),
          ("SERVICE_UNAVAILABLE", InternalError),
          ("GONE", InternalError),
          ("NOT_FOUND", NotFoundError)
        )

        val extraTysErrors = Seq(
          ("INVALID_INCOMESOURCE_TYPE", InternalError),
          ("INVALID_TAX_YEAR", TaxYearFormatError),
          ("INVALID_CORRELATIONID", InternalError),
          ("INCOME_SOURCE_NOT_FOUND", NotFoundError),
          ("INCOMPATIBLE_INCOME_SOURCE", InternalError),
          ("TAX_YEAR_NOT_SUPPORTED", RuleTaxYearNotSupportedError)
        )

        (errors ++ extraTysErrors).foreach(args => (serviceError _).tupled(args))
      }
    }
  }

}
