/*
 * Copyright 2020 HM Revenue & Customs
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
import v1.mocks.connectors.MockRetrieveReliefInvestmentsConnector
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.retrieveReliefInvestments.RetrieveReliefInvestmentsRequest
import v1.models.response.retrieveReliefInvestments._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveReliefInvestmentsServiceSpec extends UnitSpec {

  private val nino = "AA123456A"
  private val taxYear = "2017-18"
  private val correlationId = "X-123"

  private val fullResponseModel = RetrieveReliefInvestmentsBody(
    Seq(VctSubscriptionsItem(
      Some("VCTREF"),
      Some("VCT Fund X"),
      Some("2018-04-16"),
      Some(BigDecimal(23312.00)),
      Some(BigDecimal(1334.00))
    )),
    Seq(EisSubscriptionsItem(
      Some("XTAL"),
      Some("EIS Fund X"),
      Some(true),
      Some("2020-12-12"),
      Some(BigDecimal(23312.00)),
      Some(BigDecimal(43432.00))
    )),
    Seq(CommunityInvestmentItem(
      Some("CIREF"),
      Some("CI X"),
      Some("2020-12-12"),
      Some(BigDecimal(6442.00)),
      Some(BigDecimal(2344.00))
    )),
    Seq(SeedEnterpriseInvestmentItem(
      Some("123412/1A"),
      Some("Company Inc"),
      Some("2020-12-12"),
      Some(BigDecimal(123123.22)),
      Some(BigDecimal(3432.00))
    )),
    Seq(SocialEnterpriseInvestmentItem(
      Some("123412/1A"),
      Some("SE Inc"),
      Some("2020-12-12"),
      Some(BigDecimal(123123.22)),
      Some(BigDecimal(3432.00))
    ))
  )

  private val requestData = RetrieveReliefInvestmentsRequest(Nino(nino), taxYear)

  trait Test extends MockRetrieveReliefInvestmentsConnector {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")
    val service = new RetrieveReliefInvestmentsService(
      retrieveReliefInvestmentsConnector = mockRetrieveReliefInvestmentsConnector
    )
  }

  "service" when {
    "service call successful" must {
      "return mapped result" in new Test {
        MockRetrieveReliefInvestmentsConnector.retrieveReliefInvestments(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, fullResponseModel))))

        await(service.retrieveReliefInvestments(requestData)) shouldBe Right(ResponseWrapper(correlationId, fullResponseModel))
      }
    }

    "unsuccessful" must {
      "map errors according to spec" when {

        def serviceError(desErrorCode: String, error: MtdError): Unit =
          s"a $desErrorCode error is returned from the service" in new Test {

            MockRetrieveReliefInvestmentsConnector.retrieveReliefInvestments(requestData)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode(desErrorCode))))))

            await(service.retrieveReliefInvestments(requestData)) shouldBe Left(ErrorWrapper(Some(correlationId), error))
          }

        val input = Seq(
          ("INVALID_TAXABLE_ENTITY_ID", NinoFormatError),
          ("FORMAT_NINO", NinoFormatError),
          ("CLIENT_OR_AGENT_NOT_AUTHORISED", UnauthorisedError),
          ("FORMAT_TAX_YEAR", TaxYearFormatError),
          ("NOT_FOUND", NotFoundError),
          ("SERVER_ERROR", DownstreamError),
          ("SERVICE_UNAVAILABLE", DownstreamError)
        )

        input.foreach(args => (serviceError _).tupled(args))
      }
    }
  }
}
