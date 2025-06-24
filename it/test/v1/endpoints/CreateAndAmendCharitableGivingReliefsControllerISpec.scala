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

package test.v1.endpoints

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import common.{RuleGiftAidNonUkAmountWithoutNamesError, RuleGiftsNonUkAmountWithoutNamesError}
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import shared.models.errors._
import shared.services.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}
import shared.support.IntegrationBaseSpec

class CreateAndAmendCharitableGivingReliefsControllerISpec extends IntegrationBaseSpec {

  "Calling the amend endpoint" should {

    "return a 200 status code" when {

      "any valid request is made" in new NonTysTest {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.POST, downstreamUri, OK, JsObject.empty)
        }

        val response: WSResponse = await(request().put(requestJson))
        response.status shouldBe OK
        response.json shouldBe responseBody
        response.header("X-CorrelationId") should not be empty
      }

      "any valid request is made with a TYS tax year" in new TysIfsTest {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.POST, downstreamUri, OK, JsObject.empty)
        }

        val response: WSResponse = await(request().put(requestJson))
        response.status shouldBe OK
        response.json shouldBe responseBody
        response.header("X-CorrelationId") should not be empty
      }

    }

    "return error according to spec" when {

      "validation error" when {
        s"an invalid NINO is provided" in new NonTysTest {
          override val nino: String = "INVALID_NINO"

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
          }

          val response: WSResponse = await(request().put(requestJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe Json.toJson(NinoFormatError)
        }
        s"an invalid taxYear is provided" in new NonTysTest {
          override def mtdTaxYear: String = "INVALID_TAXYEAR"

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
          }

          val response: WSResponse = await(request().put(requestJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe Json.toJson(TaxYearFormatError)
        }
        s"an invalid /giftAidPayments/totalAmount is provided" in new NonTysTest {
          override val requestJson: JsValue = Json.parse(
            s"""
               |{
               |  "giftAidPayments": {
               |    "totalAmount": -1
               |  }
               |}
               |""".stripMargin
          )

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
          }

          val response: WSResponse = await(request().put(requestJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe Json.toJson(ValueFormatError.copy(paths = Some(Seq("/giftAidPayments/totalAmount"))))
        }
        s"a taxYear with range of greater than a year is provided" in new NonTysTest {
          override def mtdTaxYear: String = "2019-21"

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
          }

          val response: WSResponse = await(request().put(requestJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe Json.toJson(RuleTaxYearRangeInvalidError)
        }

        s"a taxYear below 2017-18 is provided" in new NonTysTest {
          override val mtdTaxYear: String        = "2016-17"
          override val downstreamTaxYear: String = "2018"

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
          }

          val response: WSResponse = await(request().put(requestJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe Json.toJson(RuleTaxYearNotSupportedError)
        }

        s"an empty body is provided" in new NonTysTest {
          override val requestJson: JsValue = Json.parse("""{}""")

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
          }

          val response: WSResponse = await(request().put(requestJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe Json.toJson(RuleIncorrectOrEmptyBodyError)
        }
      }

      "downstream service error" when {
        def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"downstream returns $downstreamCode and status $downstreamStatus" in new NonTysTest {

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
              DownstreamStub.onError(DownstreamStub.POST, downstreamUri, downstreamStatus, errorBody(downstreamCode))
            }

            val response: WSResponse = await(request().put(requestJson))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val errors = Seq(
          (BAD_REQUEST, "INVALID_NINO", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "INVALID_TYPE", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "INVALID_TAXYEAR", BAD_REQUEST, TaxYearFormatError),
          (BAD_REQUEST, "INVALID_PAYLOAD", INTERNAL_SERVER_ERROR, InternalError),
          (FORBIDDEN, "NOT_FOUND_INCOME_SOURCE", NOT_FOUND, NotFoundError),
          (FORBIDDEN, "MISSING_CHARITIES_NAME_GIFT_AID", BAD_REQUEST, RuleGiftAidNonUkAmountWithoutNamesError),
          (FORBIDDEN, "MISSING_GIFT_AID_AMOUNT", INTERNAL_SERVER_ERROR, InternalError),
          (FORBIDDEN, "MISSING_CHARITIES_NAME_INVESTMENT", BAD_REQUEST, RuleGiftsNonUkAmountWithoutNamesError),
          (FORBIDDEN, "MISSING_INVESTMENT_AMOUNT", INTERNAL_SERVER_ERROR, InternalError),
          (FORBIDDEN, "INVALID_ACCOUNTING_PERIOD", BAD_REQUEST, RuleTaxYearNotSupportedError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError),
          (GONE, "GONE", INTERNAL_SERVER_ERROR, InternalError),
          (NOT_FOUND, "NOT_FOUND", NOT_FOUND, NotFoundError)
        )

        val extraTysErrors = Seq(
          (BAD_REQUEST, "INVALID_INCOMESOURCE_TYPE", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "INVALID_TAX_YEAR", BAD_REQUEST, TaxYearFormatError),
          (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, InternalError),
          (NOT_FOUND, "INCOME_SOURCE_NOT_FOUND", NOT_FOUND, NotFoundError),
          (UNPROCESSABLE_ENTITY, "INCOMPATIBLE_INCOME_SOURCE", INTERNAL_SERVER_ERROR, InternalError),
          (UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_SUPPORTED", BAD_REQUEST, RuleTaxYearNotSupportedError)
        )

        (errors ++ extraTysErrors).foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }

  private trait Test {

    val nino: String = "AA123456A"
    def mtdTaxYear: String
    def downstreamTaxYear: String

    val amount: BigDecimal = 5000.99

    val requestJson: JsValue = Json.parse(s"""|{
                                              |  "giftAidPayments": {
                                              |    "totalAmount": $amount
                                              |  }
                                              |}
                                              |""".stripMargin)

    val responseBody: JsValue = Json.parse(s"""
                                              |{
                                              |  "links": [
                                              |    {
                                              |      "href": "/individuals/reliefs/charitable-giving/$nino/$mtdTaxYear",
                                              |      "method": "GET",
                                              |      "rel": "self"
                                              |    },
                                              |    {
                                              |      "href": "/individuals/reliefs/charitable-giving/$nino/$mtdTaxYear",
                                              |      "method": "PUT",
                                              |      "rel": "create-and-amend-charitable-giving-tax-relief"
                                              |    },
                                              |    {
                                              |      "href": "/individuals/reliefs/charitable-giving/$nino/$mtdTaxYear",
                                              |      "method": "DELETE",
                                              |      "rel": "delete-charitable-giving-tax-relief"
                                              |    }
                                              |  ]
                                              |}
                                              |""".stripMargin)

    def uri: String = s"/charitable-giving/$nino/$mtdTaxYear"

    def downstreamUri: String

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

  private trait NonTysTest extends Test {
    def mtdTaxYear: String        = "2019-20"
    def downstreamTaxYear: String = "2020"
    def downstreamUri: String     = s"/income-tax/nino/$nino/income-source/charity/annual/$downstreamTaxYear"

  }

  private trait TysIfsTest extends Test {
    def mtdTaxYear: String        = "2023-24"
    def downstreamTaxYear: String = "23-24"
    def downstreamUri: String     = s"/income-tax/$downstreamTaxYear/$nino/income-source/charity/annual"
  }

}
