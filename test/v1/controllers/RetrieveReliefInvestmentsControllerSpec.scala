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
import uk.gov.hmrc.domain.Nino
import play.api.mvc.Result
import uk.gov.hmrc.http.HeaderCarrier
import v1.mocks.hateoas.MockHateoasFactory
import v1.mocks.requestParsers.MockRetrieveInvestmentsRequestParser
import v1.mocks.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService, MockRetrieveReliefInvestmentsService}
import v1.models.errors._
import v1.models.hateoas.Method.GET
import v1.models.hateoas.{HateoasWrapper, Link}
import v1.models.outcomes.ResponseWrapper
import v1.models.requestData.retrieveReliefInvestments.{RetrieveReliefInvestmentsRawData, RetrieveReliefInvestmentsRequest}
import v1.models.response.retrieveReliefInvestments._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveReliefInvestmentsControllerSpec
  extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockRetrieveReliefInvestmentsService
    with MockRetrieveInvestmentsRequestParser
    with MockHateoasFactory
    with MockAuditService {

  trait Test {
    val hc = HeaderCarrier()

    val controller = new RetrieveReliefInvestmentsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      requestDataParser = mockRequestDataParser,
      service = mockRetrieveReliefInvestmentsService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
  }

  private val nino = "AA123456A"
  private val taxYear = "2019-20"
  private val correlationId = "X-123"

  private val rawData = RetrieveReliefInvestmentsRawData(nino, taxYear)
  private val requestData = RetrieveReliefInvestmentsRequest(Nino(nino), taxYear)

  private val testHateoasLink = Link(href = s"individuals/reliefs/innvestment/$nino/$taxYear", method = GET, rel = "self")

  private val responseBody = RetrieveReliefInvestmentsBody(
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

  "handleRequest" should {
    "return Ok" when {
      "the request received is valid" in new Test {

        MockRetrieveReliefInvestmentsRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockRetrieveReliefService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseBody))))

        MockHateoasFactory
          .wrap(responseBody, RetrieveReliefInvestmentsHateoasData(nino, taxYear))
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

            MockRetrieveReliefInvestmentsRequestParser
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

            MockRetrieveReliefInvestmentsRequestParser
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
          (RuleTaxYearRangeInvalidError, BAD_REQUEST),
          (NotFoundError, NOT_FOUND),
          (DownstreamError, INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }
}