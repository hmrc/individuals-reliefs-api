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

package v1.controllers

import play.api.libs.json.Json
import v1.models.domain.Nino
import play.api.mvc.Result
import uk.gov.hmrc.http.HeaderCarrier
import v1.mocks.MockIdGenerator
import v1.mocks.hateoas.MockHateoasFactory
import v1.mocks.requestParsers.MockRetrieveInvestmentsRequestParser
import v1.mocks.services._
import v1.models.errors._
import v1.models.hateoas.Method.GET
import v1.models.hateoas.{HateoasWrapper, Link}
import v1.models.outcomes.ResponseWrapper
import v1.models.request.TaxYear
import v1.models.request.retrieveReliefInvestments.{RetrieveReliefInvestmentsRawData, RetrieveReliefInvestmentsRequest}
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
    with MockAuditService
    with MockIdGenerator {

  private val nino          = "AA123456A"
  private val taxYear       = "2019-20"
  private val correlationId = "X-123"

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new RetrieveReliefInvestmentsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockRequestDataParser,
      service = mockService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.getCorrelationId.returns(correlationId)
  }

  private val rawData     = RetrieveReliefInvestmentsRawData(nino, taxYear)
  private val requestData = RetrieveReliefInvestmentsRequest(Nino(nino), TaxYear.fromMtd(taxYear))

  private val testHateoasLink = Link(href = s"individuals/reliefs/investment/$nino/$taxYear", method = GET, rel = "self")

  private val responseBody = RetrieveReliefInvestmentsResponse(
    "2020-06-17T10:53:38Z",
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
          knowledgeIntensive = true,
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
              .returns(Left(ErrorWrapper(correlationId, error, None)))

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
          (RuleTaxYearNotSupportedError, BAD_REQUEST),
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
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

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
