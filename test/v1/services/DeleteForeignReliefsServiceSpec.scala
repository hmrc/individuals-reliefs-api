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
import v1.mocks.connectors.MockDeleteForeignReliefsConnector
import v1.models.request.deleteForeignReliefs.DeleteForeignReliefsRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeleteForeignReliefsServiceSpec extends UnitSpec {

  "DeleteForeignReliefsServiceSpec" should {
    "deleteForeignReliefs" must {
      "return correct result for a success" in new Test {
        val outcome = Right(ResponseWrapper("resultId", ()))

        MockDeleteForeignReliefsConnector
          .delete(requestData)
          .returns(Future.successful(Right(ResponseWrapper("resultId", ()))))

        await(service.delete(requestData)) shouldBe outcome
      }

      "map errors according to spec" when {
        def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
          s"return ${error.code} error when $downstreamErrorCode error is returned from the connector" in new Test {

            MockDeleteForeignReliefsConnector
              .delete(requestData)
              .returns(Future.successful(Left(ResponseWrapper("resultId", DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

            await(service.delete(requestData)) shouldBe Left(ErrorWrapper("resultId", error))
          }

        val errors = Seq(
          ("NO_DATA_FOUND", NotFoundError),
          ("INVALID_TAX_YEAR", TaxYearFormatError),
          ("INVALID_CORRELATIONID", InternalError),
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

  trait Test extends MockDeleteForeignReliefsConnector {
    implicit val hc: HeaderCarrier              = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")
    implicit val correlationId: String          = "X-123"

    val validNino    = "AA123456A"
    val validTaxYear = "2019-20"

    val requestData: DeleteForeignReliefsRequest = DeleteForeignReliefsRequest(Nino(validNino), TaxYear.fromMtd(validTaxYear))

    val service = new DeleteForeignReliefsService(
      connector = mockConnector
    )

  }

}
