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

package v1.controllers

import play.api.libs.json.Json
import play.api.mvc.Result
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import v1.mocks.hateoas.MockHateoasFactory
import v1.mocks.requestParsers.MockRetrieveOtherReliefsRequestParser
import v1.mocks.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService, MockRetrieveOtherReliefsService}
import v1.models.errors.{BadRequestError, DownstreamError, ErrorWrapper, MtdError, NinoFormatError, NotFoundError, RuleTaxYearRangeInvalidError, TaxYearFormatError}
import v1.models.hateoas.{HateoasWrapper, Link}
import v1.models.hateoas.Method.GET
import v1.models.outcomes.ResponseWrapper
import v1.models.request.retrieveOtherReliefs.{RetrieveOtherReliefsRawData, RetrieveOtherReliefsRequest}
import v1.models.response.retrieveOtherReliefs.{AnnualPaymentsMade, MaintenancePayments, NonDeductableLoanInterest, PayrollGiving, PostCessationTradeReliefAndCertainOtherLosses, QualifyingDistributionRedemptionOfSharesAndSecurities, QualifyingLoanInterestPayments, RetrieveOtherReliefsBody, RetrieveOtherReliefsHateoasData}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveOtherReliefsControllerSpec
  extends ControllerBaseSpec
  with MockEnrolmentsAuthService
  with MockMtdIdLookupService
  with MockRetrieveOtherReliefsService
  with MockRetrieveOtherReliefsRequestParser
  with MockHateoasFactory
  with MockAuditService {

  trait Test {
    val hc = HeaderCarrier()

    val controller = new RetrieveOtherReliefsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockRequestDataParser,
      service = mockService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
  }

  private val nino = "AA123456A"
  private val taxYear = "2019-20"
  private val correlationId = "X-123"

  private val rawData = RetrieveOtherReliefsRawData(nino, taxYear)
  private val requestData = RetrieveOtherReliefsRequest(Nino(nino), taxYear)

  private val testHateoasLink = Link(href = s"individuals/reliefs/other/$nino/$taxYear", method = GET, rel = "self")

  private val responseBody = RetrieveOtherReliefsBody(
    Some(NonDeductableLoanInterest(
      Some("myref"),
      763.00)),
    Some(PayrollGiving(
      Some("myref"),
      154.00)),
    Some(QualifyingDistributionRedemptionOfSharesAndSecurities(
      Some("myref"),
      222.22)),
    Some(Seq(MaintenancePayments(
      Some("myref"),
      Some("Hilda"),
      Some("2000-01-01"),
      Some(222.22)))),
    Some(Seq(PostCessationTradeReliefAndCertainOtherLosses(
      Some("myref"),
      Some("ACME Inc"),
      Some("2019-08-10"),
      Some("Widgets Manufacturer"),
      Some("AB12412/A12"),
      Some(222.22)))),
    Some(AnnualPaymentsMade(
      Some("myref"),
      763.00)),
    Some(Seq(QualifyingLoanInterestPayments(
      Some("myref"),
      Some("Maurice"),
      763.00)))
  )

  "handleRequest" should {
    "return Ok" when {
      "the request received is valid" in new Test {

        MockRetrieveOtherReliefsRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockRetrieveReliefService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseBody))))

        MockHateoasFactory
          .wrap(responseBody, RetrieveOtherReliefsHateoasData(nino, taxYear))
          .returns(HateoasWrapper(responseBody, Seq(testHateoasLink)))

        val result: Future[Result] = controller.handleRequest(nino, taxYear)(fakeRequest)
        status(result) shouldBe OK
        header("X-CorrelationId", result) shouldBe Some(correlationId)
      }
    }
    "return the error as per spec" when {
      "parser errors occur" should {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockRetrieveOtherReliefsRequestParser
              .parse(rawData)
              .returns(Left(ErrorWrapper(Some(correlationId), error, None)))

            val result: Future[Result] = controller.handleRequest(nino, taxYear)(fakeRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (BadRequestError, BAD_REQUEST),
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (RuleTaxYearRangeInvalidError, BAD_REQUEST)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" should {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockRetrieveOtherReliefsRequestParser
              .parse(rawData)
              .returns(Right(requestData))

            MockRetrieveReliefService
              .retrieve(requestData)
              .returns(Future.successful(Left(ErrorWrapper(Some(correlationId), mtdError))))

            val result: Future[Result] = controller.handleRequest(nino, taxYear)(fakeRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (NotFoundError, NOT_FOUND),
          (DownstreamError, INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }
}
