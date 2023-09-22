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
import api.models.domain.{Nino, TaxYear, Timestamp}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import support.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier
import v1.connectors.MockRetrieveForeignReliefsConnector
import v1.models.request.retrieveForeignReliefs.RetrieveForeignReliefsRequestData
import v1.models.response.retrieveForeignReliefs.{
  ForeignIncomeTaxCreditRelief,
  ForeignTaxCreditRelief,
  ForeignTaxForFtcrNotClaimed,
  RetrieveForeignReliefsResponse
}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveForeignReliefsServiceSpec extends UnitSpec {

  private val nino: String           = "AA123456A"
  private val taxYear: String        = "2017-18"
  implicit val correlationId: String = "X-123"

  private val responseModel = RetrieveForeignReliefsResponse(
    Timestamp("2020-06-17T10:53:38.000Z"),
    Some(ForeignTaxCreditRelief(234567.89)),
    Some(Seq(ForeignIncomeTaxCreditRelief("FRA", Some(540.32), 204.78, false))),
    Some(ForeignTaxForFtcrNotClaimed(549.98))
  )

  private val requestData = RetrieveForeignReliefsRequestData(Nino(nino), TaxYear.fromMtd(taxYear))

  trait Test extends MockRetrieveForeignReliefsConnector {
    implicit val hc: HeaderCarrier              = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new RetrieveForeignReliefsService(
      connector = mockConnector
    )

  }

  "service" should {
    "return a successful response" when {
      "a successful response is passed through" in new Test {
        MockRetrieveForeignReliefsConnector
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseModel))))

        await(service.retrieve(requestData)) shouldBe Right(ResponseWrapper(correlationId, responseModel))
      }
    }
    "map errors according to spec" when {
      def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
        s"a $downstreamErrorCode error is returned from the service" in new Test {

          MockRetrieveForeignReliefsConnector
            .retrieve(requestData)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

          await(service.retrieve(requestData)) shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val errors = Seq(
        "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
        "INVALID_TAX_YEAR"          -> TaxYearFormatError,
        "NO_DATA_FOUND"             -> NotFoundError,
        "INVALID_CORRELATIONID"     -> InternalError,
        "SERVER_ERROR"              -> InternalError,
        "SERVICE_UNAVAILABLE"       -> InternalError
      )

      val extraTysErrors = Seq(
        "INVALID_CORRELATION_ID" -> InternalError,
        "TAX_YEAR_NOT_SUPPORTED" -> RuleTaxYearNotSupportedError
      )

      (errors ++ extraTysErrors).foreach(args => (serviceError _).tupled(args))
    }
  }

}
