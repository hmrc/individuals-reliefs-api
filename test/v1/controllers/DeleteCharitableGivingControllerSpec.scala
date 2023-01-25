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

import api.controllers.ControllerBaseSpec
import api.mocks.MockIdGenerator
import api.mocks.hateoas.MockHateoasFactory
import api.mocks.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import api.models.audit.AuditEvent
import api.models.domain.{Nino, TaxYear}
import api.models.errors
import api.models.errors.{
  BadRequestError,
  ErrorWrapper,
  InternalError,
  MtdError,
  NinoFormatError,
  NotFoundError,
  RuleTaxYearNotSupportedError,
  RuleTaxYearRangeInvalidError,
  TaxYearFormatError
}
import api.models.outcomes.ResponseWrapper
import play.api.libs.json.Json
import play.api.mvc.Result
import uk.gov.hmrc.http.HeaderCarrier
import v1.mocks.requestParsers.MockDeleteCharitableGivingReliefRequestParser
import v1.mocks.services._
import v1.models.audit.CharitableGivingReliefAuditDetail
import v1.models.request.deleteCharitableGivingTaxRelief.{DeleteCharitableGivingTaxReliefRawData, DeleteCharitableGivingTaxReliefRequest}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeleteCharitableGivingControllerSpec
    extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockDeleteCharitableGivingReliefService
    with MockAuditService
    with MockDeleteCharitableGivingReliefRequestParser
    with MockHateoasFactory
    with MockIdGenerator {

  private val nino          = "AA123456A"
  private val taxYear       = "2019-20"
  private val correlationId = "X-123"

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new DeleteCharitableGivingController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockRequestDataParser,
      service = mockDeleteCharitableGivingReliefService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.getCorrelationId.returns(correlationId)
  }

  private val rawData     = DeleteCharitableGivingTaxReliefRawData(nino, taxYear)
  private val requestData = DeleteCharitableGivingTaxReliefRequest(Nino(nino), TaxYear.fromMtd(taxYear))

  private def event(response: String, httpStatusCode: Int, errorCodes: Option[Seq[String]]): AuditEvent[CharitableGivingReliefAuditDetail] =
    AuditEvent(
      auditType = "DeleteCharitableGivingTaxRelief",
      transactionName = "delete-charitable-giving-tax-relief",
      detail = CharitableGivingReliefAuditDetail(
        versionNumber = "1.0",
        userType = "Individual",
        agentReferenceNumber = None,
        nino = nino,
        taxYear = taxYear,
        requestBody = None,
        `X-CorrelationId` = correlationId,
        response = response,
        httpStatusCode = httpStatusCode,
        errorCodes = errorCodes
      )
    )

  "handleRequest" should {
    "return NoContent" when {
      "the request received is valid" in new Test {

        MockDeleteCharitableGivingReliefRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockDeleteService
          .delete(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        val result: Future[Result] = controller.handleRequest(nino, taxYear)(fakeRequest)

        status(result) shouldBe NO_CONTENT
        header("X-CorrelationId", result) shouldBe Some(correlationId)

        MockedAuditService.verifyAuditEvent(event("success", NO_CONTENT, None)).once

      }
    }
    "return the error as per spec" when {
      "parser errors occur" should {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockDeleteCharitableGivingReliefRequestParser
              .parse(rawData)
              .returns(Left(errors.ErrorWrapper(correlationId, error, None)))

            val result: Future[Result] = controller.handleRequest(nino, taxYear)(fakeRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)

            MockedAuditService.verifyAuditEvent(event("error", expectedStatus, Some(Seq(error.code)))).once

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

            MockDeleteCharitableGivingReliefRequestParser
              .parse(rawData)
              .returns(Right(requestData))

            MockDeleteService
              .delete(requestData)
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

            val result: Future[Result] = controller.handleRequest(nino, taxYear)(fakeRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)

            MockedAuditService.verifyAuditEvent(event("error", expectedStatus, Some(Seq(mtdError.code)))).once

          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (NotFoundError, NOT_FOUND),
          (InternalError, INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }

}
