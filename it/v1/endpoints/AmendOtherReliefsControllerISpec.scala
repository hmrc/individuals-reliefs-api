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
import v1.models.errors.{BadRequestError, ErrorWrapper, MtdError, ValueFormatError}
import v1.stubs.{AuditStub, AuthStub, DesStub, MtdIdLookupStub}

class AmendOtherReliefsControllerISpec extends IntegrationBaseSpec {

  private trait Test {

    val nino = "AA123456A"
    val taxYear: String = "2021-22"
    val correlationId: String = "X-123"

    val requestBodyJson = Json.parse(
      """
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

    val responseBody = Json.parse(
      s"""
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

    def uri: String = s"/other/$nino/$taxYear"

    def desUri: String = s"/income-tax/reliefs/other/$nino/$taxYear"

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
          LenderNameFormatError.copy(paths = Some(Seq(
            "/qualifyingLoanInterestPayments/0/lenderName"
          ))),
          CustomerReferenceFormatError.copy(paths = Some(List(
            "/nonDeductibleLoanInterest/customerReference",
            "/payrollGiving/customerReference",
            "/qualifyingDistributionRedemptionOfSharesAndSecurities/customerReference",
            "/maintenancePayments/0/customerReference",
            "/postCessationTradeReliefAndCertainOtherLosses/0/customerReference",
            "/annualPaymentsMade/customerReference",
            "/qualifyingLoanInterestPayments/0/customerReference"
          ))),
          DateFormatError.copy(paths = Some(List(
            "/maintenancePayments/0/exSpouseDateOfBirth",
            "/postCessationTradeReliefAndCertainOtherLosses/0/dateBusinessCeased"
          ))),
          ExSpouseNameFormatError.copy(paths = Some(Seq(
            "/maintenancePayments/0/exSpouseName"
          ))),
          IncomeSourceFormatError.copy(paths = Some(Seq(
            "/postCessationTradeReliefAndCertainOtherLosses/0/incomeSource"
          ))),
          BusinessNameFormatError.copy(paths = Some(Seq(
            "/postCessationTradeReliefAndCertainOtherLosses/0/businessName"
          ))),
          NatureOfTradeFormatError.copy(paths = Some(Seq(
            "/postCessationTradeReliefAndCertainOtherLosses/0/natureOfTrade"
          ))),
          ValueFormatError.copy(paths = Some(List(
            "/nonDeductibleLoanInterest/reliefClaimed",
            "/payrollGiving/reliefClaimed",
            "/qualifyingDistributionRedemptionOfSharesAndSecurities/amount",
            "/maintenancePayments/0/amount",
            "/postCessationTradeReliefAndCertainOtherLosses/0/amount",
            "/annualPaymentsMade/reliefClaimed",
            "/qualifyingLoanInterestPayments/0/reliefClaimed"
          )))
        )

        val wrappedErrors: ErrorWrapper = ErrorWrapper(
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

      val validRequestBodyJson = Json.parse(
        """
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

      val allInvalidvalueFormatRequestBodyJson = Json.parse(
        """
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

      val allDatesInvalidRequestBodyJson = Json.parse(
        """
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

      val allCustomerReferencesInvalidRequestBodyJson = Json.parse(
        """
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

      val allExSpouseNamesInvalidRequestBodyJson = Json.parse(
        """
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

      val allBusinessNamesInvalidRequestBodyJson = Json.parse(
        """
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

      val allNatureOfTradesInvalidRequestBodyJson = Json.parse(
        """
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

      val allIncomeSourcesInvalidRequestBodyJson = Json.parse(
        """
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

      val allLenderNamesInvalidRequestBodyJson = Json.parse(
        """
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
        paths = Some(Seq(
          "/nonDeductibleLoanInterest/reliefClaimed",
          "/payrollGiving/reliefClaimed",
          "/qualifyingDistributionRedemptionOfSharesAndSecurities/amount",
          "/maintenancePayments/0/amount",
          "/postCessationTradeReliefAndCertainOtherLosses/0/amount",
          "/annualPaymentsMade/reliefClaimed",
          "/qualifyingLoanInterestPayments/0/reliefClaimed"
        ))
      )

      val allDateFormatError: MtdError = DateFormatError.copy(
        paths = Some(List(
          "/maintenancePayments/0/exSpouseDateOfBirth",
          "/postCessationTradeReliefAndCertainOtherLosses/0/dateBusinessCeased"
        ))
      )

      val allCustomerReferenceFormatErrors: MtdError = CustomerReferenceFormatError.copy(
        paths = Some(List(
          "/nonDeductibleLoanInterest/customerReference",
          "/payrollGiving/customerReference",
          "/qualifyingDistributionRedemptionOfSharesAndSecurities/customerReference",
          "/maintenancePayments/0/customerReference",
          "/postCessationTradeReliefAndCertainOtherLosses/0/customerReference",
          "/annualPaymentsMade/customerReference",
          "/qualifyingLoanInterestPayments/0/customerReference"
        ))
      )

      val allExSpouseNameFormatErrors: MtdError = ExSpouseNameFormatError.copy(
        paths = Some(List(
          "/maintenancePayments/0/exSpouseName"
        ))
      )

      val allBusinessNameFormatErrors: MtdError = BusinessNameFormatError.copy(
        paths = Some(List(
          "/postCessationTradeReliefAndCertainOtherLosses/0/businessName"
        ))
      )

      val allNatureOfTradeFormatErrors: MtdError = NatureOfTradeFormatError.copy(
        paths = Some(List(
          "/postCessationTradeReliefAndCertainOtherLosses/0/natureOfTrade"
        ))
      )

      val allIncomeSourceFormatErrors: MtdError = IncomeSourceFormatError.copy(
        paths = Some(List(
          "/postCessationTradeReliefAndCertainOtherLosses/0/incomeSource"
        ))
      )

      val allLenderNameFormatErrors: MtdError = LenderNameFormatError.copy(
        paths = Some(List(
          "/qualifyingLoanInterestPayments/0/lenderName"
        ))
      )

      "validation error" when {
        def validationErrorTest(requestNino: String, requestTaxYear: String, requestBody: JsValue, expectedStatus: Int, expectedBody: MtdError): Unit = {
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
          ("AA1123A", "2021-22", validRequestBodyJson, BAD_REQUEST, NinoFormatError),
          ("AA123456A", "2019-20", validRequestBodyJson, BAD_REQUEST, RuleTaxYearNotSupportedError),
          ("AA123456A", "2020-22", validRequestBodyJson, BAD_REQUEST, RuleTaxYearRangeInvalidError),
          ("AA123456A", "20121", validRequestBodyJson, BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "2021-22", allInvalidvalueFormatRequestBodyJson, BAD_REQUEST, allValueFormatError),
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

      "des service error" when {
        def serviceErrorTest(desStatus: Int, desCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"des returns an $desCode error and status $desStatus" in new Test {

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
              DesStub.onError(DesStub.PUT, desUri, desStatus, errorBody(desCode))
            }

            val response: WSResponse = await(request().put(requestBodyJson))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = Seq(
          (BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "FORMAT_TAX_YEAR", BAD_REQUEST, TaxYearFormatError),
          (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, DownstreamError),
          (UNPROCESSABLE_ENTITY, "BUSINESS_VALIDATION", FORBIDDEN, RuleSubmissionFailed),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, DownstreamError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, DownstreamError))

        input.foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }
}

