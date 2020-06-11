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
 * WITHOUT WARRANTIED OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package v1.endpoints

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import support.IntegrationBaseSpec
import v1.models.errors._
import v1.stubs.{AuditStub, AuthStub, DesStub, MtdIdLookupStub}

class AmendOtherReliefsControllerISpec extends IntegrationBaseSpec {

  private trait Test {

    val nino = "AA123456A"
    val taxYear: String = "2019-20"
    val correlationId: String = "X-123"

    val requestBodyJson = Json.parse(
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

    val responseBody = Json.parse(
      s"""
         |{
         |   "links":[
         |      {
         |         "href":"/individuals/reliefs/other/$nino/$taxYear",
         |         "method":"PUT",
         |         "rel":"amend-reliefs-other"
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

    def uri: String = s"/other/$nino/$taxYear"

    def desUri: String = s"/reliefs/other/$nino/$taxYear"

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
    }

    def errorBody(code: String): String =
      s"""
         |      {
         |        "code": "$code",
         |        "reason": "des message"
         |      }
    """.stripMargin
  }

  "Calling the amend endpoint" should {

    "return a 200 status code" when {

      "any valid request is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.PUT, desUri, NO_CONTENT, JsObject.empty)
        }

        val response: WSResponse = await(request().put(requestBodyJson))
        response.status shouldBe OK
        response.json shouldBe responseBody
        response.header("X-CorrelationId").nonEmpty shouldBe true
      }
    }
    "return a 400 with multiple errors" when {
      "all field value validations fail on the request body" in new Test {

        val allInvalidValueRequestBodyJson: JsValue = Json.parse(
          """
            |{
            |  "payrollGiving": {
            |        "customerReference": "myihadskjnadjdksnjknkqgnkxankgdankganxjkndgref",
            |        "reliefClaimed": -154.00
            |      },
            |  "qualifyingDistributionRedemptionOfSharesAndSecurities": {
            |        "customerReference": "myihadskjnadjdksnjknkqgnkxankgdankganxjkndgref",
            |        "amount": -222.22
            |      },
            |  "maintenancePayments": [
            |    {
            |        "customerReference": "myihadskjnadjdksnjknkqgnkxankgdankganxjkndgref",
            |        "exSpouseName" : "Hilda",
            |        "exSpouseDateOfBirth": "2000-01",
            |        "amount": -222.22
            |      }
            |  ],
            |  "postCessationTradeReliefAndCertainOtherLosses": [
            |    {
            |        "customerReference": "myrefmyihadskjnadjdksnjknkqgnkxankgdankganxjkndgref",
            |        "businessName": "ACME Inc",
            |        "dateBusinessCeased": "2019-08",
            |        "natureOfTrade": "Widgets Manufacturer",
            |        "incomeSource": "AB12412/A12",
            |        "amount": -222.22
            |      }
            |  ],
            |  "annualPaymentsMade": {
            |        "customerReference": "myrefmyihadskjnadjdksnjknkqgnkxankgdankganxjkndgref",
            |        "reliefClaimed": -763.00
            |      },
            |  "qualifyingLoanInterestPayments": [
            |    {
            |        "customerReference": "myrefmyihadskjnadjdksnjknkqgnkxankgdankganxjkndgref",
            |        "lenderName": "Maurice",
            |        "reliefClaimed": -763.00
            |      }
            |  ]
            |}""".stripMargin)

        val allInvalidValueRequestError: List[MtdError] = List(
          CustomerReferenceFormatError.copy(
            message = "The provided customer reference is not valid",
            paths = Some(List(
              "/nonDeductableLoanInterest/customerReference",
              "/payrollGiving/customerReference",
              "/qualifyingDistributionRedemptionOfSharesAndSecurities/customerReference",
              "/maintenancePayments/0/customerReference",
              "/postCessationTradeReliefAndCertainOtherLosses/0/customerReference",
              "/annualPaymentsMade/customerReference",
              "/qualifyingLoanInterestPayments/0/customerReference"
            ))
          ),
          ValueFormatError.copy(
            message = "The field should be between 0 and 99999999999.99",
            paths = Some(List(
              "/nonDeductableLoanInterest/reliefClaimed",
              "/payrollGiving/reliefClaimed",
              "/qualifyingDistributionRedemptionOfSharesAndSecurities/amount",
              "/maintenancePayments/0/amount",
              "/postCessationTradeReliefAndCertainOtherLosses/0/amount",
              "/annualPaymentsMade/reliefClaimed",
              "/qualifyingLoanInterestPayments/0/reliefClaimed"
            ))
          ),
          ReliefDateFormatError.copy(
            message = "The field should be in the format YYYY-MM-DD",
            paths =Some(List(
              "/maintenancePayments/0/exSpouseDateOfBirth",
              "/postCessationTradeReliefAndCertainOtherLosses/0/dateBusinessCeased"
            ))
          )
        )

        val wrappedErrors: ErrorWrapper = ErrorWrapper(
          correlationId = Some(correlationId),
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

      val validRequestBodyJson =  Json.parse(
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

      val allInvalidvalueFormatRequestBodyJson = Json.parse(
        """
          |{
          |  "nonDeductableLoanInterest": {
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

      val allDatesInvalidRequestBodyJson =  Json.parse(
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

      val allCustomerReferencesInvalidRequestBodyJson =  Json.parse(
        """
          |{
          |  "nonDeductableLoanInterest": {
          |        "customerReference": "reuewgjhgrjekjghukdrwhjgbjhguirughwuiguruhgerehgrhwuhf",
          |        "reliefClaimed": 763.00
          |      },
          |  "payrollGiving": {
          |        "customerReference": "reuewgjhgrjekjghukdrwhjgbjhguirughwuiguruhgerehgrhwuhf",
          |        "reliefClaimed": 154.00
          |      },
          |  "qualifyingDistributionRedemptionOfSharesAndSecurities": {
          |        "customerReference": "reuewgjhgrjekjghukdrwhjgbjhguirughwuiguruhgerehgrhwuhf",
          |        "amount": 222.22
          |      },
          |  "maintenancePayments": [
          |    {
          |        "customerReference": "reuewgjhgrjekjghukdrwhjgbjhguirughwuiguruhgerehgrhwuhf",
          |        "exSpouseName" : "Hilda",
          |        "exSpouseDateOfBirth": "2000-01-01",
          |        "amount": 222.22
          |      }
          |  ],
          |  "postCessationTradeReliefAndCertainOtherLosses": [
          |    {
          |        "customerReference": "reuewgjhgrjekjghukdrwhjgbjhguirughwuiguruhgerehgrhwuhf",
          |        "businessName": "ACME Inc",
          |        "dateBusinessCeased": "2019-08-10",
          |        "natureOfTrade": "Widgets Manufacturer",
          |        "incomeSource": "AB12412/A12",
          |        "amount": 222.22
          |      }
          |  ],
          |  "annualPaymentsMade": {
          |        "customerReference": "reuewgjhgrjekjghukdrwhjgbjhguirughwuiguruhgerehgrhwuhf",
          |        "reliefClaimed": 763.00
          |      },
          |  "qualifyingLoanInterestPayments": [
          |    {
          |        "customerReference": "reuewgjhgrjekjghukdrwhjgbjhguirughwuiguruhgerehgrhwuhf",
          |        "lenderName": "Maurice",
          |        "reliefClaimed": 763.00
          |      }
          |  ]
          |}""".stripMargin)

      val allValueFormatError: MtdError = ValueFormatError.copy(
        message = "The field should be between 0 and 99999999999.99",
        paths = Some(Seq(
          "/nonDeductableLoanInterest/reliefClaimed",
          "/payrollGiving/reliefClaimed",
          "/qualifyingDistributionRedemptionOfSharesAndSecurities/amount",
          "/maintenancePayments/0/amount",
          "/postCessationTradeReliefAndCertainOtherLosses/0/amount",
          "/annualPaymentsMade/reliefClaimed",
          "/qualifyingLoanInterestPayments/0/reliefClaimed"
        ))
      )

      val allDateFormatError: MtdError = ReliefDateFormatError.copy(
        message = "The field should be in the format YYYY-MM-DD",
        paths = Some(List(
          "/maintenancePayments/0/exSpouseDateOfBirth",
          "/postCessationTradeReliefAndCertainOtherLosses/0/dateBusinessCeased"
        ))
      )

      val allCustomerReferenceFormatErrors: MtdError = CustomerReferenceFormatError.copy(
        message = "The provided customer reference is not valid",
        paths = Some(List(
          "/nonDeductableLoanInterest/customerReference",
          "/payrollGiving/customerReference",
          "/qualifyingDistributionRedemptionOfSharesAndSecurities/customerReference",
          "/maintenancePayments/0/customerReference",
          "/postCessationTradeReliefAndCertainOtherLosses/0/customerReference",
          "/annualPaymentsMade/customerReference",
          "/qualifyingLoanInterestPayments/0/customerReference"
        ))
      )

      "validation error" when {
        def validationErrorTest(requestNino:String, requestTaxYear: String, requestBody: JsValue,  expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new Test {

            override val nino: String = requestNino
            override val taxYear: String = requestTaxYear
            override val requestBodyJson: JsValue = requestBody

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

        val input = Seq(
          ("AA1123A", "2017-18", validRequestBodyJson, BAD_REQUEST, NinoFormatError),
          ("AA123456A", "20177", validRequestBodyJson,  BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "2017-18", allInvalidvalueFormatRequestBodyJson, BAD_REQUEST, allValueFormatError),
          ("AA123456A", "2017-18", allDatesInvalidRequestBodyJson, BAD_REQUEST, allDateFormatError),
          ("AA123456A", "2017-18", allCustomerReferencesInvalidRequestBodyJson, BAD_REQUEST, allCustomerReferenceFormatErrors)
        )

        input.foreach(args => (validationErrorTest _).tupled(args))
      }
    }
  }
}