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

import support.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier
import v1.controllers.EndpointLogContext
import v1.mocks.connectors.MockDeleteCharitableGivingTaxReliefConnector
import v1.models.domain.Nino
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.TaxYear
import v1.models.request.deleteCharitableGivingTaxRelief.DeleteCharitableGivingTaxReliefRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeleteCharitableGivingTaxReliefServiceSpec extends UnitSpec {

  val validNino: String              = "AA123456A"
  val validTaxYear: String           = "2019-20"
  implicit val correlationId: String = "X-123"

  val requestData: DeleteCharitableGivingTaxReliefRequest = DeleteCharitableGivingTaxReliefRequest(Nino(validNino), TaxYear.fromMtd(validTaxYear))

  trait Test extends MockDeleteCharitableGivingTaxReliefConnector {
    implicit val hc: HeaderCarrier              = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new DeleteCharitableGivingTaxReliefService(
      connector = mockConnector
    )

  }

  "service" when {
    "a service call is successful" should {
      "return a mapped result" in new Test {
        MockDeleteCharitableGivingTaxReliefConnector
          .delete(requestData)
          .returns(Future.successful(Right(ResponseWrapper("resultId", ()))))

        await(service.delete(requestData)) shouldBe Right(ResponseWrapper("resultId", ()))
      }
    }
    "a service call is unsuccessful" should {
      def serviceError(desErrorCode: String, error: MtdError): Unit =
        s"return ${error.code} error when $desErrorCode error is returned from the connector" in new Test {

          MockDeleteCharitableGivingTaxReliefConnector
            .delete(requestData)
            .returns(Future.successful(Left(ResponseWrapper("resultId", DesErrors.single(DesErrorCode(desErrorCode))))))

          await(service.delete(requestData)) shouldBe Left(ErrorWrapper("resultId", error))
        }

      val input = Seq(
        ("INVALID_NINO", NinoFormatError),
        ("INVALID_TYPE", DownstreamError),
        ("INVALID_TAXYEAR", TaxYearFormatError),
        ("INVALID_PAYLOAD", DownstreamError),
        ("NOT_FOUND_INCOME_SOURCE", NotFoundError),
        ("MISSING_CHARITIES_NAME_GIFT_AID", DownstreamError),
        ("MISSING_CHARITIES_NAME_INVESTMENT", DownstreamError),
        ("MISSING_INVESTMENT_AMOUNT", DownstreamError),
        ("INVALID_ACCOUNTING_PERIOD", RuleTaxYearNotSupportedError),
        ("SERVER_ERROR", DownstreamError),
        ("SERVICE_UNAVAILABLE", DownstreamError),
        ("GONE", NotFoundError),
        ("NOT_FOUND", NotFoundError)
      )

      input.foreach(args => (serviceError _).tupled(args))
    }
  }

}
