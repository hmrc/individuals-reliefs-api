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
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import v1.mocks.requestParsers.MockAmendPensionsReliefsRequestDataParser
import v1.mocks.services._
import v1.models.request.amendPensionsReliefs._
import v1.models.response.amendPensionsReliefs.AmendPensionsReliefsHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AmendPensionsReliefsControllerSpec
  extends ControllerBaseSpec
    with ControllerTestRunner
    with MockAmendPensionsReliefsService
    with MockAmendPensionsReliefsRequestDataParser
    with MockHateoasFactory
    with MockAppConfig
    with MockAuditService {

  private val taxYear = "2019-20"

  private val testHateoasLinks = Seq(
    Link(href = s"/individuals/reliefs/pensions/$nino/$taxYear", method = PUT, rel = "amend-reliefs-pensions"),
    api.hateoas.Link(href = s"/individuals/reliefs/pensions/$nino/$taxYear", method = GET, rel = "self"),
    api.hateoas.Link(href = s"/individuals/reliefs/pensions/$nino/$taxYear", method = DELETE, rel = "delete-reliefs-pensions")
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
  private val requestData = AmendPensionsReliefsRequestData(Nino(nino), TaxYear.fromMtd(taxYear), requestBody)

  val hateoasResponse: JsValue = Json.parse(
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

  "handleRequest" should {
    "return a successful response with status 200 (OK)" when {
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

        MockAmendPensionsReliefsRequestParser
          .parseRequest(rawData)
          .returns(Left(errors.ErrorWrapper(correlationId, NinoFormatError)))

        runErrorTestWithAudit(NinoFormatError, Some(requestJson))
      }

      "service errors occur" in new Test {

        MockAmendPensionsReliefsRequestParser
          .parseRequest(rawData)
          .returns(Right(requestData))

        MockAmendReliefService
          .amend(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTestWithAudit(RuleTaxYearNotSupportedError, maybeAuditRequestBody = Some(requestJson))
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetailOld] {

    val controller = new AmendPensionsReliefsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockAmendPensionsReliefsRequestParser,
      service = mockService,
      auditService = mockAuditService,
      hateoasFactory = mockHateoasFactory,
      appConfig = mockAppConfig,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.handleRequest(nino, taxYear)(fakePostRequest(requestJson))

    def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[GenericAuditDetailOld] =
      AuditEvent(
        auditType = "CreateAmendReliefPension",
        transactionName = "create-amend-reliefs-pensions",
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
