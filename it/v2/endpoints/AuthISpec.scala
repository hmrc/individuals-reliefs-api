/*
 * copyright 2024 HM Revenue & Customs
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

package v2.endpoints

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import shared.services.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}
import shared.support.IntegrationBaseSpec

class AuthISpec extends IntegrationBaseSpec {

  private trait Test {
    val nino    = "AA123456A"
    val taxYear = "2021-22"

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      setupStubs()
      buildRequest(s"/investment/$nino/$taxYear")
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.1.0+json"),
          (AUTHORIZATION, "Bearer 123") // some bearer token
        )
    }

    def desUri: String = s"/income-tax/reliefs/investment/$nino/$taxYear"

    val desResponse: JsValue = Json.parse("""
        |{
        |  "submittedOn": "2020-06-17T10:53:38.000Z",
        |  "vctSubscription":[
        |    {
        |      "uniqueInvestmentRef": "VCTREF",
        |      "name": "VCT Fund X",
        |      "dateOfInvestment": "2018-04-16",
        |      "amountInvested": 23312.00,
        |      "reliefClaimed": 1334.00
        |      }
        |  ],
        |  "eisSubscription":[
        |    {
        |      "uniqueInvestmentRef": "XTAL",
        |      "name": "EIS Fund X",
        |      "knowledgeIntensive": true,
        |      "dateOfInvestment": "2020-12-12",
        |      "amountInvested": 23312.00,
        |      "reliefClaimed": 43432.00
        |    }
        |  ],
        |  "communityInvestment": [
        |    {
        |      "uniqueInvestmentRef": "CIREF",
        |      "name": "CI X",
        |      "dateOfInvestment": "2020-12-12",
        |      "amountInvested": 6442.00,
        |      "reliefClaimed": 2344.00
        |    }
        |  ],
        |  "seedEnterpriseInvestment": [
        |    {
        |      "uniqueInvestmentRef": "123412/1A",
        |      "companyName": "Company Inc",
        |      "dateOfInvestment": "2020-12-12",
        |      "amountInvested": 123123.22,
        |      "reliefClaimed": 3432.00
        |    }
        |  ],
        |  "socialEnterpriseInvestment": [
        |    {
        |      "uniqueInvestmentRef": "123412/1A",
        |      "socialEnterpriseName": "SE Inc",
        |      "dateOfInvestment": "2020-12-12",
        |      "amountInvested": 123123.22,
        |      "reliefClaimed": 3432.00
        |    }
        |  ]
        |}
    """.stripMargin)

  }

  "Calling the investment endpoint" when {

    "the NINO cannot be converted to a MTD ID" should {

      "return 500" in new Test {
        override val nino: String = "AA123456A"

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.error(nino, Status.INTERNAL_SERVER_ERROR)
        }

        val response: WSResponse = await(request().get())
        response.status shouldBe Status.INTERNAL_SERVER_ERROR
      }
    }

    "an MTD ID is successfully retrieve from the NINO and the user is authorised" should {

      "MTD ID lookup fails with a 403" should {

        "return 403" in new Test {
          override val nino: String = "AA123456A"

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            MtdIdLookupStub.error(nino, Status.FORBIDDEN)
          }

          val response: WSResponse = await(request().get())
          response.status shouldBe Status.FORBIDDEN
        }

      }

      "return success status" in new Test {
        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.GET, desUri, Status.OK, desResponse)
        }

        val response: WSResponse = await(request().get())
        response.status shouldBe Status.OK
      }
    }

    "an MTD ID is successfully retrieve from the NINO and the user is NOT logged in" should {

      "return 403" in new Test {
        override val nino: String = "AA123456A"

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.ninoFound(nino)
          AuthStub.unauthorisedNotLoggedIn()
        }

        val response: WSResponse = await(request().get())
        response.status shouldBe Status.FORBIDDEN
      }
    }

    "an MTD ID is successfully retrieve from the NINO and the user is NOT authorised" should {

      "return 403" in new Test {
        override val nino: String = "AA123456A"

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.ninoFound(nino)
          AuthStub.unauthorisedOther()
        }

        val response: WSResponse = await(request().get())
        response.status shouldBe Status.FORBIDDEN
      }
    }
  }

}
