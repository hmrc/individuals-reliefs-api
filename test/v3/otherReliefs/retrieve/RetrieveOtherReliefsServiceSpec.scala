/*
 * Copyright 2025 HM Revenue & Customs
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

package v3.otherReliefs.retrieve

import shared.controllers.EndpointLogContext
import shared.models.domain.{Nino, TaxYear, Timestamp}
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import shared.utils.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier
import v3.otherReliefs.retrieve.def1.model.request.Def1_RetrieveOtherReliefsRequestData
import v3.otherReliefs.retrieve.def1.model.response._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveOtherReliefsServiceSpec extends UnitSpec {

  private val nino: String           = "ZG903729C"
  private val taxYear: String        = "2017-18"
  implicit val correlationId: String = "X-123"

  private val fullResponseModel = Def1_RetrieveOtherReliefsResponse(
    Timestamp("2020-06-17T10:53:38.000Z"),
    Some(NonDeductibleLoanInterest(Some("myref"), 763.00)),
    Some(PayrollGiving(Some("myref"), 154.00)),
    Some(QualifyingDistributionRedemptionOfSharesAndSecurities(Some("myref"), 222.22)),
    Some(Seq(MaintenancePayments(Some("myref"), Some("Hilda"), Some("2000-01-01"), 222.22))),
    Some(
      Seq(
        PostCessationTradeReliefAndCertainOtherLosses(
          Some("myref"),
          Some("ACME Inc"),
          Some("2019-08-10"),
          Some("Widgets Manufacturer"),
          Some("AB12412/A12"),
          222.22))),
    Some(AnnualPaymentsMade(Some("myref"), 763.00)),
    Some(Seq(QualifyingLoanInterestPayments(Some("myref"), Some("Maurice"), 763.00)))
  )

  private val requestData = Def1_RetrieveOtherReliefsRequestData(Nino(nino), TaxYear.fromMtd(taxYear))

  trait Test extends MockRetrieveOtherReliefsConnector {
    implicit val hc: HeaderCarrier              = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new RetrieveOtherReliefsService(
      connector = mockConnector
    )

  }

  "service" should {
    "return a successful response" when {
      "a successful response is passed through" in new Test {
        MockRetrieveOtherReliefsConnector
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, fullResponseModel))))

        await(service.retrieve(requestData)) shouldBe Right(ResponseWrapper(correlationId, fullResponseModel))
      }
    }

    "map errors according to spec" when {
      def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
        s"a $downstreamErrorCode error is returned from the service" in new Test {

          MockRetrieveOtherReliefsConnector
            .retrieve(requestData)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

          await(service.retrieve(requestData)) shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val errors = Seq(
        ("INVALID_TAXABLE_ENTITY_ID", NinoFormatError),
        ("INVALID_TAX_YEAR", TaxYearFormatError),
        ("NO_DATA_FOUND", NotFoundError),
        "INVALID_CORRELATIONID" -> InternalError,
        ("SERVER_ERROR", InternalError),
        ("SERVICE_UNAVAILABLE", InternalError)
      )

      val extraTysErrors = Seq(
        "INVALID_CORRELATION_ID" -> InternalError,
        "TAX_YEAR_NOT_SUPPORTED" -> RuleTaxYearNotSupportedError
      )

      (errors ++ extraTysErrors).foreach(args => (serviceError _).tupled(args))
    }
  }

}
