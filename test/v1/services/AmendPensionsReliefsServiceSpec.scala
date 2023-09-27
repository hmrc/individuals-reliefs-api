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
import support.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier
import v1.mocks.connectors.MockAmendPensionsReliefsConnector
import v1.models.request.amendPensionsReliefs.{AmendPensionsReliefsBody, AmendPensionsReliefsRequestData, PensionReliefs}

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

  private val requestData = AmendPensionsReliefsRequestData(Nino(nino), TaxYear.fromMtd(taxYear), body)

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
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(desErrorCode))))))

          await(service.amend(requestData)) shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val errors = Seq(
        "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
        "INVALID_TAX_YEAR"          -> TaxYearFormatError,
        "INVALID_PAYLOAD"           -> InternalError,
        "SERVER_ERROR"              -> InternalError,
        "SERVICE_UNAVAILABLE"       -> InternalError
      )

      val extraTysErrors = Seq(
        "INVALID_CORRELATIONID"  -> InternalError,
        "TAX_YEAR_NOT_SUPPORTED" -> RuleTaxYearNotSupportedError
      )

      (errors ++ extraTysErrors).foreach(args => (serviceError _).tupled(args))
    }
  }

}
