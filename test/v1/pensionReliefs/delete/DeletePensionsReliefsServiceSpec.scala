/*
 * Copyright 2024 HM Revenue & Customs
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

package v1.pensionReliefs.delete

import shared.controllers.EndpointLogContext
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import shared.utils.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier
import v1.pensionReliefs.delete.def1.model.request.Def1_DeletePensionsReliefsRequestData
import v1.pensionReliefs.delete.model.request.DeletePensionsReliefsRequestData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeletePensionsReliefsServiceSpec extends UnitSpec {

  implicit val correlationId: String = "X-123"
  private val validNino              = Nino("AA123456A")
  private val validTaxYear           = TaxYear.fromMtd("2019-20")

  protected val requestData: DeletePensionsReliefsRequestData = Def1_DeletePensionsReliefsRequestData(validNino, validTaxYear)

  trait Test extends MockDeletePensionsReliefsConnector {
    implicit protected val hc: HeaderCarrier              = HeaderCarrier()
    implicit protected val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    protected val service = new DeletePensionsReliefsService(
      connector = mockDeletePensionsReliefsConnector
    )

  }

  "service" when {
    "a service call is successful" should {
      "return a mapped result" in new Test {
        MockDeletePensionsReliefsConnector
          .deletePensionsReliefs(requestData)
          .returns(Future.successful(Right(ResponseWrapper("resultId", ()))))

        await(service.deletePensionsReliefs(requestData)) shouldBe Right(ResponseWrapper("resultId", ()))
      }
    }
    "a service call is unsuccessful" should {
      def serviceError(desErrorCode: String, error: MtdError): Unit =
        s"return ${error.code} error when $desErrorCode error is returned from the connector" in new Test {

          MockDeletePensionsReliefsConnector
            .deletePensionsReliefs(requestData)
            .returns(Future.successful(Left(ResponseWrapper("resultId", DownstreamErrors.single(DownstreamErrorCode(desErrorCode))))))

          await(service.deletePensionsReliefs(requestData)) shouldBe Left(ErrorWrapper("resultId", error))
        }

      val errors = Seq(
        ("NOT_FOUND", NotFoundError),
        ("INVALID_TAX_YEAR", TaxYearFormatError),
        ("SERVER_ERROR", InternalError),
        ("SERVICE_UNAVAILABLE", InternalError),
        ("INVALID_TAXABLE_ENTITY_ID", NinoFormatError)
      )

      val extraTysErrors = Seq(
        ("INVALID_CORRELATION_ID", InternalError),
        ("TAX_YEAR_NOT_SUPPORTED", RuleTaxYearNotSupportedError)
      )

      (errors ++ extraTysErrors).foreach(args => serviceError.tupled(args))
    }
  }

}
