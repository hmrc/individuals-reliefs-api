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
import v1.models.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import v1.controllers.EndpointLogContext
import v1.mocks.connectors.MockAmendPensionsReliefsConnector
import v1.models.errors.{DesErrorCode, DesErrors, DownstreamError, ErrorWrapper, MtdError, NinoFormatError, TaxYearFormatError}
import v1.models.outcomes.ResponseWrapper
import v1.models.request.TaxYear
import v1.models.request.amendPensionsReliefs.{AmendPensionsReliefsBody, AmendPensionsReliefsRequest, PensionReliefs}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AmendPensionsReliefsServiceSpec extends UnitSpec {

  val taxYear: String                = "2017-18"
  val nino: String                   = "AA123456A"
  implicit val correlationId: String = "X-123"

  val body: AmendPensionsReliefsBody = AmendPensionsReliefsBody(
    PensionReliefs(
      Some(1999.99),
      Some(1999.99),
      Some(1999.99),
      Some(1999.99),
      Some(1999.99)
    )
  )

  private val requestData = AmendPensionsReliefsRequest(Nino(nino), TaxYear.fromMtd(taxYear), body)

  trait Test extends MockAmendPensionsReliefsConnector {
    implicit val hc: HeaderCarrier              = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new AmendPensionsReliefsService(
      connector = mockAmendPensionsReliefsConnector
    )

  }

  "service" when {
    "service call successful" must {
      "return mapped result" in new Test {
        MockAmendPensionsReliefsConnector
          .amend(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        await(service.amend(requestData)) shouldBe Right(ResponseWrapper(correlationId, ()))
      }
    }
  }

  "unsuccessful" must {
    "map errors according to spec" when {

      def serviceError(desErrorCode: String, error: MtdError): Unit =
        s"a $desErrorCode error is returned from the service" in new Test {

          MockAmendPensionsReliefsConnector
            .amend(requestData)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode(desErrorCode))))))

          await(service.amend(requestData)) shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val input = Seq(
        "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
        "INVALID_TAX_YEAR"          -> TaxYearFormatError,
        "INVALID_PAYLOAD"           -> DownstreamError,
        "SERVER_ERROR"              -> DownstreamError,
        "SERVICE_UNAVAILABLE"       -> DownstreamError
      )

      input.foreach(args => (serviceError _).tupled(args))
    }
  }

}
