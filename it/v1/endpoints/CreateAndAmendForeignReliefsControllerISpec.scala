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

import api.models.errors._
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec
import v1.fixtures.CreateAndAmendForeignReliefsFixtures.{requestBodyJson, responseWithHateoasLinks}
import v1.stubs.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}

class CreateAndAmendForeignReliefsControllerISpec extends IntegrationBaseSpec {

  "Calling the create and amend endpoint" should {

    "return a 200 status code" when {

      "any valid request is made" in new NonTysTest {

        override def setupStubs(): Unit = {
          DownstreamStub.onSuccess(DownstreamStub.PUT, downstreamUri, NO_CONTENT, JsObject.empty)
        }

        val response: WSResponse = await(request().put(requestBodyJson))
        response.status shouldBe OK
        response.json shouldBe responseWithHateoasLinks(mtdTaxYear)
        response.header("X-CorrelationId") should not be empty
      }

      "any valid request is made for a Tax Year Specific (TYS) tax year" in new TysIfsTest {

        override def setupStubs(): Unit = {
          DownstreamStub.onSuccess(DownstreamStub.PUT, downstreamUri, NO_CONTENT, JsObject.empty)
        }

        val response: WSResponse = await(request().put(requestBodyJson))
        response.status shouldBe OK
        response.json shouldBe responseWithHateoasLinks(mtdTaxYear)
        response.header("X-CorrelationId") should not be empty
      }
    }

    "return error according to spec" when {

      "validation error" when {
        s"an invalid NINO is provided" in new NonTysTest {
          override val nino: String = "INVALID_NINO"

          val response: WSResponse = await(request().put(requestBodyJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe Json.toJson(NinoFormatError)
        }
        s"an invalid taxYear is provided" in new NonTysTest {
          override val mtdTaxYear: String = "INVALID_TAXYEAR"

          val response: WSResponse = await(request().put(requestBodyJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe Json.toJson(TaxYearFormatError)
        }
        s"an invalid /foreignTaxCreditRelief/amount is provided" in new NonTysTest {
          val requestBodyJson: JsValue = Json.parse(
            s"""
               |{
               |  "foreignTaxCreditRelief": {
               |    "amount": -1
               |  }
               |}
               |""".stripMargin
          )

          val response: WSResponse = await(request().put(requestBodyJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe Json.toJson(ValueFormatError.copy(paths = Some(Seq("/foreignTaxCreditRelief/amount"))))
        }
        s"the country code is too short" in new NonTysTest {
          val requestBodyJson: JsValue = Json.parse(
            s"""
               |{
               |  "foreignIncomeTaxCreditRelief": [
               |    {
               |      "countryCode": "BADCODE",
               |      "taxableAmount": 1.00,
               |      "employmentLumpSum": true
               |    }
               |  ]
               |}
               |""".stripMargin
          )

          val response: WSResponse = await(request().put(requestBodyJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe Json.toJson(CountryCodeFormatError.copy(paths = Some(Seq("/foreignIncomeTaxCreditRelief/0/countryCode"))))
        }
        s"the country code is not a valid ISO 3166-1 alpha-3 code" in new NonTysTest {
          val requestBodyJson: JsValue = Json.parse(
            s"""
               |{
               |  "foreignIncomeTaxCreditRelief": [
               |    {
               |      "countryCode": "GER",
               |      "taxableAmount": 1.00,
               |      "employmentLumpSum": true
               |    }
               |  ]
               |}
               |""".stripMargin
          )

          val response: WSResponse = await(request().put(requestBodyJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe Json.toJson(RuleCountryCodeError.copy(paths = Some(Seq("/foreignIncomeTaxCreditRelief/0/countryCode"))))
        }
        s"an invalid /foreignIncomeTaxCreditRelief/foreignTaxPaid is provided" in new NonTysTest {
          val requestBodyJson: JsValue = Json.parse(
            s"""
               |{
               |  "foreignIncomeTaxCreditRelief": [
               |    {
               |      "countryCode": "FRA",
               |      "foreignTaxPaid": -1,
               |      "taxableAmount": 1.00,
               |      "employmentLumpSum": true
               |    }
               |  ]
               |}
               |""".stripMargin
          )

          val response: WSResponse = await(request().put(requestBodyJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe Json.toJson(ValueFormatError.copy(paths = Some(Seq("/foreignIncomeTaxCreditRelief/0/foreignTaxPaid"))))
        }
        s"an invalid /foreignIncomeTaxCreditRelief/taxableAmount is provided" in new NonTysTest {
          val requestBodyJson: JsValue = Json.parse(
            s"""
               |{
               |  "foreignIncomeTaxCreditRelief": [
               |    {
               |      "countryCode": "FRA",
               |      "taxableAmount": -1,
               |      "employmentLumpSum": true
               |    }
               |  ]
               |}
               |""".stripMargin
          )

          val response: WSResponse = await(request().put(requestBodyJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe Json.toJson(ValueFormatError.copy(paths = Some(Seq("/foreignIncomeTaxCreditRelief/0/taxableAmount"))))
        }
        s"an invalid /foreignTaxForFtcrNotClaimed/amount is provided" in new NonTysTest {
          val requestBodyJson: JsValue = Json.parse(
            s"""
               |{
               |  "foreignTaxForFtcrNotClaimed": {
               |    "amount": -1
               |  }
               |}
               |""".stripMargin
          )

          val response: WSResponse = await(request().put(requestBodyJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe Json.toJson(ValueFormatError.copy(paths = Some(Seq("/foreignTaxForFtcrNotClaimed/amount"))))
        }
        s"a taxYear with range of greater than a year is provided" in new NonTysTest {
          override val mtdTaxYear: String = "2019-21"

          val response: WSResponse = await(request().put(requestBodyJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe Json.toJson(RuleTaxYearRangeInvalidError)
        }

        s"a taxYear below 2020-21 is provided" in new NonTysTest {
          override val mtdTaxYear: String = "2019-20"

          val response: WSResponse = await(request().put(requestBodyJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe Json.toJson(RuleTaxYearNotSupportedError)
        }

        s"an empty body is provided" in new NonTysTest {
          val requestBodyJson: JsValue = Json.parse("""{}""")

          val response: WSResponse = await(request().put(requestBodyJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe Json.toJson(RuleIncorrectOrEmptyBodyError)
        }
        s"a body missing mandatory fields is provided" in new NonTysTest {
          val requestBodyJson: JsValue = Json.parse("""{
              | "foreignTaxCreditRelief": {}
              |}""".stripMargin)

          val response: WSResponse = await(request().put(requestBodyJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe Json.toJson(RuleIncorrectOrEmptyBodyError.withPath("/foreignTaxCreditRelief/amount"))
        }
      }

      "downstream service error" when {
        def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"downstream returns an $downstreamCode error and status $downstreamStatus" in new NonTysTest {

            override def setupStubs(): Unit = {
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
          (BAD_REQUEST, "INVALID_PAYLOAD", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, InternalError),
          (UNPROCESSABLE_ENTITY, "UNPROCESSABLE_ENTITY", INTERNAL_SERVER_ERROR, InternalError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError)
        )

        val extraTysErrors = List(
          (BAD_REQUEST, "INVALID_CORRELATION_ID", INTERNAL_SERVER_ERROR, InternalError),
          (UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_SUPPORTED", BAD_REQUEST, RuleTaxYearNotSupportedError)
        )

        (errors ++ extraTysErrors).foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }

  private trait Test {

    val nino: String = "AA123456A"

    def mtdTaxYear: String
    def downstreamUri: String

    def setupStubs(): Unit = {}

    def request(): WSRequest = {
      AuditStub.audit()
      AuthStub.authorised()
      MtdIdLookupStub.ninoFound(nino)
      setupStubs()

      buildRequest(s"/foreign/$nino/$mtdTaxYear")
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

  private trait NonTysTest extends Test {
    def mtdTaxYear: String = "2021-22"

    def downstreamUri: String = s"/income-tax/reliefs/foreign/$nino/2021-22"
  }

  private trait TysIfsTest extends Test {
    def mtdTaxYear: String = "2023-24"

    def downstreamUri: String = s"/income-tax/reliefs/foreign/23-24/$nino"
  }

}
