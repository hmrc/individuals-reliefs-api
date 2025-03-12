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

package v2.otherReliefs.retrieve

import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import shared.config.MockSharedAppConfig
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.models.domain.{TaxYear, Timestamp}
import shared.models.errors
import shared.models.errors.{NinoFormatError, RuleTaxYearNotSupportedError}
import shared.models.outcomes.ResponseWrapper
import v2.otherReliefs.retrieve.def1.model.request.Def1_RetrieveOtherReliefsRequestData
import v2.otherReliefs.retrieve.def1.model.response._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveOtherReliefsControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockRetrieveOtherReliefsService
    with MockRetrieveOtherReliefsValidatorFactory
    with MockSharedAppConfig {

  private val taxYear     = "2019-20"
  private val requestData = Def1_RetrieveOtherReliefsRequestData(parsedNino, TaxYear.fromMtd(taxYear))

  private val responseBody = Def1_RetrieveOtherReliefsResponse(
    Timestamp("2020-06-17T10:53:38.000Z"),
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

  val mtdResponseJson: JsValue = Json
    .parse(
      s"""
         |{
         |   "submittedOn":"2020-06-17T10:53:38.000Z",
         |   "nonDeductibleLoanInterest":{
         |      "customerReference":"myref",
         |      "reliefClaimed":763
         |   },
         |   "payrollGiving":{
         |      "customerReference":"myref",
         |      "reliefClaimed":154
         |   },
         |   "qualifyingDistributionRedemptionOfSharesAndSecurities":{
         |      "customerReference":"myref",
         |      "amount":222.22
         |   },
         |   "maintenancePayments":[
         |      {
         |         "customerReference":"myref",
         |         "exSpouseName":"Hilda",
         |         "exSpouseDateOfBirth":"2000-01-01",
         |         "amount":222.22
         |      }
         |   ],
         |   "postCessationTradeReliefAndCertainOtherLosses":[
         |      {
         |         "customerReference":"myref",
         |         "businessName":"ACME Inc",
         |         "dateBusinessCeased":"2019-08-10",
         |         "natureOfTrade":"Widgets Manufacturer",
         |         "incomeSource":"AB12412/A12",
         |         "amount":222.22
         |      }
         |   ],
         |   "annualPaymentsMade":{
         |      "customerReference":"myref",
         |      "reliefClaimed":763
         |   },
         |   "qualifyingLoanInterestPayments":[
         |      {
         |         "customerReference":"myref",
         |         "lenderName":"Maurice",
         |         "reliefClaimed":763
         |      }
         |   ]
         |}
        """.stripMargin
    )

  "handleRequest" should {
    "return Ok" when {
      "the request received is valid" in new Test {

        willUseValidator(returningSuccess(requestData))

        MockRetrieveReliefService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseBody))))

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(mtdResponseJson))
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))

        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrieveReliefService
          .retrieve(requestData)
          .returns(Future.successful(Left(errors.ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTest(RuleTaxYearNotSupportedError)
      }
    }
  }

  trait Test extends ControllerTest {

    val controller = new RetrieveOtherReliefsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockRetrieveOtherReliefsValidatorFactory,
      service = mockService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedSharedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = controller.handleRequest(validNino, taxYear)(fakeGetRequest)
  }

}
