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
import play.api.libs.json.Json
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec
import v1.models.errors.{
  InternalError,
  MtdError,
  NinoFormatError,
  NotFoundError,
  RuleTaxYearNotSupportedError,
  RuleTaxYearRangeInvalidError,
  TaxYearFormatError
}
import v1.stubs.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}

class RetrievePensionsReliefsControllerISpec extends IntegrationBaseSpec {

  private trait Test {

    val nino    = "AA123456A"
    val taxYear = "2020-21"

    val responseBody = Json.parse(
      s"""
         |{
         |   "submittedOn":"2020-07-14T10:30:18Z",
         |   "pensionReliefs":{
         |      "regularPensionContributions":1999.99,
         |      "oneOffPensionContributionsPaid":1999.99,
         |      "retirementAnnuityPayments":1999.99,
         |      "paymentToEmployersSchemeNoTaxRelief":1999.99,
         |      "overseasPensionSchemeContributions":1999.99
         |   },
         |  "links":[
         |      {
         |         "href":"/individuals/reliefs/pensions/AA123456A/$taxYear",
         |         "method":"PUT",
         |         "rel":"create-and-amend-reliefs-pensions"
         |      },
         |      {
         |         "href":"/individuals/reliefs/pensions/AA123456A/$taxYear",
         |         "method":"GET",
         |         "rel":"self"
         |      },
         |      {
         |         "href":"/individuals/reliefs/pensions/AA123456A/$taxYear",
         |         "method":"DELETE",
         |         "rel":"delete-reliefs-pensions"
         |      }
         |   ]
         |}
         |""".stripMargin
    )

    val desResponseBody = Json.parse(s"""
         |{
         |   "submittedOn":"2020-07-14T10:30:18Z",
         |   "pensionReliefs":{
         |      "regularPensionContributions":1999.99,
         |      "oneOffPensionContributionsPaid":1999.99,
         |      "retirementAnnuityPayments":1999.99,
         |      "paymentToEmployersSchemeNoTaxRelief":1999.99,
         |      "overseasPensionSchemeContributions":1999.99
         |   }
         |}
         |""".stripMargin)

    def uri: String    = s"/pensions/$nino/$taxYear"
    def desUri: String = s"/income-tax/reliefs/pensions/$nino/$taxYear"

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
         |        "reason": "message"
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
          DownstreamStub.onSuccess(DownstreamStub.GET, desUri, Status.OK, desResponseBody)
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
          ("Walrus", "2020-21", Status.BAD_REQUEST, NinoFormatError),
          ("AA123456A", "203100", Status.BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "2018-20", Status.BAD_REQUEST, RuleTaxYearRangeInvalidError),
          ("AA123456A", "2019-20", Status.BAD_REQUEST, RuleTaxYearNotSupportedError)
        )
        input.foreach(args => (validationErrorTest _).tupled(args))
      }

      "downstream service error" when {
        def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"downstream returns an $downstreamCode error and status $downstreamStatus" in new Test {

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
              DownstreamStub.onError(DownstreamStub.GET, desUri, downstreamStatus, errorBody(downstreamCode))
            }

            val response: WSResponse = await(request().get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = Seq(
          (Status.BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", Status.BAD_REQUEST, NinoFormatError),
          (Status.BAD_REQUEST, "INVALID_TAX_YEAR", Status.BAD_REQUEST, TaxYearFormatError),
          (Status.NOT_FOUND, "NOT_FOUND", Status.NOT_FOUND, NotFoundError),
          (Status.INTERNAL_SERVER_ERROR, "SERVER_ERROR", Status.INTERNAL_SERVER_ERROR, InternalError),
          (Status.SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", Status.INTERNAL_SERVER_ERROR, InternalError)
        )
        input.foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }

}
