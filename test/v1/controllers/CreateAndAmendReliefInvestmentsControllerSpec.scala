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

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.hateoas.Method._
import api.hateoas.{HateoasWrapper, Link, MockHateoasFactory}
import api.models.audit.{AuditEvent, AuditResponse, GenericAuditDetailOld}
import api.models.domain.{Nino, TaxYear}
import api.models.errors
import api.models.errors.{ErrorWrapper, NinoFormatError, RuleTaxYearNotSupportedError}
import api.models.outcomes.ResponseWrapper
import api.services.MockAuditService
import mocks.MockAppConfig
import play.api.libs.json.JsValue
import play.api.mvc.Result
import v1.fixtures.CreateAndAmendReliefInvestmentsFixtures.{hateoasResponse, requestBodyJson, requestBodyModel}
import v1.mocks.requestParsers.MockCreateAndAmendReliefInvestmentsRequestParser
import v1.mocks.services._
import v1.models.request.createAndAmendReliefInvestments._
import v1.models.response.createAndAmendReliefInvestments.CreateAndAmendReliefInvestmentsHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateAndAmendReliefInvestmentsControllerSpec
  extends ControllerBaseSpec
    with ControllerTestRunner
    with MockCreateAndAmendReliefInvestmentsService
    with MockCreateAndAmendReliefInvestmentsRequestParser
    with MockHateoasFactory
    with MockAuditService
    with MockAppConfig {

  private val taxYear = "2019-20"

  private val testHateoasLinks: Seq[Link] = Seq(
    api.hateoas.Link(href = s"/individuals/reliefs/investment/$nino/$taxYear", method = GET, rel = "self"),
    api.hateoas.Link(href = s"/individuals/reliefs/investment/$nino/$taxYear", method = PUT, rel = "create-and-amend-reliefs-investments"),
    api.hateoas.Link(href = s"/individuals/reliefs/investment/$nino/$taxYear", method = DELETE, rel = "delete-reliefs-investments")
  )

  private val rawData = CreateAndAmendReliefInvestmentsRawData(nino, taxYear, requestBodyJson)
  private val requestData = CreateAndAmendReliefInvestmentsRequest(Nino(nino), TaxYear.fromMtd(taxYear), requestBodyModel)

  "handleRequest" should {
    "return a successful response with status 200 (OK)" when {
      "the request received is valid" in new Test {

        MockCreateAndAmendReliefInvestmentsRequestParser
          .parseRequest(rawData)
          .returns(Right(requestData))

        MockCreateAndAmendReliefService
          .amend(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        MockHateoasFactory
          .wrap((), CreateAndAmendReliefInvestmentsHateoasData(nino, taxYear))
          .returns(HateoasWrapper((), testHateoasLinks))

        runOkTestWithAudit(
          expectedStatus = OK,
          maybeAuditRequestBody = Some(requestBodyJson),
          maybeExpectedResponseBody = Some(hateoasResponse()),
          maybeAuditResponseBody = Some(hateoasResponse())
        )
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {

        MockCreateAndAmendReliefInvestmentsRequestParser
          .parseRequest(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError)))

        runErrorTestWithAudit(NinoFormatError, Some(requestBodyJson))

      }

      "the service returns an error" in new Test {

        MockCreateAndAmendReliefInvestmentsRequestParser
          .parseRequest(rawData)
          .returns(Right(requestData))

        MockCreateAndAmendReliefService
          .amend(requestData)
          .returns(Future.successful(Left(errors.ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTestWithAudit(RuleTaxYearNotSupportedError, maybeAuditRequestBody = Some(requestBodyJson))
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetailOld] {

    val controller = new CreateAndAmendReliefInvestmentsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockCreateAndAmendReliefInvestmentsRequestParser,
      service = mockService,
      auditService = mockAuditService,
      hateoasFactory = mockHateoasFactory,
      appConfig = mockAppConfig,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.handleRequest(nino, taxYear)(fakePostRequest(requestBodyJson))

    def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[GenericAuditDetailOld] =
      AuditEvent(
        auditType = "CreateAmendReliefsInvestment",
        transactionName = "create-amend-reliefs-investment",
        detail = GenericAuditDetailOld(
          userType = "Individual",
          agentReferenceNumber = None,
          pathParams = Map("nino" -> nino, "taxYear" -> taxYear),
          queryParams = None,
          requestBody = maybeRequestBody,
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )

  }

}
