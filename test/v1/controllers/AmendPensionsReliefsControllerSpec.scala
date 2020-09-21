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
import v1.mocks.requestParsers.MockAmendPensionsReliefsRequestParser
import v1.mocks.services.{MockAmendPensionsReliefsService, MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import v1.models.audit.{AmendPensionsReliefsAuditDetail, AuditError, AuditEvent, AuditResponse}
import v1.models.errors._
import v1.models.hateoas.Method.{DELETE, GET, PUT}
import v1.models.hateoas.{HateoasWrapper, Link}
import v1.models.outcomes.ResponseWrapper
import v1.models.request.amendPensionsReliefs._
import v1.models.response.amendPensionsReliefs.AmendPensionsReliefsHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AmendPensionsReliefsControllerSpec
  extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockAmendPensionsReliefsService
    with MockAmendPensionsReliefsRequestParser
    with MockHateoasFactory
    with MockAuditService {

  private val nino = "AA123456A"
  private val taxYear = "2019-20"
  private val correlationId = "X-123"

  private val testHateoasLinks = Seq(
    Link(href = s"/individuals/reliefs/pensions/$nino/$taxYear", method = PUT, rel = "amend-reliefs-pensions"),
    Link(href = s"/individuals/reliefs/pensions/$nino/$taxYear", method = GET, rel = "self"),
    Link(href = s"/individuals/reliefs/pensions/$nino/$taxYear", method = DELETE, rel = "delete-reliefs-pensions")
  )

  private val requestJson = Json.parse(
    """|{
       |  "pensionReliefs": {
       |    "regularPensionContributions": 1999.99,
       |    "oneOffPensionContributionsPaid": 1999.99,
       |    "retirementAnnuityPayments": 1999.99,
       |    "paymentToEmployersSchemeNoTaxRelief": 1999.99,
       |    "overseasPensionSchemeContributions": 1999.99
       |  }
       |}""".stripMargin
  )

  private val requestBody = AmendPensionsReliefsBody(
    pensionReliefs = PensionReliefs(
      regularPensionContributions = Some(1999.99),
      oneOffPensionContributionsPaid = Some(1999.99),
      retirementAnnuityPayments = Some(1999.99),
      paymentToEmployersSchemeNoTaxRelief = Some(1999.99),
      overseasPensionSchemeContributions = Some(1999.99)
    )
  )

  private val rawData = AmendPensionsReliefsRawData(nino, taxYear, requestJson)
  private val requestData = AmendPensionsReliefsRequest(Nino(nino), taxYear, requestBody)

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new AmendPensionsReliefsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockAmendPensionsReliefsRequestParser,
      service = mockService,
      auditService = mockAuditService,
      hateoasFactory = mockHateoasFactory,
      cc = cc
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
  }

  val hateoasResponse = Json.parse(
    """
      |{
      |        "links": [
      |          {
      |            "href": "/individuals/reliefs/pensions/AA123456A/2019-20",
      |            "rel": "amend-reliefs-pensions",
      |            "method": "PUT"
      |          },
      |          {
      |            "href": "/individuals/reliefs/pensions/AA123456A/2019-20",
      |            "rel": "self",
      |            "method": "GET"
      |          },
      |          {
      |            "href": "/individuals/reliefs/pensions/AA123456A/2019-20",
      |            "rel": "delete-reliefs-pensions",
      |            "method": "DELETE"
      |          }
      |        ]
      |      }
      |""".stripMargin)


  def event(auditResponse: AuditResponse): AuditEvent[AmendPensionsReliefsAuditDetail] =
    AuditEvent(
      auditType = "CreateAmendReliefPension",
      transactionName = "create-amend-reliefs-pensions",
      detail = AmendPensionsReliefsAuditDetail(
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

        MockAmendPensionsReliefsRequestParser
          .parseRequest(rawData)
          .returns(Right(requestData))

        MockAmendReliefService
          .amend(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        MockHateoasFactory
          .wrap((), AmendPensionsReliefsHateoasData(nino, taxYear))
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

            MockAmendPensionsReliefsRequestParser
              .parseRequest(rawData)
              .returns(Left(ErrorWrapper(Some(correlationId), error, None)))

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
          (RuleTaxYearRangeInvalidError, BAD_REQUEST),
          (RuleTaxYearNotSupportedError, BAD_REQUEST),
          (RuleIncorrectOrEmptyBodyError, BAD_REQUEST),
          (ValueFormatError.copy(paths = Some(Seq("/path/to/field"))), BAD_REQUEST)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" should {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockAmendPensionsReliefsRequestParser
              .parseRequest(rawData)
              .returns(Right(requestData))

            MockAmendReliefService
              .amend(requestData)
              .returns(Future.successful(Left(ErrorWrapper(Some(correlationId), mtdError))))

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
          (NotFoundError, NOT_FOUND),
          (DownstreamError, INTERNAL_SERVER_ERROR),
          (TaxYearFormatError, BAD_REQUEST)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }
}