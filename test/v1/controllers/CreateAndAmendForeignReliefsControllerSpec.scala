/*
 * Copyright 2023 HM Revenue & Customs
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

import api.models.audit.{AuditError, AuditEvent, AuditResponse}
import api.models.domain.{Nino, TaxYear}
import api.models.errors.{
  BadRequestError,
  CountryCodeFormatError,
  ErrorWrapper,
  InternalError,
  MtdError,
  NinoFormatError,
  RuleCountryCodeError,
  RuleTaxYearNotSupportedError,
  RuleTaxYearRangeInvalidError,
  TaxYearFormatError,
  ValueFormatError
}
import api.models.hateoas
import api.models.hateoas.HateoasWrapper
import api.models.hateoas.Method.{DELETE, GET, PUT}
import api.models.outcomes.ResponseWrapper
import play.api.libs.json.Json
import play.api.mvc.Result
import uk.gov.hmrc.http.HeaderCarrier
import v1.fixtures.CreateAndAmendForeignReliefsFixtures.{requestBodyJson, requestBodyModel, responseWithHateoasLinks}
import v1.mocks.MockIdGenerator
import v1.mocks.hateoas.MockHateoasFactory
import v1.mocks.requestParsers.MockCreateAndAmendForeignReliefsRequestParser
import v1.mocks.services._
import v1.models.audit.CreateAndAmendForeignReliefsAuditDetail
import v1.models.request.createAndAmendForeignReliefs._
import v1.models.response.createAndAmendForeignReliefs.CreateAndAmendForeignReliefsHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateAndAmendForeignReliefsControllerSpec
    extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockCreateAndAmendForeignReliefsService
    with MockCreateAndAmendForeignReliefsRequestParser
    with MockHateoasFactory
    with MockAuditService
    with MockIdGenerator {

  private val nino          = "AA123456A"
  private val taxYear       = "2019-20"
  private val correlationId = "X-123"

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new CreateAndAmendForeignReliefsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockCreateAndAmendForeignReliefsRequestParser,
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
    hateoas.Link(href = s"/individuals/reliefs/foreign/$nino/$taxYear", method = GET, rel = "self"),
    hateoas.Link(href = s"/individuals/reliefs/foreign/$nino/$taxYear", method = PUT, rel = "create-and-amend-reliefs-foreign"),
    hateoas.Link(href = s"/individuals/reliefs/foreign/$nino/$taxYear", method = DELETE, rel = "delete-reliefs-foreign")
  )

  def event(auditResponse: AuditResponse): AuditEvent[CreateAndAmendForeignReliefsAuditDetail] =
    AuditEvent(
      auditType = "CreateAmendForeignReliefs",
      transactionName = "create-amend-foreign-reliefs",
      detail = CreateAndAmendForeignReliefsAuditDetail(
        userType = "Individual",
        agentReferenceNumber = None,
        nino,
        taxYear,
        requestBodyJson,
        correlationId,
        response = auditResponse
      )
    )

  private val rawData     = CreateAndAmendForeignReliefsRawData(nino, taxYear, requestBodyJson)
  private val requestData = CreateAndAmendForeignReliefsRequest(Nino(nino), TaxYear.fromMtd(taxYear), requestBodyModel)

  "handleRequest" should {
    "return Ok" when {
      "the request received is valid" in new Test {

        MockCreateAndAmendForeignReliefsRequestParser
          .parseRequest(rawData)
          .returns(Right(requestData))

        MockCreateAndAmendForeignReliefsService
          .createAndAmend(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        MockHateoasFactory
          .wrap((), CreateAndAmendForeignReliefsHateoasData(nino, taxYear))
          .returns(HateoasWrapper((), testHateoasLinks))

        val result: Future[Result] = controller.handleRequest(nino, taxYear)(fakePostRequest(requestBodyJson))
        status(result) shouldBe OK
        header("X-CorrelationId", result) shouldBe Some(correlationId)

        val auditResponse: AuditResponse = AuditResponse(OK, None, Some(responseWithHateoasLinks(taxYear)))
        MockedAuditService.verifyAuditEvent(event(auditResponse)).once
      }
    }
    "return the error as per spec" when {
      "parser errors occur" should {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockCreateAndAmendForeignReliefsRequestParser
              .parseRequest(rawData)
              .returns(Left(ErrorWrapper(correlationId, error, None)))

            val result: Future[Result] = controller.handleRequest(nino, taxYear)(fakePostRequest(requestBodyJson))

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

            MockCreateAndAmendForeignReliefsRequestParser
              .parseRequest(rawData)
              .returns(Right(requestData))

            MockCreateAndAmendForeignReliefsService
              .createAndAmend(requestData)
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

            val result: Future[Result] = controller.handleRequest(nino, taxYear)(fakePostRequest(requestBodyJson))

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)

            val auditResponse: AuditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(mtdError.code))), None)
            MockedAuditService.verifyAuditEvent(event(auditResponse)).once
          }
        }

        val errors = List(
          (NinoFormatError, BAD_REQUEST),
          (InternalError, INTERNAL_SERVER_ERROR),
          (TaxYearFormatError, BAD_REQUEST)
        )

        val extraTysErrors = List(
          (RuleTaxYearNotSupportedError, BAD_REQUEST)
        )

        (errors ++ extraTysErrors).foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }

}
