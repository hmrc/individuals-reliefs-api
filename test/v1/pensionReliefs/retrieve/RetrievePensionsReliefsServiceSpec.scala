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

package v1.pensionReliefs.retrieve

import shared.controllers.EndpointLogContext
import shared.models.domain.{Nino, TaxYear, Timestamp}
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import shared.utils.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier
import v1.pensionReliefs.retrieve.def1.model.request.Def1_RetrievePensionsReliefsRequestData
import v1.pensionReliefs.retrieve.def1.model.response.{Def1_RetrievePensionsReliefsResponse, PensionsReliefs}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrievePensionsReliefsServiceSpec extends UnitSpec {

  private val nino: String           = "ZG903729C"
  private val taxYear: String        = "2017-18"
  implicit val correlationId: String = "X-123"

  private val fullResponseModel = Def1_RetrievePensionsReliefsResponse(
    Timestamp("2019-04-04T01:01:01.000Z"),
    PensionsReliefs(
      Some(1999.99),
      Some(1999.99),
      Some(1999.99),
      Some(1999.99),
      Some(1999.99)
    )
  )

  private val requestData = Def1_RetrievePensionsReliefsRequestData(Nino(nino), TaxYear.fromMtd(taxYear))

  trait Test extends MockRetrievePensionsReliefsConnector {
    implicit val hc: HeaderCarrier              = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new RetrievePensionsReliefsService(
      connector = mockConnector
    )

  }

  "service" when {
    "service call successful" must {
      "return mapped result" in new Test {
        MockRetrievePensionsReliefsConnector
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, fullResponseModel))))

        await(service.retrieve(requestData)) shouldBe Right(ResponseWrapper(correlationId, fullResponseModel))
      }
    }

    "unsuccessful" must {
      "map errors according to spec" when {

        def serviceError(desErrorCode: String, error: MtdError): Unit =
          s"a $desErrorCode error is returned from the service" in new Test {

            MockRetrievePensionsReliefsConnector
              .retrieve(requestData)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(desErrorCode))))))

            await(service.retrieve(requestData)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val errors = Seq(
          ("INVALID_TAXABLE_ENTITY_ID", NinoFormatError),
          ("INVALID_TAX_YEAR", TaxYearFormatError),
          ("NOT_FOUND", NotFoundError),
          ("SERVER_ERROR", InternalError),
          ("SERVICE_UNAVAILABLE", InternalError)
        )

        val extraTysErrors = Seq(
          ("INVALID_CORRELATIONID", InternalError),
          ("TAX_YEAR_NOT_SUPPORTED", RuleTaxYearNotSupportedError)
        )

        (errors ++ extraTysErrors).foreach(args => (serviceError).tupled(args))
      }
    }
  }

}
