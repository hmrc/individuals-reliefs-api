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

package v1.endpoints

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import shared.models.errors._
import shared.services.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}
import shared.support.{IntegrationBaseSpec, WireMockMethods}

class RetrievePensionsReliefsControllerISpec extends IntegrationBaseSpec with WireMockMethods {

  "Calling the 'Retrieve Pensions Relief' endpoint" should {
    "return a 200 status code" when {
      "any valid request is made" in new NonTysTest {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUri, OK, downstreamResponse)
        }

        val response: WSResponse = await(mtdRequest.get())
        response.status shouldBe OK
        response.json shouldBe mtdResponse
        response.header("X-CorrelationId") should not be empty
        response.header("Content-Type") shouldBe Some("application/json")
      }

      "a valid request is made for a Tax Year Specific tax year" in new TysIfsTest {
        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUri, OK, downstreamResponse)
        }

        val response: WSResponse = await(mtdRequest.get())
        response.status shouldBe OK
        response.json shouldBe mtdResponse
        response.header("X-CorrelationId") should not be empty
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }
    "return error according to spec" when {

      "validation error" when {
        def validationErrorTest(requestNino: String, requestTaxYear: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new NonTysTest {

            override val nino: String    = requestNino
            override val taxYear: String = requestTaxYear

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(requestNino)
            }

            val response: WSResponse = await(mtdRequest.get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }

        val input = Seq(
          ("Walrus", "2020-21", BAD_REQUEST, NinoFormatError),
          ("AA123456A", "203100", BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "2018-20", BAD_REQUEST, RuleTaxYearRangeInvalidError),
          ("AA123456A", "2019-20", BAD_REQUEST, RuleTaxYearNotSupportedError)
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
              DownstreamStub.onError(DownstreamStub.GET, downstreamUri, downstreamStatus, errorBody(downstreamCode))
            }

            val response: WSResponse = await(mtdRequest.get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        def errorBody(code: String): String =
          s"""
             |{
             |   "code": "$code",
             |   "reason": "message"
             |}
            """.stripMargin

        val errors = Seq(
          (BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "INVALID_TAX_YEAR", BAD_REQUEST, TaxYearFormatError),
          (NOT_FOUND, "NOT_FOUND", NOT_FOUND, NotFoundError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError)
        )
        val extraTysErrors = Seq(
          (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, InternalError),
          (UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_SUPPORTED", BAD_REQUEST, RuleTaxYearNotSupportedError)
        )

        (errors ++ extraTysErrors).foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }

  private trait Test {

    def taxYear: String

    def downstreamTaxYear: String

    def downstreamUri: String

    def taxableEntityId: String = "AA123456A"

    def nino: String = "AA123456A"

    val downstreamResponse: JsValue = Json.parse(s"""
         |{
         |   "submittedOn":"2020-07-14T10:30:18.000Z",
         |   "pensionReliefs":{
         |      "regularPensionContributions":1999.99,
         |      "oneOffPensionContributionsPaid":1999.99,
         |      "retirementAnnuityPayments":1999.99,
         |      "paymentToEmployersSchemeNoTaxRelief":1999.99,
         |      "overseasPensionSchemeContributions":1999.99
         |   }
         |}
         |""".stripMargin)

    val mtdResponse: JsValue = Json.parse(s"""
         |{
         |   "submittedOn":"2020-07-14T10:30:18.000Z",
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
         |""".stripMargin)

    def setupStubs(): StubMapping

    def mtdRequest: WSRequest = {
      setupStubs()
      buildRequest(s"/pensions/$nino/$taxYear")
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.1.0+json"),
          (AUTHORIZATION, "Bearer 123")
        )
    }

  }

  private trait NonTysTest extends Test {

    def taxYear: String = "2020-21"

    def downstreamTaxYear: String = "2020-21" // Supposed to be YYYY-YY not YYYY

    def downstreamUri: String = s"/itsa/income-tax/v1/reliefs/pensions/$taxableEntityId/$downstreamTaxYear"

  }

  private trait TysIfsTest extends Test {

    def taxYear: String = "2023-24"

    def downstreamTaxYear: String = "23-24"

    def downstreamUri: String = s"/income-tax/reliefs/pensions/$downstreamTaxYear/$taxableEntityId"

  }

}
