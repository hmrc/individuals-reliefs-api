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
import v1.models.errors.{BadRequestError, ErrorWrapper, MtdError, ValueFormatError}
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
      """
        |{
        |   "links":[
        |      {
        |         "href":"/individuals/reliefs/other/{nino}/{taxYear}",
        |         "rel":"amend-reliefs-other",
        |         "method":"PUT"
        |      },
        |      {
        |         "href":"/individuals/reliefs/other/{nino}/{taxYear}",
        |         "rel":"self",
        |         "method":"GET"
        |      },
        |      {
        |         "href":"/individuals/reliefs/other/{nino}/{taxYear}",
        |         "rel":"delete-reliefs-other",
        |         "method":"DELETE"
        |      }
        |   ]
        |}""".stripMargin)

    def uri: String = s"/reliefs/other/$nino/$taxYear"

    def desUri: String = s"/individuals/reliefs/other/$nino/$taxYear"

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
            |  "nonDeductableLoanInterest": {
            |        "customerReference": "myr    ef",
            |        "reliefClaimed": -763.00
            |      },
            |  "payrollGiving": {
            |        "customerReference": "m!y!r!e!f",
            |        "reliefClaimed": -154.00
            |      },
            |  "qualifyingDistributionRedemptionOfSharesAndSecurities": {
            |        "customerReference": "my***ref",
            |        "amount": 222222222222222222222222222.22
            |      },
            |  "maintenancePayments": [
            |    {
            |        "customerReference": "myr!$@%#ef",
            |        "exSpouseName" : "H i l d a",
            |        "exSpouseDateOfBirth": "01-2018-01",
            |        "amount": 222.22222222
            |      }
            |  ],
            |  "postCessationTradeReliefAndCertainOtherLosses": [
            |    {
            |        "customerReference": "my-ref",
            |        "businessName": "ACME***Inc",
            |        "dateBusinessCeased": "08-10-2019",
            |        "natureOfTrade": "Widgets M$a$n$u$f$a$c$t$u$r$e$r",
            |        "incomeSource": "AB12412/%#^#!&A12",
            |        "amount": 222
            |      }
            |  ],
            |  "annualPaymentsMade": {
            |        "customerReference": "m-y-r-e-f",
            |        "reliefClaimed": -763.00
            |      },
            |  "qualifyingLoanInterestPayments": [
            |    {
            |        "customerReference": "m y  r e f",
            |        "lenderName": "Maurice^2",
            |        "reliefClaimed": -763.00
            |      }
            |  ]
            |}""".stripMargin)

        val allInvalidValueRequestError: List[MtdError] = List(
          ValueFormatError.copy(
            message = "The field should be between 1 and 99999999999.99",
            paths = Some(List(
              "/nonDeductableLoanInterest/0/reliefClaimed",
              "/payrollGiving/0/reliefClaimed",
              "/qualifyingDistributionRedemptionOfSharesAndSecurities/0/amount",
              "/maintenancePayments/0/customerReference",
              "/maintenancePayments/0/amount",
              "/postCessationTradeReliefAndCertainOtherLosses/0/amount",
              "/annualPaymentsMade/0/reliefClaimed",
              "/qualifyingLoanInterestPayments/0/reliefClaimed"
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
  }

}
