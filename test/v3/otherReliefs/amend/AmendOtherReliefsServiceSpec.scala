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

package v3.otherReliefs.amend

import common.RuleSubmissionFailedError
import shared.controllers.EndpointLogContext
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import shared.utils.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier
import v3.otherReliefs.amend.def1.model.request.{Def1_AmendOtherReliefsRequestBody, Def1_AmendOtherReliefsRequestData}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AmendOtherReliefsServiceSpec extends UnitSpec {

  "service" when {
    "service call successful" must {
      "return mapped result" in new Test {
        MockAmendOtherReliefsConnector
          .amend(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        await(service.amend(requestData)) shouldBe Right(ResponseWrapper(correlationId, ()))
      }
    }
  }

  "unsuccessful" must {
    "map errors according to spec" when {

      def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
        s"a $downstreamErrorCode error is returned from the service" in new Test {

          MockAmendOtherReliefsConnector
            .amend(requestData)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

          await(service.amend(requestData)) shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val errors = Seq(
        "INVALID_TAXABLE_ENTITY_ID"        -> NinoFormatError,
        "INVALID_TAX_YEAR"                 -> TaxYearFormatError,
        "INVALID_CORRELATIONID"            -> InternalError,
        "BUSINESS_VALIDATION_RULE_FAILURE" -> RuleSubmissionFailedError,
        "SERVER_ERROR"                     -> InternalError,
        "SERVICE_UNAVAILABLE"              -> InternalError
      )

      val extraTysErrors = Seq(
        "INVALID_CORRELATION_ID" -> InternalError,
        "INVALID_PAYLOAD"        -> InternalError,
        "TAX_YEAR_NOT_SUPPORTED" -> RuleTaxYearNotSupportedError,
        "UNPROCESSABLE_ENTITY"   -> InternalError
      )

      (errors ++ extraTysErrors).foreach(args => (serviceError _).tupled(args))
    }
  }

  trait Test extends MockAmendOtherReliefsConnector {
    implicit val hc: HeaderCarrier              = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new AmendOtherReliefsService(
      connector = mockAmendOtherReliefsConnector
    )

    val taxYear: String                = "2017-18"
    val nino: String                   = "ZG903729C"
    implicit val correlationId: String = "X-123"

    val body: Def1_AmendOtherReliefsRequestBody = Def1_AmendOtherReliefsRequestBody(None, None, None, None, None, None, None)

    protected val requestData: Def1_AmendOtherReliefsRequestData = Def1_AmendOtherReliefsRequestData(Nino(nino), TaxYear.fromMtd(taxYear), body)

  }

}
