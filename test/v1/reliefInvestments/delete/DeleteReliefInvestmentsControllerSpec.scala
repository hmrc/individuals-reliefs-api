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

package v1.reliefInvestments.delete

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import api.models.domain.{Nino, TaxYear}
import api.models.errors.{ErrorWrapper, NinoFormatError, RuleTaxYearNotSupportedError}
import api.models.outcomes.ResponseWrapper
import api.services.MockAuditService
import mocks.MockAppConfig
import play.api.Configuration
import play.api.libs.json.JsValue
import play.api.mvc.Result
import v1.reliefInvestments.delete.def1.Def1_DeleteReliefInvestmentsRequestData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeleteReliefInvestmentsControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockDeleteReliefInvestmentsService
    with MockDeleteReliefInvestmentsValidatorFactory
    with MockAuditService
    with MockAppConfig {

  private val taxYear     = "2019-20"
  private val requestData = Def1_DeleteReliefInvestmentsRequestData(Nino(nino), TaxYear.fromMtd(taxYear))

  "handleRequest" should {
    "return a successful response with status 204 (No Content)" when {
      "the request received is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockDeleteService
          .delete(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        runOkTestWithAudit(expectedStatus = NO_CONTENT)
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))

        runErrorTestWithAudit(NinoFormatError)
      }

      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockDeleteService
          .delete(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTestWithAudit(RuleTaxYearNotSupportedError)
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] {

    val controller = new DeleteReliefInvestmentsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockDeleteReliefInvestmentsValidatorFactory,
      service = mockService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedAppConfig.featureSwitches.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false
    protected def callController(): Future[Result] = controller.handleRequest(nino, taxYear)(fakeRequest)

    def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "DeleteReliefsInvestment",
        transactionName = "delete-reliefs-investment",
        detail = GenericAuditDetail(
          versionNumber = "1.0",
          userType = "Individual",
          agentReferenceNumber = None,
          params = Map("nino" -> nino, "taxYear" -> taxYear),
          requestBody = None,
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )

  }

}
