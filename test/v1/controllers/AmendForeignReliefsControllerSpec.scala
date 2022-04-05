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

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import v1.models.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import v1.mocks.MockIdGenerator
import v1.mocks.hateoas.MockHateoasFactory
import v1.mocks.requestParsers.MockAmendForeignReliefsRequestParser
import v1.mocks.services._
import v1.models.audit.{AmendForeignReliefsAuditDetail, AuditError, AuditEvent, AuditResponse}
import v1.models.errors._
import v1.models.hateoas.Method.{DELETE, GET, PUT}
import v1.models.hateoas.{HateoasWrapper, Link}
import v1.models.outcomes.ResponseWrapper
import v1.models.request.amendForeignReliefs._
import v1.models.response.amendForeignReliefs.AmendForeignReliefsHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AmendForeignReliefsControllerSpec
    extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockAmendForeignReliefsService
    with MockAmendForeignReliefsRequestParser
    with MockHateoasFactory
    with MockAuditService
    with MockIdGenerator {

  private val nino          = "AA123456A"
  private val taxYear       = "2019-20"
  private val correlationId = "X-123"
  private val amount        = 1234.56

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new AmendForeignReliefsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockAmendForeignReliefsRequestParser,
      service = mockService,
      hateoasFactory = mockHateoasFactory,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.getCorrelationId.returns(correlationId)
  }

  private val testHateoasLinks = Seq(
    Link(href = s"/individuals/reliefs/foreign/$nino/$taxYear", method = GET, rel = "self"),
    Link(href = s"/individuals/reliefs/foreign/$nino/$taxYear", method = PUT, rel = "amend-reliefs-foreign"),
    Link(href = s"/individuals/reliefs/foreign/$nino/$taxYear", method = DELETE, rel = "delete-reliefs-foreign")
  )

  private val requestJson = Json.parse(
    s"""|
        |{
        |  "foreignTaxCreditRelief": {
        |    "amount": $amount
        |  },
        |  "foreignIncomeTaxCreditRelief": [
        |    {
        |      "countryCode": "FRA",
        |      "foreignTaxPaid": $amount,
        |      "taxableAmount": $amount,
        |      "employmentLumpSum": true
        |    }
        |  ],
        |  "foreignTaxForFtcrNotClaimed": {
        |    "amount": $amount
        |  }
        |}
        |""".stripMargin
  )

  private val requestBody = AmendForeignReliefsBody(
    foreignTaxCreditRelief = Some(
      ForeignTaxCreditRelief(
        amount = amount
      )),
    foreignIncomeTaxCreditRelief = Some(
      Seq(
        ForeignIncomeTaxCreditRelief(
          countryCode = "FRA",
          foreignTaxPaid = Some(amount),
          taxableAmount = amount,
          employmentLumpSum = true
        ))),
    foreignTaxForFtcrNotClaimed = Some(
      ForeignTaxForFtcrNotClaimed(
        amount = amount
      ))
  )

  val responseBody: JsValue = Json.parse(s"""
       |{
       |  "links": [
       |    {
       |      "href": "/individuals/reliefs/foreign/$nino/$taxYear",
       |      "method": "GET",
       |      "rel": "self"
       |    },
       |    {
       |      "href": "/individuals/reliefs/foreign/$nino/$taxYear",
       |      "method": "PUT",
       |      "rel": "amend-reliefs-foreign"
       |    },
       |    {
       |      "href": "/individuals/reliefs/foreign/$nino/$taxYear",
       |      "method": "DELETE",
       |      "rel": "delete-reliefs-foreign"
       |    }
       |  ]
       |}
       |""".stripMargin)

  def event(auditResponse: AuditResponse): AuditEvent[AmendForeignReliefsAuditDetail] =
    AuditEvent(
      auditType = "CreateAmendForeignReliefs",
      transactionName = "create-amend-foreign-reliefs",
      detail = AmendForeignReliefsAuditDetail(
        userType = "Individual",
        agentReferenceNumber = None,
        nino,
        taxYear,
        requestJson,
        correlationId,
        response = auditResponse
      )
    )

  private val rawData     = AmendForeignReliefsRawData(nino, taxYear, requestJson)
  private val requestData = AmendForeignReliefsRequest(Nino(nino), taxYear, requestBody)

  "handleRequest" should {
    "return Ok" when {
      "the request received is valid" in new Test {

        MockAmendForeignReliefsRequestParser
          .parseRequest(rawData)
          .returns(Right(requestData))

        MockAmendReliefService
          .amend(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        MockHateoasFactory
          .wrap((), AmendForeignReliefsHateoasData(nino, taxYear))
          .returns(HateoasWrapper((), testHateoasLinks))

        val result: Future[Result] = controller.handleRequest(nino, taxYear)(fakePostRequest(requestJson))
        status(result) shouldBe OK
        header("X-CorrelationId", result) shouldBe Some(correlationId)

        val auditResponse: AuditResponse = AuditResponse(OK, None, Some(responseBody))
        MockedAuditService.verifyAuditEvent(event(auditResponse)).once
      }
    }
    "return the error as per spec" when {
      "parser errors occur" should {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockAmendForeignReliefsRequestParser
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
          (CountryCodeFormatError, BAD_REQUEST),
          (RuleCountryCodeError, BAD_REQUEST)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" should {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockAmendForeignReliefsRequestParser
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
