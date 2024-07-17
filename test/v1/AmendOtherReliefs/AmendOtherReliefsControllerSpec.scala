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

package v1.AmendOtherReliefs

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.hateoas.Method._
import api.hateoas.{HateoasWrapper, Link, MockHateoasFactory}
import api.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.MockAuditService
import mocks.MockAppConfig
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import v1.AmendOtherReliefs.def1.model.request._
import v1.AmendOtherReliefs.model.request.Def1_AmendOtherReliefsRequestData
import v1.models.response.amendOtherReliefs.AmendOtherReliefsHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AmendOtherReliefsControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockAmendOtherReliefsService
    with MockAmendOtherReliefsValidatorFactory
    with MockHateoasFactory
    with MockAppConfig
    with MockAuditService {

  private val taxYear = "2019-20"

  private val testHateoasLinks = List(
    Link(href = s"/individuals/reliefs/other/$nino/$taxYear", method = PUT, rel = "amend-reliefs-other"),
    Link(href = s"/individuals/reliefs/other/$nino/$taxYear", method = GET, rel = "self"),
    Link(href = s"/individuals/reliefs/other/$nino/$taxYear", method = DELETE, rel = "delete-reliefs-other")
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
    Some(Def1_NonDeductibleLoanInterest(Some("myref"), 763.00)),
    Some(Def1_PayrollGiving(Some("myref"), 154.00)),
    Some(Def1_QualifyingDistributionRedemptionOfSharesAndSecurities(Some("myref"), 222.22)),
    Some(Seq(Def1_MaintenancePayments(Some("myref"), Some("Hilda"), Some("2000-01-01"), 222.22))),
    Some(
      Seq(
        Def1_PostCessationTradeReliefAndCertainOtherLosses(
          Some("myref"),
          Some("ACME Inc"),
          Some("2019-08-10"),
          Some("Widgets Manufacturer"),
          Some("AB12412/A12"),
          222.22))),
    Some(Def1_AnnualPaymentsMade(Some("myref"), 763.00)),
    Some(Seq(Def1_QualifyingLoanInterestPayments(Some("myref"), Some("Maurice"), 763.00)))
  )

  private val requestData = Def1_AmendOtherReliefsRequestData(Nino(nino), TaxYear.fromMtd(taxYear), requestBody)

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
          .wrap((), AmendOtherReliefsHateoasData(nino, taxYear))
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
      appConfig = mockAppConfig,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.handleRequest(nino, taxYear)(fakePostRequest(requestJson))

    def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "CreateAmendOtherReliefs",
        transactionName = "create-amend-other-reliefs",
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
