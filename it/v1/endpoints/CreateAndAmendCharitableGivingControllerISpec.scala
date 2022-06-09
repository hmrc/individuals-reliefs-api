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
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec
import v1.models.errors._
import v1.stubs.{AuditStub, AuthStub, DesStub, MtdIdLookupStub}

class CreateAndAmendCharitableGivingControllerISpec extends IntegrationBaseSpec {

  private trait Test {

    val nino: String       = "AA123456A"
    val mtdTaxYear: String = "2021-22"
    val desTaxYear: String = "2022"

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

    def desUri(yearFromTo: String): String = s"/income-tax/nino/$nino/income-source/charity/annual/$yearFromTo"

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

  "Calling the amend endpoint" should {

    "return a 200 status code" when {

      "any valid request is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.POST, desUri(desTaxYear), OK, JsObject.empty)
        }

        val response: WSResponse = await(request().put(requestJson))
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

          val response: WSResponse = await(request().put(requestJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe Json.toJson(NinoFormatError)
        }
        s"an invalid taxYear is provided" in new Test {
          override val mtdTaxYear: String = "INVALID_TAXYEAR"

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
          }

          val response: WSResponse = await(request().put(requestJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe Json.toJson(TaxYearFormatError)
        }
        s"an invalid /giftAidPayments/totalAmount is provided" in new Test {
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
        s"a taxYear with range of greater than a year is provided" in new Test {
          override val mtdTaxYear: String = "2019-21"

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
          }

          val response: WSResponse = await(request().put(requestJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe Json.toJson(RuleTaxYearRangeInvalidError)
        }

        s"a taxYear below 2019-21 is provided" in new Test {
          override val mtdTaxYear: String = "2017-18"
          override val desTaxYear: String = "2018"

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DesStub.onError(DesStub.POST, desUri(desTaxYear), BAD_REQUEST, errorBody("INVALID_ACCOUNTING_PERIOD"))
          }

          val response: WSResponse = await(request().put(requestJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe Json.toJson(RuleTaxYearNotSupportedError)
        }

        s"an empty body is provided" in new Test {
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
        def serviceErrorTest(desStatus: Int, desCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"DES returns $desCode and status $desStatus" in new Test {

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
              DesStub.onError(DesStub.POST, desUri(desTaxYear), desStatus, errorBody(desCode))
            }

            val response: WSResponse = await(request().put(requestJson))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = Seq(
          (BAD_REQUEST, "INVALID_NINO", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "INVALID_TYPE", INTERNAL_SERVER_ERROR, DownstreamError),
          (BAD_REQUEST, "INVALID_TAXYEAR", BAD_REQUEST, TaxYearFormatError),
          (BAD_REQUEST, "INVALID_PAYLOAD", INTERNAL_SERVER_ERROR, DownstreamError),
          (FORBIDDEN, "NOT_FOUND_INCOME_SOURCE", NOT_FOUND, NotFoundError),
          (FORBIDDEN, "MISSING_CHARITIES_NAME_GIFT_AID", BAD_REQUEST, RuleGiftAidNonUkAmountWithoutNamesError),
          (FORBIDDEN, "MISSING_GIFT_AID_AMOUNT", INTERNAL_SERVER_ERROR, DownstreamError),
          (FORBIDDEN, "MISSING_CHARITIES_NAME_INVESTMENT", BAD_REQUEST, RuleGiftsNonUkAmountWithoutNamesError),
          (FORBIDDEN, "MISSING_INVESTMENT_AMOUNT", INTERNAL_SERVER_ERROR, DownstreamError),
          (FORBIDDEN, "INVALID_ACCOUNTING_PERIOD", BAD_REQUEST, RuleTaxYearNotSupportedError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, DownstreamError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, DownstreamError),
          (GONE, "GONE", INTERNAL_SERVER_ERROR, DownstreamError),
          (NOT_FOUND, "NOT_FOUND", NOT_FOUND, NotFoundError)
        )
        input.foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }

}
