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
import v1.mocks.connectors.MockCreateAndAmendReliefInvestmentsConnector
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.TaxYear
import v1.models.request.createAndAmendReliefInvestments._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateAndAmendReliefInvestmentsServiceSpec extends UnitSpec {

  private val nino: String           = "AA123456A"
  private val taxYear: String        = "2017-18"
  implicit val correlationId: String = "X-123"

  private val requestBody = CreateAndAmendReliefInvestmentsBody(
    Some(
      Seq(
        VctSubscriptionsItem(
          "VCTREF",
          Some("VCT Fund X"),
          Some("2018-04-16"),
          Some(BigDecimal(23312.00)),
          BigDecimal(1334.00)
        ))),
    Some(
      Seq(
        EisSubscriptionsItem(
          "XTAL",
          Some("EIS Fund X"),
          true,
          Some("2020-12-12"),
          Some(BigDecimal(23312.00)),
          BigDecimal(43432.00)
        ))),
    Some(
      Seq(
        CommunityInvestmentItem(
          "CIREF",
          Some("CI X"),
          Some("2020-12-12"),
          Some(BigDecimal(6442.00)),
          BigDecimal(2344.00)
        ))),
    Some(
      Seq(
        SeedEnterpriseInvestmentItem(
          "123412/1A",
          Some("Company Inc"),
          Some("2020-12-12"),
          Some(BigDecimal(123123.22)),
          BigDecimal(3432.00)
        ))),
    Some(
      Seq(
        SocialEnterpriseInvestmentItem(
          "123412/1A",
          Some("SE Inc"),
          Some("2020-12-12"),
          Some(BigDecimal(123123.22)),
          BigDecimal(3432.00)
        )))
  )

  private val requestData = CreateAndAmendReliefInvestmentsRequest(Nino(nino), TaxYear.fromMtd(taxYear), requestBody)

  trait Test extends MockCreateAndAmendReliefInvestmentsConnector {
    implicit val hc: HeaderCarrier              = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new CreateAndAmendReliefInvestmentsService(
      connector = mockConnector
    )

  }

  "service" when {
    "service call successsful" must {
      "return mapped result" in new Test {
        MockCreateAndAmendReliefInvestmentsConnector
          .amend(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        await(service.amend(requestData)) shouldBe Right(ResponseWrapper(correlationId, ()))
      }
    }

    "unsuccessful" must {
      "map errors according to spec" when {

        def serviceError(desErrorCode: String, error: MtdError): Unit =
          s"a $desErrorCode error is returned from the service" in new Test {

            MockCreateAndAmendReliefInvestmentsConnector
              .amend(requestData)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(desErrorCode))))))

            await(service.amend(requestData)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val input = Seq(
          ("INVALID_TAXABLE_ENTITY_ID", NinoFormatError),
          ("FORMAT_TAX_YEAR", TaxYearFormatError),
          ("SERVER_ERROR", InternalError),
          ("SERVICE_UNAVAILABLE", InternalError)
        )

        input.foreach(args => (serviceError _).tupled(args))
      }
    }
  }

}
