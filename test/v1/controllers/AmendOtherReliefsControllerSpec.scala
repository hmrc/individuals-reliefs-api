/*
 * Copyright 2020 HM Revenue & Customs
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

import play.api.libs.json.Json
import play.api.mvc.Result
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import v1.mocks.hateoas.MockHateoasFactory
import v1.mocks.requestParsers.MockAmendOtherReliefsRequestParser
import v1.mocks.services.{MockAmendOtherReliefsService, MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import v1.models.errors._
import v1.models.hateoas.Method.PUT
import v1.models.hateoas.{HateoasWrapper, Link}
import v1.models.outcomes.ResponseWrapper
import v1.models.request.amendOtherReliefs._
import v1.models.response.amendOtherReliefs.AmendOtherReliefsHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class AmendOtherReliefsControllerSpec
  extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockAmendOtherReliefsService
    with MockAmendOtherReliefsRequestParser
    with MockHateoasFactory
    with MockAuditService {

  trait Test {
    val hc = HeaderCarrier()

    val controller = new AmendOtherReliefsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockAmendOtherReliefsRequestParser,
      service = mockService,
      hateoasFactory = mockHateoasFactory,
      cc = cc
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
  }

  private val nino = "AA123456A"
  private val taxYear = "2021-22"
  private val correlationId = "X-123"

  private val testHateoasLink = Link(href = s"individuals/reliefs/other/$nino/$taxYear", method = PUT, rel = "self")

  private val requestJson = Json.parse(
    """
      |{
      |  "nonDeductableLoanInterest": {
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

  private val requestBody = AmendOtherReliefsBody(
    Some(NonDeductableLoanInterest(
    Some("myref"),
    763.00)),
    Some(PayrollGiving(
      Some("myref"),
      154.00)),
    Some(QualifyingDistributionRedemptionOfSharesAndSecurities(
      Some("myref"),
      222.22)),
    Some(Seq(MaintenancePayments(
      "myref",
      Some("Hilda"),
      Some("2000-01-01"),
      Some(222.22)))),
    Some(Seq(PostCessationTradeReliefAndCertainOtherLosses(
      "myref",
      Some("ACME Inc"),
      Some("2019-08-10"),
      Some("Widgets Manufacturer"),
      Some("AB12412/A12"),
      Some(222.22)))),
    Some(AnnualPaymentsMade(
      Some("myref"),
      763.00)),
    Some(Seq(QualifyingLoanInterestPayments(
      "myref",
      Some("Maurice"),
      763.00)))
  )

  private val rawData = AmendOtherReliefsRawData(nino, taxYear, requestJson)
  private val requestData = AmendOtherReliefsRequest(Nino(nino), taxYear, requestBody)


  "handleRequest" should {
    "return OK" when {
      "the request received is valid" in new Test {

        MockAmendOtherReliefsRequestParser
          .parseRequest(rawData)
          .returns(Right(requestData))

        MockAmendOtherReliefsService
          .amend(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        MockHateoasFactory
          .wrap((), AmendOtherReliefsHateoasData(nino, taxYear))
          .returns(HateoasWrapper((), Seq(testHateoasLink)))

        val result: Future[Result] = controller.handleRequest(nino, taxYear)(fakePostRequest(requestJson))
        status(result) shouldBe OK
        header("X-CorrelationId", result) shouldBe Some(correlationId)
      }
    }
    "return the error as per spec" when {
      "parser errors occur" should {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockAmendOtherReliefsRequestParser
              .parseRequest(rawData)
              .returns(Left(ErrorWrapper(Some(correlationId), error, None)))

            val result: Future[Result] = controller.handleRequest(nino, taxYear)(fakePostRequest(requestJson))

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (BadRequestError, BAD_REQUEST),
          (NinoFormatError, BAD_REQUEST),
          (RuleTaxYearRangeInvalidError, BAD_REQUEST),
          (ValueFormatError.copy(paths = Some(Seq(
            "vctSubscription/0/amountInvested",
            "vctSubscription/0/reliefClaimed"))), BAD_REQUEST),
          (ReliefDateFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (RuleTaxYearNotSupportedError, BAD_REQUEST),
          (RuleIncorrectOrEmptyBodyError, BAD_REQUEST),
          (CustomerReferenceFormatError, BAD_REQUEST)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "Service errors occur" should {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockAmendOtherReliefsRequestParser
              .parseRequest(rawData)
              .returns(Right(requestData))

            MockAmendOtherReliefsService
              .amend(requestData)
              .returns(Future.successful(Left(ErrorWrapper(Some(correlationId), mtdError))))

            val result: Future[Result] = controller.handleRequest(nino, taxYear)(fakePostRequest(requestJson))

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (NotFoundError, NOT_FOUND),
          (DownstreamError, INTERNAL_SERVER_ERROR),
          (TaxYearFormatError, BAD_REQUEST)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }
}
