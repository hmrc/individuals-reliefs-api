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

package v2.deleteCharitableGivingReliefs

import common.RuleOutsideAmendmentWindowError
import shared.controllers.EndpointLogContext
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import shared.utils.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier
import v2.deleteCharitableGivingReliefs.model.request.Def1_DeleteCharitableGivingTaxReliefsRequestData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeleteCharitableGivingTaxReliefsServiceSpec extends UnitSpec {

  "service" when {

    "a service call is successful" should {
      "return a mapped result" in new Test {
        MockedDeleteCharitableGivingReliefConnector
          .delete(requestData)
          .returns(Future.successful(Right(ResponseWrapper("resultId", ()))))

        await(service.delete(requestData)) shouldBe Right(ResponseWrapper("resultId", ()))
      }
    }

    "a service call is unsuccessful" should {
      def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
        s"return ${error.code} error when $downstreamErrorCode error is returned from the connector" in new Test {

          MockedDeleteCharitableGivingReliefConnector
            .delete(requestData)
            .returns(Future.successful(Left(ResponseWrapper("resultId", DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

          await(service.delete(requestData)) shouldBe Left(ErrorWrapper("resultId", error))
        }

      val errors = List(
        ("INVALID_NINO", NinoFormatError),
        ("INVALID_TYPE", InternalError),
        ("INVALID_TAXYEAR", TaxYearFormatError),
        ("OUTSIDE_AMENDMENT_WINDOW", RuleOutsideAmendmentWindowError),
        ("INVALID_PAYLOAD", InternalError),
        ("NOT_FOUND_INCOME_SOURCE", NotFoundError),
        ("MISSING_CHARITIES_NAME_GIFT_AID", InternalError),
        ("MISSING_CHARITIES_NAME_INVESTMENT", InternalError),
        ("MISSING_INVESTMENT_AMOUNT", InternalError),
        ("INVALID_ACCOUNTING_PERIOD", RuleTaxYearNotSupportedError),
        ("SERVER_ERROR", InternalError),
        ("SERVICE_UNAVAILABLE", InternalError),
        ("GONE", NotFoundError),
        ("NOT_FOUND", NotFoundError)
      )

      val extraTysErrors = List(
        ("INVALID_INCOMESOURCE_ID", InternalError),
        ("INVALID_INCOMESOURCE_TYPE", InternalError),
        ("INVALID_TAX_YEAR", TaxYearFormatError),
        ("TAX_YEAR_NOT_SUPPORTED", RuleTaxYearNotSupportedError),
        ("PERIOD_NOT_FOUND", NotFoundError),
        ("PERIOD_ALREADY_DELETED", NotFoundError),
        ("INCOME_SOURCE_DATA_NOT_FOUND", NotFoundError),
        ("INVALID_CORRELATION_ID", InternalError)
      )

      (errors ++ extraTysErrors).foreach(args => (serviceError _).tupled(args))
    }
  }

  trait Test extends MockDeleteCharitableGivingReliefConnector {
    implicit protected val hc: HeaderCarrier              = HeaderCarrier()
    implicit protected val logContext: EndpointLogContext = EndpointLogContext("c", "ep")
    implicit protected val correlationId: String          = "X-123"

    val validNino    = "AA123456A"
    val validTaxYear = "2019-20"

    protected val requestData: Def1_DeleteCharitableGivingTaxReliefsRequestData =
      Def1_DeleteCharitableGivingTaxReliefsRequestData(Nino(validNino), TaxYear.fromMtd(validTaxYear))

    protected val service = new DeleteCharitableGivingTaxReliefsService(
      connector = mockConnector
    )

  }

}
