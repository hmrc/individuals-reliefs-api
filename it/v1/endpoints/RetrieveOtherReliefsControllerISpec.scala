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

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec
import v1.models.errors.{DownstreamError, MtdError, NinoFormatError, NotFoundError, RuleTaxYearNotSupportedError, RuleTaxYearRangeInvalidError, TaxYearFormatError}
import v1.stubs.{AuditStub, AuthStub, DesStub, MtdIdLookupStub}

class RetrieveOtherReliefsControllerISpec extends IntegrationBaseSpec {

  private trait Test {

    val nino    = "AA123456A"
    val taxYear = "2021-22"

    val responseBody: JsValue = Json.parse(
      s"""
         |{
         |    "submittedOn": "2020-06-17T10:53:38Z",
         |    "nonDeductibleLoanInterest": {
         |        "customerReference": "INPOLY123A",
         |        "reliefClaimed": 2000.99
         |    },
         |    "payrollGiving": {
         |        "customerReference": "INPOLY123A",
         |        "reliefClaimed": 2000.99
         |    },
         |    "qualifyingDistributionRedemptionOfSharesAndSecurities": {
         |        "customerReference": "INPOLY123A",
         |        "amount": 2000.99
         |    },
         |    "maintenancePayments": [
         |        {
         |            "customerReference": "INPOLY123A",
         |            "exSpouseName": "Wilson",
         |            "exSpouseDateOfBirth": "1985-04-06",
         |            "amount": 2000.99
         |        }
         |    ],
         |    "postCessationTradeReliefAndCertainOtherLosses": [
         |        {
         |            "customerReference": "INPOLY123A",
         |            "businessName": "CA Ltd",
         |            "dateBusinessCeased": "2019-04-06",
         |            "natureOfTrade": "Widgets Manufacturer",
         |            "incomeSource": "AB12412/A12",
         |            "amount": 2000.99
         |        }
         |    ],
         |    "annualPaymentsMade": {
         |        "customerReference": "INPOLY123A",
         |        "reliefClaimed": 2000.99
         |    },
         |    "qualifyingLoanInterestPayments": [
         |        {
         |            "customerReference": "INPOLY123A",
         |            "lenderName": "Peters",
         |            "reliefClaimed": 2000.99
         |        }
         |    ],
         |    "links":[
         |        {
         |            "href":"/individuals/reliefs/other/$nino/$taxYear",
         |            "method":"GET",
         |            "rel":"self"
         |        },
         |        {
         |            "href":"/individuals/reliefs/other/$nino/$taxYear",
         |            "method":"PUT",
         |            "rel":"create-and-amend-reliefs-other"
         |        },
         |        {
         |            "href":"/individuals/reliefs/other/$nino/$taxYear",
         |            "method":"DELETE",
         |            "rel":"delete-reliefs-other"
         |        }
         |    ]
         |}
         |""".stripMargin
    )

    val desResponseBody: JsValue = Json.parse(s"""
         |{
         |    "submittedOn": "2020-06-17T10:53:38Z",
         |    "nonDeductibleLoanInterest": {
         |        "customerReference": "INPOLY123A",
         |        "reliefClaimed": 2000.99
         |    },
         |    "payrollGiving": {
         |        "customerReference": "INPOLY123A",
         |        "reliefClaimed": 2000.99
         |    },
         |    "qualifyingDistributionRedemptionOfSharesAndSecurities": {
         |        "customerReference": "INPOLY123A",
         |        "amount": 2000.99
         |    },
         |    "maintenancePayments": [{
         |        "customerReference": "INPOLY123A",
         |        "exSpouseName": "Wilson",
         |        "exSpouseDateOfBirth": "1985-04-06",
         |        "amount": 2000.99
         |    }],
         |    "postCessationTradeReliefAndCertainOtherLosses": [{
         |        "customerReference": "INPOLY123A",
         |        "businessName": "CA Ltd",
         |        "dateBusinessCeased": "2019-04-06",
         |        "natureOfTrade": "Widgets Manufacturer",
         |        "incomeSource": "AB12412/A12",
         |        "amount": 2000.99
         |    }],
         |    "annualPaymentsMade": {
         |        "customerReference": "INPOLY123A",
         |        "reliefClaimed": 2000.99
         |    },
         |    "qualifyingLoanInterestPayments": [{
         |        "customerReference": "INPOLY123A",
         |        "lenderName": "Peters",
         |        "reliefClaimed": 2000.99
         |    }]
         |}
         |""".stripMargin)

    def uri: String    = s"/other/$nino/$taxYear"
    def desUri: String = s"/income-tax/reliefs/other/$nino/$taxYear"

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.1.0+json"),
          (AUTHORIZATION, "Bearer 123") // some bearer token
      )
    }

    def errorBody(code: String): String =
      s"""
         |      {
         |        "code": "$code",
         |        "reason": "des message"
         |      }
    """.stripMargin

  }

  "Calling the retrieve endpoint" should {

    "return a 200 status code" when {

      "any valid request is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.GET, desUri, Status.OK, desResponseBody)
        }

        val response: WSResponse = await(request().get())
        response.status shouldBe Status.OK
        response.json shouldBe responseBody
        response.header("X-CorrelationId").nonEmpty shouldBe true
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return error according to spec" when {

      "validation error" when {
        def validationErrorTest(requestNino: String, requestId: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new Test {

            override val nino: String    = requestNino
            override val taxYear: String = requestId

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(requestNino)
            }

            val response: WSResponse = await(request().get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }

        val input = Seq(
          ("Walrus", "2021-22", Status.BAD_REQUEST, NinoFormatError),
          ("AA123456A", "203100", Status.BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "2019-20", Status.BAD_REQUEST, RuleTaxYearNotSupportedError),
          ("AA123456A", "2018-20", Status.BAD_REQUEST, RuleTaxYearRangeInvalidError)
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
              DesStub.onError(DesStub.GET, desUri, desStatus, errorBody(desCode))
            }

            val response: WSResponse = await(request().get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = Seq(
          (Status.BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", Status.BAD_REQUEST, NinoFormatError),
          (Status.BAD_REQUEST, "FORMAT_TAX_YEAR", Status.BAD_REQUEST, TaxYearFormatError),
          (Status.NOT_FOUND, "NO_DATA_FOUND", Status.NOT_FOUND, NotFoundError),
          (Status.INTERNAL_SERVER_ERROR, "SERVER_ERROR", Status.INTERNAL_SERVER_ERROR, DownstreamError),
          (Status.SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", Status.INTERNAL_SERVER_ERROR, DownstreamError)
        )
        input.foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }
}