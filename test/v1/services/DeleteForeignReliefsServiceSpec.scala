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
import v1.mocks.connectors.MockDeleteForeignReliefsConnector
import v1.models.domain.Nino
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.TaxYear
import v1.models.request.deleteForeignReliefs.DeleteForeignReliefsRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeleteForeignReliefsServiceSpec extends UnitSpec {

  val validNino: String              = "AA123456A"
  val validTaxYear: String           = "2019-20"
  implicit val correlationId: String = "X-123"

  val requestData: DeleteForeignReliefsRequest = DeleteForeignReliefsRequest(Nino(validNino), TaxYear.fromMtd(validTaxYear))

  trait Test extends MockDeleteForeignReliefsConnector {
    implicit val hc: HeaderCarrier              = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new DeleteForeignReliefsService(
      connector = mockConnector
    )

  }

  "service" when {
    "a service call is successful" should {
      "return a mapped result" in new Test {
        MockDeleteForeignReliefsConnector
          .delete(requestData)
          .returns(Future.successful(Right(ResponseWrapper("resultId", ()))))

        await(service.delete(requestData)) shouldBe Right(ResponseWrapper("resultId", ()))
      }
    }
    "a service call is unsuccessful" should {
      def serviceError(desErrorCode: String, error: MtdError): Unit =
        s"return ${error.code} error when $desErrorCode error is returned from the connector" in new Test {

          MockDeleteForeignReliefsConnector
            .delete(requestData)
            .returns(Future.successful(Left(ResponseWrapper("resultId", DownstreamErrors.single(DownstreamErrorCode(desErrorCode))))))

          await(service.delete(requestData)) shouldBe Left(ErrorWrapper("resultId", error))
        }

      val input = Seq(
        ("NO_DATA_FOUND", NotFoundError),
        ("FORMAT_TAX_YEAR", TaxYearFormatError),
        ("SERVER_ERROR", InternalError),
        ("SERVICE_UNAVAILABLE", InternalError),
        ("INVALID_TAXABLE_ENTITY_ID", NinoFormatError)
      )

      input.foreach(args => (serviceError _).tupled(args))
    }
  }

}
