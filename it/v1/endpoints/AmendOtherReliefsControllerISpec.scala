/*
 * copyright 2022 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIED OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package v1.endpoints

import api.models.errors
import api.models.errors._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec
import v1.endpoints.AmendOtherReliefsControllerISpec._
import v1.stubs.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}

class AmendOtherReliefsControllerISpec extends IntegrationBaseSpec {

  "Calling the amend endpoint" should {

    "return a 200 status code" when {

      "any valid request is made" in new NonTysTest {

        override def setupStubs(): StubMapping = {
          DownstreamStub.onSuccess(DownstreamStub.PUT, downstreamUri, NO_CONTENT, JsObject.empty)
        }

        val response: WSResponse = await(request().put(requestBodyJson))
        response.status shouldBe OK
        response.json shouldBe responseBody
        response.header("X-CorrelationId").nonEmpty shouldBe true
      }

      "any valid Tax-Year-Specific (TYS) request is made" in new TysTest {
        override def setupStubs(): StubMapping = {
          DownstreamStub.onSuccess(DownstreamStub.PUT, downstreamUri, NO_CONTENT, JsObject.empty)
        }

        val response: WSResponse = await(request().put(requestBodyJson))
        response.status shouldBe OK
        response.json shouldBe responseBody
        response.header("X-CorrelationId").nonEmpty shouldBe true
      }
    }
    "return a 400 with multiple errors" when {
      "all field value validations fail on the request body" in new NonTysTest {

        val wrappedErrors: ErrorWrapper = errors.ErrorWrapper(
          correlationId = correlationId,
          error = BadRequestError,
          errors = Some(allInvalidValueRequestError)
        )

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
        }

        val response: WSResponse = await(request().put(allInvalidValueRequestBodyJson))
        response.status shouldBe BAD_REQUEST
        response.json shouldBe Json.toJson(wrappedErrors)
      }
    }

    "return an error according to spec" when {

      "validation error" when {
        def validationErrorTest(requestNino: String,
                                requestTaxYear: String,
                                requestBody: JsValue,
                                expectedStatus: Int,
                                expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new NonTysTest {

            override val nino: String    = requestNino
            override val taxYear: String = requestTaxYear
            val requestBodyJson: JsValue = requestBody

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
            }

            val response: WSResponse = await(request().put(requestBodyJson))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = List(
          ("AA1123A", "2021-22", validRequestBodyJson, BAD_REQUEST, NinoFormatError),
          ("AA123456A", "2019-20", validRequestBodyJson, BAD_REQUEST, RuleTaxYearNotSupportedError),
          ("AA123456A", "2020-22", validRequestBodyJson, BAD_REQUEST, RuleTaxYearRangeInvalidError),
          ("AA123456A", "20121", validRequestBodyJson, BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "2021-22", allInvalidValueFormatRequestBodyJson, BAD_REQUEST, allValueFormatError),
          ("AA123456A", "2021-22", allDatesInvalidRequestBodyJson, BAD_REQUEST, allDateFormatError),
          ("AA123456A", "2021-22", allCustomerReferencesInvalidRequestBodyJson, BAD_REQUEST, allCustomerReferenceFormatErrors),
          ("AA123456A", "2021-22", allExSpouseNamesInvalidRequestBodyJson, BAD_REQUEST, allExSpouseNameFormatErrors),
          ("AA123456A", "2021-22", allBusinessNamesInvalidRequestBodyJson, BAD_REQUEST, allBusinessNameFormatErrors),
          ("AA123456A", "2021-22", allNatureOfTradesInvalidRequestBodyJson, BAD_REQUEST, allNatureOfTradeFormatErrors),
          ("AA123456A", "2021-22", allIncomeSourcesInvalidRequestBodyJson, BAD_REQUEST, allIncomeSourceFormatErrors),
          ("AA123456A", "2021-22", allLenderNamesInvalidRequestBodyJson, BAD_REQUEST, allLenderNameFormatErrors)
        )
        input.foreach(args => (validationErrorTest _).tupled(args))
      }

      "downstream service error" when {
        def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"downstream returns an $downstreamCode error and status $downstreamStatus" in new NonTysTest {

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
              DownstreamStub.onError(DownstreamStub.PUT, downstreamUri, downstreamStatus, errorBody(downstreamCode))
            }

            val response: WSResponse = await(request().put(requestBodyJson))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val errors = List(
          (BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "INVALID_TAX_YEAR", BAD_REQUEST, TaxYearFormatError),
          (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, InternalError),
          (UNPROCESSABLE_ENTITY, "BUSINESS_VALIDATION_RULE_FAILURE", BAD_REQUEST, RuleSubmissionFailedError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError)
        )

        val extraTysErrors = List(
          (BAD_REQUEST, "INVALID_CORRELATION_ID", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "INVALID_PAYLOAD", INTERNAL_SERVER_ERROR, InternalError),
          (UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_SUPPORTED", BAD_REQUEST, RuleTaxYearNotSupportedError),
          (UNPROCESSABLE_ENTITY, "UNPROCESSABLE_ENTITY", INTERNAL_SERVER_ERROR, InternalError)
        )
        (errors ++ extraTysErrors).foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }

  private trait Test {

    val nino = "AA123456A"

    def taxYear: String

    val correlationId: String = "X-123"

    def mtdUri: String = s"/other/$nino/$taxYear"

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      AuditStub.audit()
      AuthStub.authorised()
      MtdIdLookupStub.ninoFound(nino)
      setupStubs()
      buildRequest(mtdUri)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.1.0+json"),
          (AUTHORIZATION, "Bearer 123") // some bearer token
        )
    }

    def errorBody(code: String): String =
      s"""
         |      {
         |        "code": "$code",
         |        "reason": "message"
         |      }
    """.stripMargin

    protected val responseBody: JsValue = Json.parse(s"""
         |{
         |   "links":[
         |      {
         |         "href":"/individuals/reliefs/other/$nino/$taxYear",
         |         "method":"PUT",
         |         "rel":"create-and-amend-reliefs-other"
         |      },
         |      {
         |         "href":"/individuals/reliefs/other/$nino/$taxYear",
         |         "method":"GET",
         |         "rel":"self"
         |      },
         |      {
         |         "href":"/individuals/reliefs/other/$nino/$taxYear",
         |         "method":"DELETE",
         |         "rel":"delete-reliefs-other"
         |
         |      }
         |   ]
         |}""".stripMargin)

  }

  private trait NonTysTest extends Test {
    def taxYear: String = "2020-21"

    def downstreamUri: String = s"/income-tax/reliefs/other/$nino/2020-21"
  }

  private trait TysTest extends Test {
    def taxYear: String = "2023-24"

    def downstreamUri: String = s"/income-tax/reliefs/other/23-24/$nino"
  }

}

object AmendOtherReliefsControllerISpec {

  private val requestBodyJson = Json.parse("""
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

  val allInvalidValueRequestBodyJson: JsValue = Json.parse("""
      |{
      |  "nonDeductibleLoanInterest": {
      |        "customerReference": "",
      |        "reliefClaimed": -763.00
      |      },
      |  "payrollGiving": {
      |        "customerReference": "",
      |        "reliefClaimed": -154.00
      |      },
      |  "qualifyingDistributionRedemptionOfSharesAndSecurities": {
      |        "customerReference": "",
      |        "amount": -222.22
      |      },
      |  "maintenancePayments": [
      |    {
      |        "customerReference": "",
      |        "exSpouseName" : "",
      |        "exSpouseDateOfBirth": "2000-01",
      |        "amount": -222.22
      |      }
      |  ],
      |  "postCessationTradeReliefAndCertainOtherLosses": [
      |    {
      |        "customerReference": "",
      |        "businessName": "",
      |        "dateBusinessCeased": "2019-08",
      |        "natureOfTrade": "",
      |        "incomeSource": "",
      |        "amount": -222.22
      |      }
      |  ],
      |  "annualPaymentsMade": {
      |        "customerReference": "",
      |        "reliefClaimed": -763.00
      |      },
      |  "qualifyingLoanInterestPayments": [
      |    {
      |        "customerReference": "",
      |        "lenderName": "",
      |        "reliefClaimed": -763.00
      |      }
      |  ]
      |}""".stripMargin)

  val allInvalidValueRequestError: List[MtdError] = List(
    LenderNameFormatError.withPath("/qualifyingLoanInterestPayments/0/lenderName"),
    CustomerReferenceFormatError.withPaths(
      List(
        "/nonDeductibleLoanInterest/customerReference",
        "/payrollGiving/customerReference",
        "/qualifyingDistributionRedemptionOfSharesAndSecurities/customerReference",
        "/postCessationTradeReliefAndCertainOtherLosses/0/customerReference",
        "/maintenancePayments/0/customerReference",
        "/annualPaymentsMade/customerReference",
        "/qualifyingLoanInterestPayments/0/customerReference"
      )),
    IncomeSourceFormatError.withPath("/postCessationTradeReliefAndCertainOtherLosses/0/incomeSource"),
    BusinessNameFormatError.withPath("/postCessationTradeReliefAndCertainOtherLosses/0/businessName"),
    NatureOfTradeFormatError.withPath("/postCessationTradeReliefAndCertainOtherLosses/0/natureOfTrade"),
    ValueFormatError.withPaths(
      List(
        "/nonDeductibleLoanInterest/reliefClaimed",
        "/payrollGiving/reliefClaimed",
        "/qualifyingDistributionRedemptionOfSharesAndSecurities/amount",
        "/postCessationTradeReliefAndCertainOtherLosses/0/amount",
        "/maintenancePayments/0/amount",
        "/annualPaymentsMade/reliefClaimed",
        "/qualifyingLoanInterestPayments/0/reliefClaimed"
      )),
    DateFormatError.withPaths(
      List(
        "/postCessationTradeReliefAndCertainOtherLosses/0/dateBusinessCeased",
        "/maintenancePayments/0/exSpouseDateOfBirth"
      )),
    ExSpouseNameFormatError.withPath("/maintenancePayments/0/exSpouseName")
  )

  private val validRequestBodyJson = Json.parse("""
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

  private val allInvalidValueFormatRequestBodyJson = Json.parse("""
      |{
      |  "nonDeductibleLoanInterest": {
      |        "customerReference": "myref",
      |        "reliefClaimed": -763.00
      |      },
      |  "payrollGiving": {
      |        "customerReference": "myref",
      |        "reliefClaimed": -154.00
      |      },
      |  "qualifyingDistributionRedemptionOfSharesAndSecurities": {
      |        "customerReference": "myref",
      |        "amount": -222.22
      |      },
      |  "maintenancePayments": [
      |    {
      |        "customerReference": "myref",
      |        "exSpouseName" : "Hilda",
      |        "exSpouseDateOfBirth": "2000-01-01",
      |        "amount": -222.22
      |      }
      |  ],
      |  "postCessationTradeReliefAndCertainOtherLosses": [
      |    {
      |        "customerReference": "myref",
      |        "businessName": "ACME Inc",
      |        "dateBusinessCeased": "2019-08-10",
      |        "natureOfTrade": "Widgets Manufacturer",
      |        "incomeSource": "AB12412/A12",
      |        "amount": -222.22
      |      }
      |  ],
      |  "annualPaymentsMade": {
      |        "customerReference": "myref",
      |        "reliefClaimed": -763.00
      |      },
      |  "qualifyingLoanInterestPayments": [
      |    {
      |        "customerReference": "myref",
      |        "lenderName": "Maurice",
      |        "reliefClaimed": -763.00
      |      }
      |  ]
      |}""".stripMargin)

  private val allDatesInvalidRequestBodyJson = Json.parse("""
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
      |        "exSpouseDateOfBirth": "01-01-2000",
      |        "amount": 222.22
      |      }
      |  ],
      |  "postCessationTradeReliefAndCertainOtherLosses": [
      |    {
      |        "customerReference": "myref",
      |        "businessName": "ACME Inc",
      |        "dateBusinessCeased": "20190810",
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

  private val allCustomerReferencesInvalidRequestBodyJson = Json.parse("""
      |{
      |  "nonDeductibleLoanInterest": {
      |        "customerReference": "",
      |        "reliefClaimed": 763.00
      |      },
      |  "payrollGiving": {
      |        "customerReference": "",
      |        "reliefClaimed": 154.00
      |      },
      |  "qualifyingDistributionRedemptionOfSharesAndSecurities": {
      |        "customerReference": "",
      |        "amount": 222.22
      |      },
      |  "maintenancePayments": [
      |    {
      |        "customerReference": "",
      |        "exSpouseName" : "Hilda",
      |        "exSpouseDateOfBirth": "2000-01-01",
      |        "amount": 222.22
      |      }
      |  ],
      |  "postCessationTradeReliefAndCertainOtherLosses": [
      |    {
      |        "customerReference": "",
      |        "businessName": "ACME Inc",
      |        "dateBusinessCeased": "2019-08-10",
      |        "natureOfTrade": "Widgets Manufacturer",
      |        "incomeSource": "AB12412/A12",
      |        "amount": 222.22
      |      }
      |  ],
      |  "annualPaymentsMade": {
      |        "customerReference": "",
      |        "reliefClaimed": 763.00
      |      },
      |  "qualifyingLoanInterestPayments": [
      |    {
      |        "customerReference": "",
      |        "lenderName": "Maurice",
      |        "reliefClaimed": 763.00
      |      }
      |  ]
      |}""".stripMargin)

  private val allExSpouseNamesInvalidRequestBodyJson = Json.parse("""
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
      |        "exSpouseName" : "",
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

  private val allBusinessNamesInvalidRequestBodyJson = Json.parse("""
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
      |        "businessName": "",
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

  private val allNatureOfTradesInvalidRequestBodyJson = Json.parse("""
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
      |        "natureOfTrade": "",
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

  private val allIncomeSourcesInvalidRequestBodyJson = Json.parse("""
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
      |        "incomeSource": "",
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

  private val allLenderNamesInvalidRequestBodyJson = Json.parse("""
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
      |        "lenderName": "",
      |        "reliefClaimed": 763.00
      |      }
      |  ]
      |}""".stripMargin)

  val allValueFormatError: MtdError = ValueFormatError.copy(
    paths = Some(
      List(
        "/nonDeductibleLoanInterest/reliefClaimed",
        "/payrollGiving/reliefClaimed",
        "/qualifyingDistributionRedemptionOfSharesAndSecurities/amount",
        "/postCessationTradeReliefAndCertainOtherLosses/0/amount",
        "/maintenancePayments/0/amount",
        "/annualPaymentsMade/reliefClaimed",
        "/qualifyingLoanInterestPayments/0/reliefClaimed"
      ))
  )

  val allDateFormatError: MtdError = DateFormatError.copy(
    paths = Some(
      List(
        "/postCessationTradeReliefAndCertainOtherLosses/0/dateBusinessCeased",
        "/maintenancePayments/0/exSpouseDateOfBirth"
      ))
  )

  val allCustomerReferenceFormatErrors: MtdError = CustomerReferenceFormatError.copy(
    paths = Some(
      List(
        "/nonDeductibleLoanInterest/customerReference",
        "/payrollGiving/customerReference",
        "/qualifyingDistributionRedemptionOfSharesAndSecurities/customerReference",
        "/postCessationTradeReliefAndCertainOtherLosses/0/customerReference",
        "/maintenancePayments/0/customerReference",
        "/annualPaymentsMade/customerReference",
        "/qualifyingLoanInterestPayments/0/customerReference"
      ))
  )

  val allExSpouseNameFormatErrors: MtdError = ExSpouseNameFormatError.copy(
    paths = Some(
      List(
        "/maintenancePayments/0/exSpouseName"
      ))
  )

  val allBusinessNameFormatErrors: MtdError = BusinessNameFormatError.copy(
    paths = Some(
      List(
        "/postCessationTradeReliefAndCertainOtherLosses/0/businessName"
      ))
  )

  val allNatureOfTradeFormatErrors: MtdError = NatureOfTradeFormatError.copy(
    paths = Some(
      List(
        "/postCessationTradeReliefAndCertainOtherLosses/0/natureOfTrade"
      ))
  )

  val allIncomeSourceFormatErrors: MtdError = IncomeSourceFormatError.copy(
    paths = Some(
      List(
        "/postCessationTradeReliefAndCertainOtherLosses/0/incomeSource"
      ))
  )

  val allLenderNameFormatErrors: MtdError = LenderNameFormatError.copy(
    paths = Some(
      List(
        "/qualifyingLoanInterestPayments/0/lenderName"
      ))
  )

}
