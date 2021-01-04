/*
 * Copyright 2021 HM Revenue & Customs
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

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import v1.mocks.MockIdGenerator
import v1.mocks.hateoas.MockHateoasFactory
import v1.mocks.requestParsers.MockAmendReliefInvestmentsRequestParser
import v1.mocks.services.{MockAmendReliefInvestmentsService, MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import v1.models.audit.{AmendReliefInvestmentsAuditDetail, AuditError, AuditEvent, AuditResponse}
import v1.models.errors._
import v1.models.hateoas.Method.{DELETE, GET, PUT}
import v1.models.hateoas.{HateoasWrapper, Link}
import v1.models.outcomes.ResponseWrapper
import v1.models.request.amendReliefInvestments._
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
    with MockAuditService
    with MockIdGenerator {

  private val nino = "AA123456A"
  private val taxYear = "2019-20"
  private val correlationId = "X-123"

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new AmendReliefInvestmentsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockAmendReliefInvestmentsRequestParser,
      service = mockService,
      auditService = mockAuditService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.getCorrelationId.returns(correlationId)
  }

  private val testHateoasLinks: Seq[Link] = Seq(
    Link(href = s"/individuals/reliefs/investment/$nino/$taxYear", method = PUT, rel = "amend-reliefs-investment"),
    Link(href = s"/individuals/reliefs/investment/$nino/$taxYear", method = GET, rel = "self"),
    Link(href = s"/individuals/reliefs/investment/$nino/$taxYear", method = DELETE, rel = "delete-reliefs-investment")
  )

  private val requestJson = Json.parse(
    """|
       |{
       |  "vctSubscription":[
       |    {
       |      "uniqueInvestmentRef": "VCTREF",
       |      "name": "VCT Fund X",
       |      "dateOfInvestment": "2018-04-16",
       |      "amountInvested": 23312.00,
       |      "reliefClaimed": 1334.00
       |      }
       |  ],
       |  "eisSubscription":[
       |    {
       |      "uniqueInvestmentRef": "XTAL",
       |      "name": "EIS Fund X",
       |      "knowledgeIntensive": true,
       |      "dateOfInvestment": "2020-12-12",
       |      "amountInvested": 23312.00,
       |      "reliefClaimed": 43432.00
       |    }
       |  ],
       |  "communityInvestment": [
       |    {
       |      "uniqueInvestmentRef": "CIREF",
       |      "name": "CI X",
       |      "dateOfInvestment": "2020-12-12",
       |      "amountInvested": 6442.00,
       |      "reliefClaimed": 2344.00
       |    }
       |  ],
       |  "seedEnterpriseInvestment": [
       |    {
       |      "uniqueInvestmentRef": "123412/1A",
       |      "companyName": "Company Inc",
       |      "dateOfInvestment": "2020-12-12",
       |      "amountInvested": 123123.22,
       |      "reliefClaimed": 3432.00
       |    }
       |  ],
       |  "socialEnterpriseInvestment": [
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
      "VCTREF",
      Some("VCT Fund X"),
      Some("2018-04-16"),
      Some(BigDecimal(23312.00)),
      BigDecimal(1334.00)
    ))),
    Some(Seq(EisSubscriptionsItem(
      "XTAL",
      Some("EIS Fund X"),
      knowledgeIntensive = true,
      Some("2020-12-12"),
      Some(BigDecimal(23312.00)),
      BigDecimal(43432.00)
    ))),
    Some(Seq(CommunityInvestmentItem(
      "CIREF",
      Some("CI X"),
      Some("2020-12-12"),
      Some(BigDecimal(6442.00)),
      BigDecimal(2344.00)
    ))),
    Some(Seq(SeedEnterpriseInvestmentItem(
      "123412/1A",
      Some("Company Inc"),
      Some("2020-12-12"),
      Some(BigDecimal(123123.22)),
      BigDecimal(3432.00)
    ))),
    Some(Seq(SocialEnterpriseInvestmentItem(
      "123412/1A",
      Some("SE Inc"),
      Some("2020-12-12"),
      Some(BigDecimal(123123.22)),
      BigDecimal(3432.00)
    )))
  )

  private val rawData = AmendReliefInvestmentsRawData(nino, taxYear, requestJson)
  private val requestData = AmendReliefInvestmentsRequest(Nino(nino), taxYear, requestBody)

  val hateoasResponse: JsValue = Json.parse(
    """
      |{
      |        "links": [
      |          {
      |            "href": "/individuals/reliefs/investment/AA123456A/2019-20",
      |            "rel": "amend-reliefs-investment",
      |            "method": "PUT"
      |          },
      |          {
      |            "href": "/individuals/reliefs/investment/AA123456A/2019-20",
      |            "rel": "self",
      |            "method": "GET"
      |          },
      |          {
      |            "href": "/individuals/reliefs/investment/AA123456A/2019-20",
      |            "rel": "delete-reliefs-investment",
      |            "method": "DELETE"
      |          }
      |        ]
      |}
      |""".stripMargin)

  def event(auditResponse: AuditResponse): AuditEvent[AmendReliefInvestmentsAuditDetail] =
    AuditEvent(
      auditType = "CreateAmendReliefsInvestment",
      transactionName = "create-amend-reliefs-investment",
      detail = AmendReliefInvestmentsAuditDetail(
        userType = "Individual",
        agentReferenceNumber = None,
        nino,
        taxYear,
        requestJson,
        correlationId,
        response = auditResponse
      )
    )

  "handleRequest" should {
    "return Ok" when {
      "the request received is valid" in new Test {

        MockAmendReliefInvestmentsRequestParser
          .parseRequest(rawData)
          .returns(Right(requestData))

        MockAmendReliefService
          .amend(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        MockHateoasFactory
          .wrap((), AmendReliefInvestmentsHateoasData(nino, taxYear))
          .returns(HateoasWrapper((), testHateoasLinks))

        val result: Future[Result] = controller.handleRequest(nino, taxYear)(fakePostRequest(requestJson))
        status(result) shouldBe OK
        header("X-CorrelationId", result) shouldBe Some(correlationId)

        val auditResponse: AuditResponse = AuditResponse(OK, None, Some(hateoasResponse))
        MockedAuditService.verifyAuditEvent(event(auditResponse)).once
      }
    }
    "return the error as per spec" when {
      "parser errors occur" should {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockAmendReliefInvestmentsRequestParser
              .parseRequest(rawData)
              .returns(Left(ErrorWrapper(correlationId, error, None)))

            val result: Future[Result] = controller.handleRequest(nino, taxYear)(fakePostRequest(requestJson))

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)

            val auditResponse: AuditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(error.code))), None)
            MockedAuditService.verifyAuditEvent(event(auditResponse)).once
          }
        }

        val input = Seq(
          (BadRequestError, BAD_REQUEST),
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (RuleTaxYearNotSupportedError, BAD_REQUEST),
          (RuleTaxYearRangeInvalidError, BAD_REQUEST),
          (ValueFormatError, BAD_REQUEST),
          (RuleIncorrectOrEmptyBodyError, BAD_REQUEST),
          (DateOfInvestmentFormatError, BAD_REQUEST),
          (UniqueInvestmentRefFormatError, BAD_REQUEST),
          (NameFormatError, BAD_REQUEST)
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
              .amend(requestData)
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

            val result: Future[Result] = controller.handleRequest(nino, taxYear)(fakePostRequest(requestJson))

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)

            val auditResponse: AuditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(mtdError.code))), None)
            MockedAuditService.verifyAuditEvent(event(auditResponse)).once
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (DownstreamError, INTERNAL_SERVER_ERROR),
          (TaxYearFormatError, BAD_REQUEST)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }
}