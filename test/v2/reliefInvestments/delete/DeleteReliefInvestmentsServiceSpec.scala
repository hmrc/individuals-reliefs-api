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

package v2.reliefInvestments.delete

import common.RuleOutsideAmendmentWindowError
import shared.controllers.EndpointLogContext
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors.*
import shared.models.outcomes.ResponseWrapper
import shared.utils.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier
import v2.reliefInvestments.delete.def1.Def1_DeleteReliefInvestmentsRequestData
import v2.reliefInvestments.delete.model.DeleteReliefInvestmentsRequestData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeleteReliefInvestmentsServiceSpec extends UnitSpec with MockDeleteReliefInvestmentsConnector {

  val validNino: String              = "AA123456A"
  val validTaxYear: String           = "2019-20"
  implicit val correlationId: String = "X-123"

  val requestData: DeleteReliefInvestmentsRequestData = Def1_DeleteReliefInvestmentsRequestData(Nino(validNino), TaxYear.fromMtd(validTaxYear))

  trait Test {
    implicit val hc: HeaderCarrier              = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new DeleteReliefInvestmentsService(
      connector = mockConnector
    )

  }

  "service" when {
    "a service call is successful" should {
      "return a mapped result" in new Test {
        MockDeleteReliefInvestmentsConnector
          .delete(requestData)
          .returns(Future.successful(Right(ResponseWrapper("resultId", ()))))

        await(service.delete(requestData)) shouldBe Right(ResponseWrapper("resultId", ()))
      }
    }

    "a service call is unsuccessful" should {
      def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
        s"return ${error.code} error when $downstreamErrorCode error is returned from the connector" in new Test {

          MockDeleteReliefInvestmentsConnector
            .delete(requestData)
            .returns(Future.successful(Left(ResponseWrapper("resultId", DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

          await(service.delete(requestData)) shouldBe Left(ErrorWrapper("resultId", error))
        }

      val errors = Seq(
        ("NO_DATA_FOUND", NotFoundError),
        ("FORMAT_TAX_YEAR", TaxYearFormatError),
        ("OUTSIDE_AMENDMENT_WINDOW", RuleOutsideAmendmentWindowError),
        ("SERVER_ERROR", InternalError),
        ("SERVICE_UNAVAILABLE", InternalError),
        ("INVALID_TAXABLE_ENTITY_ID", NinoFormatError)
      )

      val extraTysErrors = Seq(
        ("INVALID_CORRELATION_ID", InternalError),
        ("TAX_YEAR_NOT_SUPPORTED", RuleTaxYearNotSupportedError)
      )

      (errors ++ extraTysErrors).foreach(args => (serviceError _).tupled(args))
    }
  }

}
