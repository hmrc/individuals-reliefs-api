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
import v1.mocks.requestParsers.MockAmendReliefInvestmentsRequestParser
import v1.mocks.services.{MockAmendReliefInvestmentsService, MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import v1.models.errors._
import v1.models.hateoas.{HateoasWrapper, Link}
import v1.models.hateoas.Method.PUT
import v1.models.outcomes.ResponseWrapper
import v1.models.request.amendReliefInvestments.{AmendReliefInvestmentsBody, AmendReliefInvestmentsRawData, AmendReliefInvestmentsRequest, CommunityInvestmentItem, EisSubscriptionsItem, SeedEnterpriseInvestmentItem, SocialEnterpriseInvestmentItem, VctSubscriptionsItem}
import v1.models.response.amendReliefInvestments.AmendReliefInvestmentsHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AmendReliefInvestmentsControllerSpec
  extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockAmendReliefInvestmentsService
    with MockAmendReliefInvestmentsRequestParser
    with MockHateoasFactory
    with MockAuditService {

  trait Test {
    val hc = HeaderCarrier()

    val controller = new AmendReliefInvestmentsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      amendReliefInvestmentsParser = mockAmendReliefInvestmentsRequestParser,
      amendReliefInvestmentsService = mockAmendReliefInvestmentsService,
      hateoasFactory = mockHateoasFactory,
      cc = cc
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
  }

  private val nino = "AA123456A"
  private val taxYear = "2019-20"
  private val correlationId = "X-123"

  private val testHateoasLink = Link(href = s"individuals/reliefs/innvestment/$nino/$taxYear", method = PUT, rel = "self")

  private val requestJson = Json.parse(
    """|
       |{
       |  "vctSubscriptionsItems":[
       |    {
       |      "uniqueInvestmentRef": "VCTREF",
       |      "name": "VCT Fund X",
       |      "dateOfInvestment": "2018-04-16",
       |      "amountInvested": 23312.00,
       |      "reliefClaimed": 1334.00
       |      }
       |  ],
       |  "eisSubscriptionsItems":[
       |    {
       |      "uniqueInvestmentRef": "XTAL",
       |      "name": "EIS Fund X",
       |      "knowledgeIntensive": true,
       |      "dateOfInvestment": "2020-12-12",
       |      "amountInvested": 23312.00,
       |      "reliefClaimed": 43432.00
       |    }
       |  ],
       |  "communityInvestmentItems": [
       |    {
       |      "uniqueInvestmentRef": "CIREF",
       |      "name": "CI X",
       |      "dateOfInvestment": "2020-12-12",
       |      "amountInvested": 6442.00,
       |      "reliefClaimed": 2344.00
       |    }
       |  ],
       |  "seedEnterpriseInvestmentItems": [
       |    {
       |      "uniqueInvestmentRef": "123412/1A",
       |      "companyName": "Company Inc",
       |      "dateOfInvestment": "2020-12-12",
       |      "amountInvested": 123123.22,
       |      "reliefClaimed": 3432.00
       |    }
       |  ],
       |  "socialEnterpriseInvestmentItems": [
       |    {
       |      "uniqueInvestmentRef": "123412/1A",
       |      "socialEnterpriseName": "SE Inc",
       |      "dateOfInvestment": "2020-12-12",
       |      "amountInvested": 123123.22,
       |      "reliefClaimed": 3432.00
       |    }
       |  ]
       |}
       |""".stripMargin
  )

  private val requestBody = AmendReliefInvestmentsBody(
    Some(Seq(VctSubscriptionsItem(
      Some("VCTREF"),
      Some("VCT Fund X"),
      Some("2018-04-16"),
      Some(BigDecimal(23312.00)),
      Some(BigDecimal(1334.00))
    ))),
    Some(Seq(EisSubscriptionsItem(
      Some("XTAL"),
      Some("EIS Fund X"),
      Some(true),
      Some("2020-12-12"),
      Some(BigDecimal(23312.00)),
      Some(BigDecimal(43432.00))
    ))),
    Some(Seq(CommunityInvestmentItem(
      Some("CIREF"),
      Some("CI X"),
      Some("2020-12-12"),
      Some(BigDecimal(6442.00)),
      Some(BigDecimal(2344.00))
    ))),
    Some(Seq(SeedEnterpriseInvestmentItem(
      Some("123412/1A"),
      Some("Company Inc"),
      Some("2020-12-12"),
      Some(BigDecimal(123123.22)),
      Some(BigDecimal(3432.00))
    ))),
    Some(Seq(SocialEnterpriseInvestmentItem(
      Some("123412/1A"),
      Some("SE Inc"),
      Some("2020-12-12"),
      Some(BigDecimal(123123.22)),
      Some(BigDecimal(3432.00))
    )))
  )

  private val rawData = AmendReliefInvestmentsRawData(nino, taxYear, requestJson)
  private val requestData = AmendReliefInvestmentsRequest(Nino(nino), taxYear, requestBody)

  "handleRequest" should {
    "return Ok" when {
      "the request received is valid" in new Test {

        MockAmendReliefInvestmentsRequestParser
          .parseRequest(rawData)
          .returns(Right(requestData))

        MockAmendReliefService
          .doServiceThing(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        MockHateoasFactory
          .wrap((), AmendReliefInvestmentsHateoasData(nino, taxYear))
          .returns(HateoasWrapper((), Seq(testHateoasLink)))

        val result: Future[Result] = controller.handleRequest(nino, taxYear)(fakePostRequest(requestJson))
        status(result) shouldBe OK
        header("X-CorrelationId", result) shouldBe Some(correlationId)
      }
    }
    "return the error as per spec" when {
      "parser errors occur" should {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockAmendReliefInvestmentsRequestParser
              .parseRequest(rawData)
              .returns(Left(ErrorWrapper(Some(correlationId), error, None)))

            val result: Future[Result] = controller.handleRequest(nino, taxYear)(fakePostRequest(requestJson))

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (BadRequestError, BAD_REQUEST),
          (NinoFormatError, BAD_REQUEST),
          (ValueFormatError.copy(paths = Some(Seq(
            "vctSubscriptionsItems/0/amountInvested",
            "vctSubscriptionsItems/0/reliefClaimed"))), BAD_REQUEST),
          (DateOfInvestmentFormatError.copy(paths = Some(Seq(
            "vctSubscriptionsItems/0/dateOfInvestment",
            "eisSubscriptionsItems/0/dateOfInvestment"))), BAD_REQUEST),
          (NameFormatError.copy(paths = Some(Seq(
            "vctSubscriptionsItems/0/name",
            "eisSubscriptionsItems/0/name"))), BAD_REQUEST),
          (InvestmentRefFormatError.copy(paths = Some(Seq(
            "vctSubscriptionsItems/0/uniqueInvestmentRef",
            "eisSubscriptionsItems/0/uniqueInvestmentRef"))), BAD_REQUEST)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" should {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockAmendReliefInvestmentsRequestParser
              .parseRequest(rawData)
              .returns(Right(requestData))

            MockAmendReliefService
              .doServiceThing(requestData)
              .returns(Future.successful(Left(ErrorWrapper(Some(correlationId), mtdError))))

            val result: Future[Result] = controller.handleRequest(nino, taxYear)(fakePostRequest(requestJson))

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (NotFoundError, NOT_FOUND),
          (DownstreamError, INTERNAL_SERVER_ERROR),
          (UnauthorisedError, UNAUTHORIZED),
          (TaxYearFormatError, BAD_REQUEST)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }
}