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
import play.api.http.Status._
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import support.IntegrationBaseSpec
import v1.models.errors._
import v1.stubs.{AuditStub, AuthStub, DesStub, MtdIdLookupStub}

class AmendForeignReliefsControllerISpec extends IntegrationBaseSpec {

  private trait Test {

    val nino: String    = "AA123456A"
    val taxYear: String = "2021-22"

    val amount: BigDecimal = 5000.99

    val requestBodyJson: JsValue = Json.parse(
      s"""
         |{
         |  "foreignTaxCreditRelief": {
         |    "amount": $amount
         |  },
         |  "foreignIncomeTaxCreditRelief": [
         |    {
         |      "countryCode": "FRA",
         |      "foreignTaxPaid": $amount,
         |      "taxableAmount": $amount,
         |      "employmentLumpSum": true
         |    }
         |  ],
         |  "foreignTaxForFtcrNotClaimed": {
         |    "amount": $amount
         |  }
         |}
         |""".stripMargin
    )

    val responseBody: JsValue = Json.parse(s"""
         |{
         |  "links": [
         |    {
         |      "href": "/individuals/reliefs/foreign/$nino/$taxYear",
         |      "method": "GET",
         |      "rel": "self"
         |    },
         |    {
         |      "href": "/individuals/reliefs/foreign/$nino/$taxYear",
         |      "method": "PUT",
         |      "rel": "create-and-amend-reliefs-foreign"
         |    },
         |    {
         |      "href": "/individuals/reliefs/foreign/$nino/$taxYear",
         |      "method": "DELETE",
         |      "rel": "delete-reliefs-foreign"
         |    }
         |  ]
         |}
         |""".stripMargin)

    def uri: String = s"/foreign/$nino/$taxYear"

    def desUri: String = s"/income-tax/reliefs/foreign/$nino/$taxYear"

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

    "return error according to spec" when {

      "validation error" when {
        s"an invalid NINO is provided" in new Test {
          override val nino: String = "INVALID_NINO"

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
          }

          val response: WSResponse = await(request().put(requestBodyJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe Json.toJson(NinoFormatError)
        }
        s"an invalid taxYear is provided" in new Test {
          override val taxYear: String = "INVALID_TAXYEAR"

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
          }

          val response: WSResponse = await(request().put(requestBodyJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe Json.toJson(TaxYearFormatError)
        }
        s"an invalid /foreignTaxCreditRelief/amount is provided" in new Test {
          override val requestBodyJson: JsValue = Json.parse(
            s"""
               |{
               |  "foreignTaxCreditRelief": {
               |    "amount": -1
               |  }
               |}
               |""".stripMargin
          )

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
          }

          val response: WSResponse = await(request().put(requestBodyJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe Json.toJson(ValueFormatError.copy(paths = Some(Seq("/foreignTaxCreditRelief/amount"))))
        }
        s"the country code is too short" in new Test {
          override val requestBodyJson: JsValue = Json.parse(
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

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
          }

          val response: WSResponse = await(request().put(requestBodyJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe Json.toJson(CountryCodeFormatError.copy(paths = Some(Seq("/foreignIncomeTaxCreditRelief/0/countryCode"))))
        }
        s"the country code is not a valid ISO 3166-1 alpha-3 code" in new Test {
          override val requestBodyJson: JsValue = Json.parse(
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

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
          }

          val response: WSResponse = await(request().put(requestBodyJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe Json.toJson(RuleCountryCodeError.copy(paths = Some(Seq("/foreignIncomeTaxCreditRelief/0/countryCode"))))
        }
        s"an invalid /foreignIncomeTaxCreditRelief/foreignTaxPaid is provided" in new Test {
          override val requestBodyJson: JsValue = Json.parse(
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

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
          }

          val response: WSResponse = await(request().put(requestBodyJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe Json.toJson(ValueFormatError.copy(paths = Some(Seq("/foreignIncomeTaxCreditRelief/0/foreignTaxPaid"))))
        }
        s"an invalid /foreignIncomeTaxCreditRelief/taxableAmount is provided" in new Test {
          override val requestBodyJson: JsValue = Json.parse(
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

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
          }

          val response: WSResponse = await(request().put(requestBodyJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe Json.toJson(ValueFormatError.copy(paths = Some(Seq("/foreignIncomeTaxCreditRelief/0/taxableAmount"))))
        }
        s"an invalid /foreignTaxForFtcrNotClaimed/amount is provided" in new Test {
          override val requestBodyJson: JsValue = Json.parse(
            s"""
               |{
               |  "foreignTaxForFtcrNotClaimed": {
               |    "amount": -1
               |  }
               |}
               |""".stripMargin
          )

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
          }

          val response: WSResponse = await(request().put(requestBodyJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe Json.toJson(ValueFormatError.copy(paths = Some(Seq("/foreignTaxForFtcrNotClaimed/amount"))))
        }
        s"a taxYear with range of greater than a year is provided" in new Test {
          override val taxYear: String = "2019-21"

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
          }

          val response: WSResponse = await(request().put(requestBodyJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe Json.toJson(RuleTaxYearRangeInvalidError)
        }

        s"a taxYear below 2020-21 is provided" in new Test {
          override val taxYear: String = "2019-20"

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
          }

          val response: WSResponse = await(request().put(requestBodyJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe Json.toJson(RuleTaxYearNotSupportedError)
        }

        s"an empty body is provided" in new Test {
          override val requestBodyJson: JsValue = Json.parse("""{}""")

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
          }

          val response: WSResponse = await(request().put(requestBodyJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe Json.toJson(RuleIncorrectOrEmptyBodyError)
        }
        s"a body missing mandatory fields is provided" in new Test {
          override val requestBodyJson: JsValue = Json.parse("""{
              | "foreignTaxCreditRelief": {}
              |}""".stripMargin)

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
          }

          val response: WSResponse = await(request().put(requestBodyJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe Json.toJson(RuleIncorrectOrEmptyBodyError)
        }
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
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, DownstreamError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, DownstreamError)
        )

        input.foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }

}
