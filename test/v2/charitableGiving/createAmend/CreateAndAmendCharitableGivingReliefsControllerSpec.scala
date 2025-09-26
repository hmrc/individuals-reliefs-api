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

package v2.charitableGiving.createAmend

import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import shared.config.MockSharedAppConfig
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import shared.models.domain.TaxYear
import shared.models.errors
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import shared.utils.MockIdGenerator
import v2.charitableGiving.createAmend.def1.model.request.{
  Def1_CreateAndAmendCharitableGivingTaxReliefsBody,
  Def1_GiftAidPayments,
  Def1_NonUkCharities
}
import v2.charitableGiving.createAmend.model.request.Def1_CreateAndAmendCharitableGivingTaxReliefsRequestData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateAndAmendCharitableGivingReliefsControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockCreateAndAmendCharitableGivingReliefsService
    with MockCreateAndAmendCharitableGivingReliefsValidatorFactory
    with MockIdGenerator
    with MockSharedAppConfig {

  private val taxYear = "2019-20"
  private val amount  = 1234.56

  private val nonUkCharities = Def1_NonUkCharities(Some(List("non-UK charity 1", "non-UK charity 2")), amount)

  private val giftAidPayments =
    Def1_GiftAidPayments(Some(nonUkCharities), Some(amount), Some(amount), Some(amount), Some(amount))

  private val requestJson = Json.parse(
    s"""|{
        |  "giftAidPayments": {
        |    "totalAmount": $amount
        |  }
        |}
        |""".stripMargin
  )

  private val requestBody = Def1_CreateAndAmendCharitableGivingTaxReliefsBody(Some(giftAidPayments), None)

  private val requestData = Def1_CreateAndAmendCharitableGivingTaxReliefsRequestData(parsedNino, TaxYear.fromMtd(taxYear), requestBody)

  "handleRequest" should {
    "return a successful response with status 204 (No Content)" when {
      "the request received is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockAmendReliefService
          .amend(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        runOkTestWithAudit(
          expectedStatus = NO_CONTENT,
          maybeAuditRequestBody = Some(requestJson),
          maybeExpectedResponseBody = None,
          maybeAuditResponseBody = None
        )
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))

        runErrorTestWithAudit(NinoFormatError, Some(requestJson))

      }

      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockAmendReliefService
          .amend(requestData)
          .returns(Future.successful(Left(errors.ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTestWithAudit(RuleTaxYearNotSupportedError, maybeAuditRequestBody = Some(requestJson))
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] {

    val controller: CreateAndAmendCharitableGivingReliefsController = new CreateAndAmendCharitableGivingReliefsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockCreateAndAmendCharitableGivingReliefsValidatorFactory,
      service = mockService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedSharedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = controller.handleRequest(validNino, taxYear)(fakePostRequest(requestJson))

    def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "CreateAndAmendCharitableGivingTaxRelief",
        transactionName = "create-and-amend-charitable-giving-tax-relief",
        detail = GenericAuditDetail(
          versionNumber = "2.0",
          userType = "Individual",
          agentReferenceNumber = None,
          params = Map("nino" -> validNino, "taxYear" -> taxYear),
          requestBody = maybeRequestBody,
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )

  }

}
