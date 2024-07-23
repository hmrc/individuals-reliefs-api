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

package v1.pensionReliefs.createAmend

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.hateoas.Method._
import api.hateoas.{HateoasWrapper, Link, MockHateoasFactory}
import api.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import api.models.domain.{Nino, TaxYear}
import api.models.errors.{ErrorWrapper, NinoFormatError, RuleTaxYearNotSupportedError}
import api.models.outcomes.ResponseWrapper
import api.services.MockAuditService
import mocks.MockAppConfig
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import v1.pensionReliefs.createAmend.model.request.{CreateAmendPensionsReliefsBody, CreateAmendPensionsReliefsRequestData, PensionReliefs}
import v1.pensionReliefs.createAmend.model.response.CreateAmendPensionsReliefsHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateAmendPensionsReliefsControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockCreateAmendPensionsReliefsService
    with MockCreateAmendPensionsReliefsValidatorFactory
    with MockHateoasFactory
    with MockAppConfig
    with MockAuditService {

  private val taxYear = "2019-20"

  private val testHateoasLinks = List(
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

  private val requestBody = CreateAmendPensionsReliefsBody(
    pensionReliefs = PensionReliefs(
      regularPensionContributions = Some(1999.99),
      oneOffPensionContributionsPaid = Some(1999.99),
      retirementAnnuityPayments = Some(1999.99),
      paymentToEmployersSchemeNoTaxRelief = Some(1999.99),
      overseasPensionSchemeContributions = Some(1999.99)
    )
  )

  private val requestData = CreateAmendPensionsReliefsRequestData(Nino(nino), TaxYear.fromMtd(taxYear), requestBody)

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
          .wrap((), CreateAmendPensionsReliefsHateoasData(nino, taxYear))
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
      appConfig = mockAppConfig,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.handleRequest(nino, taxYear)(fakePostRequest(requestJson))

    def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "CreateAmendReliefPension",
        transactionName = "create-amend-reliefs-pensions",
        detail = GenericAuditDetail(
          versionNumber = "1.0",
          userType = "Individual",
          agentReferenceNumber = None,
          params = Map("nino" -> nino, "taxYear" -> taxYear),
          requestBody = maybeRequestBody,
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )

  }

}
