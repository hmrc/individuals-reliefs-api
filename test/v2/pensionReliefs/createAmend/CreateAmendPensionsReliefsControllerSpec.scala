/*
 * Copyright 2024 HM Revenue & Customs
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

package v2.pensionReliefs.createAmend

import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import shared.config.MockSharedAppConfig
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.hateoas.Method._
import shared.hateoas.{HateoasWrapper, Link, MockHateoasFactory}
import shared.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import shared.models.domain.TaxYear
import shared.models.errors.{ErrorWrapper, NinoFormatError, RuleTaxYearNotSupportedError}
import shared.models.outcomes.ResponseWrapper
import shared.services.MockAuditService
import v2.pensionReliefs.createAmend.def1.model.request.{CreateAmendPensionsReliefsBody, Def1_CreateAmendPensionsReliefsRequestData, PensionReliefs}
import v2.pensionReliefs.createAmend.model.response.CreateAmendPensionsReliefsHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateAmendPensionsReliefsControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockCreateAmendPensionsReliefsService
    with MockCreateAmendPensionsReliefsValidatorFactory
    with MockHateoasFactory
    with MockSharedAppConfig
    with MockAuditService {

  private val taxYear = "2019-20"

  private val testHateoasLinks = List(
    Link(href = s"/individuals/reliefs/pensions/$validNino/$taxYear", method = PUT, rel = "amend-reliefs-pensions"),
    Link(href = s"/individuals/reliefs/pensions/$validNino/$taxYear", method = GET, rel = "self"),
    Link(href = s"/individuals/reliefs/pensions/$validNino/$taxYear", method = DELETE, rel = "delete-reliefs-pensions")
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

  private val requestBody = CreateAmendPensionsReliefsBody(
    pensionReliefs = PensionReliefs(
      regularPensionContributions = Some(1999.99),
      oneOffPensionContributionsPaid = Some(1999.99),
      retirementAnnuityPayments = Some(1999.99),
      paymentToEmployersSchemeNoTaxRelief = Some(1999.99),
      overseasPensionSchemeContributions = Some(1999.99)
    )
  )

  private val requestData = Def1_CreateAmendPensionsReliefsRequestData(parsedNino, TaxYear.fromMtd(taxYear), requestBody)

  private val hateoasResponse = Json.parse("""
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

  "handleRequest" should {
    "return a successful response with status 200 (OK)" when {
      "the request received is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockAmendReliefService
          .amend(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        MockHateoasFactory
          .wrap((), CreateAmendPensionsReliefsHateoasData(validNino, taxYear))
          .returns(HateoasWrapper((), testHateoasLinks))

        runOkTestWithAudit(
          expectedStatus = OK,
          maybeAuditRequestBody = Some(requestJson),
          maybeExpectedResponseBody = Some(hateoasResponse),
          maybeAuditResponseBody = Some(hateoasResponse)
        )
      }
    }

    "return the error as per spec" when {
      "parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))

        runErrorTestWithAudit(NinoFormatError, Some(requestJson))
      }

      "service errors occur" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockAmendReliefService
          .amend(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTestWithAudit(RuleTaxYearNotSupportedError, maybeAuditRequestBody = Some(requestJson))
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] {

    val controller = new CreateAmendPensionsReliefsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockAmendPensionsReliefsValidatorFactory,
      service = mockService,
      auditService = mockAuditService,
      hateoasFactory = mockHateoasFactory,
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
        auditType = "CreateAmendReliefPension",
        transactionName = "create-amend-reliefs-pensions",
        detail = GenericAuditDetail(
          versionNumber = "1.0",
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
