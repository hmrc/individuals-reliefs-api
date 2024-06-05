/*
 * Copyright 2022 HM Revenue & Customs
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
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec
import v1.stubs.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}

class DeleteReliefInvestmentsControllerISpec extends IntegrationBaseSpec {

  private trait Test {

    val nino = "AA123456A"
    def taxYear: String

    def downstreamUri: String

    def setupStubs(): Unit = ()

    def request(): WSRequest = {
      AuditStub.audit()
      AuthStub.authorised()
      MtdIdLookupStub.ninoFound(nino)
      setupStubs()
      buildRequest(s"/investment/$nino/$taxYear")
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
    def taxYear: String       = "2021-22"
    def downstreamUri: String = s"/income-tax/reliefs/investment/$nino/2021-22"
  }

  private trait TysTest extends Test {
    def taxYear: String = "2023-24"

    def downstreamUri: String = s"/income-tax/reliefs/investment/23-24/$nino"
  }

  "Calling the delete endpoint" should {

    "return a 204 status code" when {

      "any valid request is made" in new NonTysTest {

        override def setupStubs(): Unit =
          DownstreamStub.onSuccess(DownstreamStub.DELETE, downstreamUri, NO_CONTENT, JsObject.empty)

        val response: WSResponse = await(request().delete())
        response.status shouldBe NO_CONTENT
        response.header("X-CorrelationId") should not be empty
      }

      "any valid request is made (TYS)" in new TysTest {

        override def setupStubs(): Unit =
          DownstreamStub.onSuccess(DownstreamStub.DELETE, downstreamUri, NO_CONTENT, JsObject.empty)

        val response: WSResponse = await(request().delete())
        response.status shouldBe NO_CONTENT
        response.header("X-CorrelationId") should not be empty
      }
    }

    "return error according to spec" when {

      "validation error" when {
        def validationErrorTest(requestNino: String, requestId: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new NonTysTest {

            override val nino: String    = requestNino
            override val taxYear: String = requestId

            val response: WSResponse = await(request().delete())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }

        val input = Seq(
          ("Walrus", "2021-22", BAD_REQUEST, NinoFormatError),
          ("AA123456A", "203100", BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "2019-20", BAD_REQUEST, RuleTaxYearNotSupportedError),
          ("AA123456A", "2018-20", BAD_REQUEST, RuleTaxYearRangeInvalidError)
        )
        input.foreach(args => (validationErrorTest _).tupled(args))
      }

      "downstream service error" when {
        def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"downstream returns an $downstreamCode error and status $downstreamStatus" in new NonTysTest {

            override def setupStubs(): Unit =
              DownstreamStub.onError(DownstreamStub.DELETE, downstreamUri, downstreamStatus, errorBody(downstreamCode))

            val response: WSResponse = await(request().delete())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val errors = Seq(
          (BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "FORMAT_TAX_YEAR", BAD_REQUEST, TaxYearFormatError),
          (NOT_FOUND, "NO_DATA_FOUND", NOT_FOUND, NotFoundError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError)
        )

        val extraTysErrors = Seq(
          (BAD_REQUEST, "INVALID_CORRELATION_ID", INTERNAL_SERVER_ERROR, InternalError),
          (UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_SUPPORTED", BAD_REQUEST, RuleTaxYearNotSupportedError)
        )

        (errors ++ extraTysErrors).foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }

}
