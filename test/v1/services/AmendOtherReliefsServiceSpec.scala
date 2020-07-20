/*
 * Copyright 2020 HM Revenue & Customs
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

import support.UnitSpec
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import v1.controllers.EndpointLogContext
import v1.mocks.connectors.MockAmendOtherReliefsConnector
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.amendOtherReliefs._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class AmendOtherReliefsServiceSpec extends UnitSpec {

  val taxYear = "2017-18"
  val nino = Nino("AA123456A")
  private val correlationId = "X-123"

  val body = AmendOtherReliefsBody(
    Some(NonDeductableLoanInterest(
      Some("myref"),
      763.00)),
    Some(PayrollGiving(
      Some("myref"),
      154.00)),
    Some(QualifyingDistributionRedemptionOfSharesAndSecurities(
      Some("myref"),
      222.22)),
    Some(Seq(MaintenancePayments(
      Some("myRef"),
      Some("Hilda"),
      Some("2000-01-01"),
      Some(222.22)))),
    Some(Seq(PostCessationTradeReliefAndCertainOtherLosses(
      Some("myRef"),
      Some("ACME Inc"),
      Some("2019-08-10"),
      Some("Widgets Manufacturer"),
      Some("AB12412/A12"),
      Some(222.22)))),
    Some(AnnualPaymentsMade(
      Some("myref"),
      763.00)),
    Some(Seq(QualifyingLoanInterestPayments(
      Some("myRef"),
      Some("Maurice"),
      763.00)))
  )

  private val requestData = AmendOtherReliefsRequest(nino, taxYear, body)

  trait Test extends MockAmendOtherReliefsConnector {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new AmendOtherReliefsService(
      connector = mockAmendOtherReliefsConnector
    )
  }

  "service" when {
    "service call successful" must {
      "return mapped result" in new Test {
        MockAmendOtherReliefsConnector.amend(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        await(service.amend(requestData)) shouldBe Right(ResponseWrapper(correlationId, ()))
      }
    }
  }

  "unsuccessful" must {
    "map errors according to spec" when {

      def serviceError(desErrorCode: String, error: MtdError): Unit =
        s"a $desErrorCode error is returned from the service" in new Test {

          MockAmendOtherReliefsConnector.amend(requestData)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode(desErrorCode))))))

          await(service.amend(requestData)) shouldBe Left(ErrorWrapper(Some(correlationId), error))
        }

      val input = Seq(
        "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
        "FORMAT_TAX_YEAR" -> TaxYearFormatError,
        "NOT_FOUND" -> NotFoundError,
        "SERVER_ERROR" -> DownstreamError,
        "SERVICE_UNAVAILABLE" -> DownstreamError
      )

      input.foreach(args => (serviceError _).tupled(args))
    }
  }
}
