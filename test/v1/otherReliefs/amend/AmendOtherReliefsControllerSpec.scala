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

package v1.otherReliefs.amend

import common.RuleSubmissionFailedError
import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
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
import v1.otherReliefs.amend.def1.model.request._
import v1.otherReliefs.amend.model.response.AmendOtherReliefsHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AmendOtherReliefsControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockAmendOtherReliefsService
    with MockAmendOtherReliefsValidatorFactory
    with MockHateoasFactory
    with MockSharedAppConfig
    with MockAuditService {

  private val taxYear = "2019-20"

  private val testHateoasLinks = List(
    Link(href = s"/individuals/reliefs/other/$validNino/$taxYear", method = PUT, rel = "amend-reliefs-other"),
    Link(href = s"/individuals/reliefs/other/$validNino/$taxYear", method = GET, rel = "self"),
    Link(href = s"/individuals/reliefs/other/$validNino/$taxYear", method = DELETE, rel = "delete-reliefs-other")
  )

  private val requestJson = Json.parse("""
                                         |{
                                         |  "nonDeductibleLoanInterest": {
                                         |        "customerReference": "myref",
                                         |        "reliefClaimed": 763.00
                                         |      },
                                         |  "payrollGiving": {
                                         |        "customerReference": "myref",
                                         |        "reliefClaimed": 154.00
                                         |      },
                                         |  "qualifyingDistributionRedemptionOfSharesAndSecurities": {
                                         |        "customerReference": "myref",
                                         |        "amount": 222.22
                                         |      },
                                         |  "maintenancePayments": [
                                         |    {
                                         |        "customerReference": "myref",
                                         |        "exSpouseName" : "Hilda",
                                         |        "exSpouseDateOfBirth": "2000-01-01",
                                         |        "amount": 222.22
                                         |      }
                                         |  ],
                                         |  "postCessationTradeReliefAndCertainOtherLosses": [
                                         |    {
                                         |        "customerReference": "myref",
                                         |        "businessName": "ACME Inc",
                                         |        "dateBusinessCeased": "2019-08-10",
                                         |        "natureOfTrade": "Widgets Manufacturer",
                                         |        "incomeSource": "AB12412/A12",
                                         |        "amount": 222.22
                                         |      }
                                         |  ],
                                         |  "annualPaymentsMade": {
                                         |        "customerReference": "myref",
                                         |        "reliefClaimed": 763.00
                                         |      },
                                         |  "qualifyingLoanInterestPayments": [
                                         |    {
                                         |        "customerReference": "myref",
                                         |        "lenderName": "Maurice",
                                         |        "reliefClaimed": 763.00
                                         |      }
                                         |  ]
                                         |}""".stripMargin)

  private val requestBody = Def1_AmendOtherReliefsRequestBody(
    Some(NonDeductibleLoanInterest(Some("myref"), 763.00)),
    Some(PayrollGiving(Some("myref"), 154.00)),
    Some(QualifyingDistributionRedemptionOfSharesAndSecurities(Some("myref"), 222.22)),
    Some(Seq(MaintenancePayments(Some("myref"), Some("Hilda"), Some("2000-01-01"), 222.22))),
    Some(
      Seq(
        PostCessationTradeReliefAndCertainOtherLosses(
          Some("myref"),
          Some("ACME Inc"),
          Some("2019-08-10"),
          Some("Widgets Manufacturer"),
          Some("AB12412/A12"),
          222.22))),
    Some(AnnualPaymentsMade(Some("myref"), 763.00)),
    Some(Seq(QualifyingLoanInterestPayments(Some("myref"), Some("Maurice"), 763.00)))
  )

  private val requestData = Def1_AmendOtherReliefsRequestData(parsedNino, TaxYear.fromMtd(taxYear), requestBody)

  val hateoasResponse: JsValue = Json.parse("""
                                              |{
                                              |   "links":[
                                              |      {
                                              |         "href":"/individuals/reliefs/other/AA123456A/2019-20",
                                              |         "method":"PUT",
                                              |         "rel":"amend-reliefs-other"
                                              |      },
                                              |      {
                                              |         "href":"/individuals/reliefs/other/AA123456A/2019-20",
                                              |         "method":"GET",
                                              |         "rel":"self"
                                              |      },
                                              |      {
                                              |         "href":"/individuals/reliefs/other/AA123456A/2019-20",
                                              |         "method":"DELETE",
                                              |         "rel":"delete-reliefs-other"
                                              |      }
                                              |   ]
                                              |}
                                              |""".stripMargin)

  "handleRequest" should {
    "return a successful response with status 200 (OK)" when {
      "the request received is valid" in new Test {

        willUseValidator(returningSuccess(requestData))

        MockAmendOtherReliefsService
          .amend(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        MockHateoasFactory
          .wrap((), AmendOtherReliefsHateoasData(validNino, taxYear))
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
      "the parser validation fails" in new Test {

        willUseValidator(returning(NinoFormatError))

        runErrorTestWithAudit(NinoFormatError, Some(requestJson))

      }

      "the service returns an error" in new Test {

        willUseValidator(returningSuccess(requestData))

        MockAmendOtherReliefsService
          .amend(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleSubmissionFailedError))))

        runErrorTestWithAudit(RuleSubmissionFailedError, maybeAuditRequestBody = Some(requestJson))
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] {

    val controller = new AmendOtherReliefsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockAmendOtherReliefsValidatorFactory,
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

    protected def callController(): Future[Result] = controller.handleRequest(validNino, taxYear)(fakePostRequest(requestJson))

    def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "CreateAmendOtherReliefs",
        transactionName = "create-amend-other-reliefs",
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
