/*
 * Copyright 2021 HM Revenue & Customs
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
import v1.mocks.connectors.MockDeletePensionsReliefsConnector
import v1.models.errors.{DesErrorCode, DesErrors, DownstreamError, ErrorWrapper, MtdError, NinoFormatError, NotFoundError, TaxYearFormatError}
import v1.models.outcomes.ResponseWrapper
import v1.models.request.deletePensionsReliefs.DeletePensionsReliefsRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeletePensionsReliefsServiceSpec extends UnitSpec {

  val validNino = Nino("AA123456A")
  val validTaxYear = "2019-20"
  implicit val correlationId = "X-123"

  val requestData = DeletePensionsReliefsRequest(validNino, validTaxYear)

  trait Test extends MockDeletePensionsReliefsConnector {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new DeletePensionsReliefsService(
      connector = mockConnector
    )
  }

  "service" when {
    "a service call is successful" should {
      "return a mapped result" in new Test {
        MockDeletePensionsReliefsConnector.delete(requestData)
          .returns(Future.successful(Right(ResponseWrapper("resultId", ()))))

        await(service.delete(requestData)) shouldBe Right(ResponseWrapper("resultId", ()))
      }
    }
    "a service call is unsuccessful" should {
      def serviceError(desErrorCode: String, error: MtdError): Unit =
        s"return ${error.code} error when $desErrorCode error is returned from the connector" in new Test {

          MockDeletePensionsReliefsConnector.delete(requestData)
            .returns(Future.successful(Left(ResponseWrapper("resultId", DesErrors.single(DesErrorCode(desErrorCode))))))

          await(service.delete(requestData)) shouldBe Left(ErrorWrapper("resultId", error))
        }

      val input = Seq(
        ("NOT_FOUND", NotFoundError),
        ("INVALID_TAX_YEAR", TaxYearFormatError),
        ("SERVER_ERROR", DownstreamError),
        ("SERVICE_UNAVAILABLE", DownstreamError),
        ("INVALID_TAXABLE_ENTITY_ID", NinoFormatError)
      )

      input.foreach(args => (serviceError _).tupled(args))
    }
  }

}

