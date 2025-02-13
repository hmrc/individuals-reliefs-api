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

package v2.createAndAmendForeignReliefs

import shared.controllers.EndpointLogContext
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import shared.utils.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier
import v2.deleteForeignReliefs.model.Def1_DeleteForeignReliefsRequestData
import v2.deleteForeignReliefs.{DeleteForeignReliefsService, MockDeleteForeignReliefsConnector}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeleteForeignReliefsServiceSpec extends UnitSpec {

  implicit private val correlationId: String = "X-123"
  private val nino                           = Nino("AA123456A")
  private val taxYear                        = TaxYear.fromMtd("2019-20")

  "Delete Foreign Reliefs Service" when {
    "service call successful" should {
      "return mapped result" in new Test {
        MockDeleteForeignReliefsConnector
          .delete(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        await(service.delete(requestData)) shouldBe Right(ResponseWrapper(correlationId, ()))
      }
    }
    "unsuccessful" should {
      "map errors according to spec" when {
        def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
          s"a $downstreamErrorCode error is returned from the service" in new Test {

            MockDeleteForeignReliefsConnector
              .delete(requestData)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

            await(service.delete(requestData)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val errors = List(
          ("NO_DATA_FOUND", NotFoundError),
          ("INVALID_TAX_YEAR", TaxYearFormatError),
          ("INVALID_CORRELATIONID", InternalError),
          ("SERVER_ERROR", InternalError),
          ("SERVICE_UNAVAILABLE", InternalError),
          ("INVALID_TAXABLE_ENTITY_ID", NinoFormatError)
        )

        val extraTysErrors = List(
          ("INVALID_CORRELATION_ID", InternalError),
          ("TAX_YEAR_NOT_SUPPORTED", RuleTaxYearNotSupportedError)
        )

        (errors ++ extraTysErrors).foreach(args => (serviceError _).tupled(args))
      }
    }
  }

  trait Test extends MockDeleteForeignReliefsConnector {
    implicit protected val hc: HeaderCarrier              = HeaderCarrier()
    implicit protected val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    protected val service = new DeleteForeignReliefsService(connector = mockDeleteForeignReliefsConnector)

    val requestData: Def1_DeleteForeignReliefsRequestData = Def1_DeleteForeignReliefsRequestData(nino, taxYear)

  }

}
