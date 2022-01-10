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
import v1.mocks.connectors.MockAmendForeignReliefsConnector
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.amendForeignReliefs._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AmendForeignReliefsServiceSpec extends UnitSpec {

  private val taxYear = "2017-18"
  private val nino = Nino("AA123456A")
  private implicit val correlationId: String = "X-123"
  private val amount: BigDecimal = 1234.56

  private val body = AmendForeignReliefsBody(
    foreignTaxCreditRelief = Some(ForeignTaxCreditRelief(
      amount = amount
    )),
    foreignIncomeTaxCreditRelief = Some(Seq(ForeignIncomeTaxCreditRelief(
      countryCode = "FRA",
      foreignTaxPaid = Some(amount),
      taxableAmount = amount,
      employmentLumpSum = true
    ))),
    foreignTaxForFtcrNotClaimed = Some(ForeignTaxForFtcrNotClaimed(
      amount = amount
    ))
  )

  private val requestData = AmendForeignReliefsRequest(nino, taxYear, body)

  trait Test extends MockAmendForeignReliefsConnector {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new AmendForeignReliefsService(
      connector = mockAmendForeignReliefsConnector
    )
  }

  "service" when {
    "service call successful" must {
      "return mapped result" in new Test {
        MockAmendForeignReliefsConnector.amend(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        await(service.amend(requestData)) shouldBe Right(ResponseWrapper(correlationId, ()))
      }
    }
  }

  "unsuccessful" must {
    "map errors according to spec" when {

      def serviceError(desErrorCode: String, error: MtdError): Unit =
        s"a $desErrorCode error is returned from the service" in new Test {

          MockAmendForeignReliefsConnector.amend(requestData)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode(desErrorCode))))))

          await(service.amend(requestData)) shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val input = Seq(
        "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
        "FORMAT_TAX_YEAR" -> TaxYearFormatError,
        "SERVER_ERROR" -> DownstreamError,
        "SERVICE_UNAVAILABLE" -> DownstreamError
      )

      input.foreach(args => (serviceError _).tupled(args))
    }
  }
}
