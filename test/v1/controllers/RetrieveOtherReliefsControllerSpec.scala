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
import api.mocks.hateoas.MockHateoasFactory
import api.models.domain.{Nino, TaxYear}
import api.models.errors.{ErrorWrapper, NinoFormatError, RuleTaxYearNotSupportedError}
import api.models.hateoas.HateoasWrapper
import api.models.hateoas.Method.GET
import api.models.outcomes.ResponseWrapper
import api.models.{errors, hateoas}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import v1.mocks.requestParsers.MockRetrieveOtherReliefsRequestParser
import v1.mocks.services._
import v1.models.request.retrieveOtherReliefs.{RetrieveOtherReliefsRawData, RetrieveOtherReliefsRequest}
import v1.models.response.retrieveOtherReliefs._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveOtherReliefsControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockRetrieveOtherReliefsService
    with MockRetrieveOtherReliefsRequestParser
    with MockHateoasFactory {

  private val taxYear     = "2019-20"
  private val rawData     = RetrieveOtherReliefsRawData(nino, taxYear)
  private val requestData = RetrieveOtherReliefsRequest(Nino(nino), TaxYear.fromMtd(taxYear))

  private val testHateoasLink = hateoas.Link(href = s"individuals/reliefs/other/$nino/$taxYear", method = GET, rel = "self")

  private val responseBody = RetrieveOtherReliefsResponse(
    "2020-06-17T10:53:38Z",
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
         |   "submittedOn":"2020-06-17T10:53:38Z",
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
         |   ],
         |   "links":[
         |      {
         |         "href":"individuals/reliefs/other/AA123456A/2019-20",
         |         "method":"GET",
         |         "rel":"self"
         |      }
         |   ]
         |}
        """.stripMargin
    )

  "handleRequest" should {
    "return Ok" when {
      "the request received is valid" in new Test {

        MockRetrieveOtherReliefsRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockRetrieveReliefService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseBody))))

        MockHateoasFactory
          .wrap(responseBody, RetrieveOtherReliefsHateoasData(nino, taxYear))
          .returns(HateoasWrapper(responseBody, Seq(testHateoasLink)))

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(mtdResponseJson))
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        MockRetrieveOtherReliefsRequestParser
          .parse(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError)))

        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        MockRetrieveOtherReliefsRequestParser
          .parse(rawData)
          .returns(Right(requestData))

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
      parser = mockRequestDataParser,
      service = mockService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.handleRequest(nino, taxYear)(fakeGetRequest)
  }

}
