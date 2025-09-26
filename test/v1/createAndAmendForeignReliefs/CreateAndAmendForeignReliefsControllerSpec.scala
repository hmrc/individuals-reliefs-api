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

package v1.createAndAmendForeignReliefs

import play.api.Configuration
import play.api.libs.json.JsValue
import play.api.mvc.Result
import shared.config.MockSharedAppConfig
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.hateoas.Method._
import shared.hateoas.{HateoasWrapper, Link, MockHateoasFactory}
import shared.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import shared.models.domain.TaxYear
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import shared.services.MockAuditService
import v1.createAndAmendForeignReliefs.CreateAndAmendForeignReliefsFixtures.{requestBodyJson, requestBodyModel, responseWithHateoasLinks}
import v1.createAndAmendForeignReliefs.def1.model.request.Def1_CreateAndAmendForeignReliefsRequestData
import v1.createAndAmendForeignReliefs.model.response.CreateAndAmendForeignReliefsHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateAndAmendForeignReliefsControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockCreateAndAmendForeignReliefsService
    with MockCreateAndAmendForeignReliefsValidatorFactory
    with MockHateoasFactory
    with MockSharedAppConfig
    with MockAuditService {

  private val taxYear = "2019-20"

  private val testHateoasLinks = List(
    Link(href = s"/individuals/reliefs/foreign/$validNino/$taxYear", method = GET, rel = "self"),
    Link(href = s"/individuals/reliefs/foreign/$validNino/$taxYear", method = PUT, rel = "create-and-amend-reliefs-foreign"),
    Link(href = s"/individuals/reliefs/foreign/$validNino/$taxYear", method = DELETE, rel = "delete-reliefs-foreign")
  )

  private val requestData = Def1_CreateAndAmendForeignReliefsRequestData(parsedNino, TaxYear.fromMtd(taxYear), requestBodyModel)

  "handleRequest" should {
    "return a successful response with status 200 (OK)" when {
      "the request received is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockCreateAndAmendForeignReliefsService
          .createAndAmend(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        MockHateoasFactory
          .wrap((), CreateAndAmendForeignReliefsHateoasData(validNino, taxYear))
          .returns(HateoasWrapper((), testHateoasLinks))

        runOkTestWithAudit(
          expectedStatus = OK,
          maybeAuditRequestBody = Some(requestBodyJson),
          maybeExpectedResponseBody = Some(responseWithHateoasLinks(taxYear)),
          maybeAuditResponseBody = Some(responseWithHateoasLinks(taxYear))
        )
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))

        runErrorTestWithAudit(NinoFormatError, Some(requestBodyJson))
      }

      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockCreateAndAmendForeignReliefsService
          .createAndAmend(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTestWithAudit(RuleTaxYearNotSupportedError, maybeAuditRequestBody = Some(requestBodyJson))
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] {

    val controller: CreateAndAmendForeignReliefsController = new CreateAndAmendForeignReliefsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockCreateAndAmendForeignReliefsValidatorFactory,
      service = mockService,
      hateoasFactory = mockHateoasFactory,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedSharedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = controller.handleRequest(validNino, taxYear)(fakePostRequest(requestBodyJson))

    def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "CreateAmendForeignReliefs",
        transactionName = "create-amend-foreign-reliefs",
        detail = GenericAuditDetail(
          userType = "Individual",
          agentReferenceNumber = None,
          versionNumber = "1.0",
          params = Map("nino" -> validNino, "taxYear" -> taxYear),
          requestBody = maybeRequestBody,
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )

  }

}
