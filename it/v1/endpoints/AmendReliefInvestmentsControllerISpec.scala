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
import play.api.http.Status
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import support.IntegrationBaseSpec
import v1.stubs.{AuditStub, AuthStub, DesStub, MtdIdLookupStub}

class AmendReliefInvestmentsControllerISpec extends IntegrationBaseSpec {

  private trait Test {

    val nino = "AA123456A"
    val taxYear = "2019-20"

    val requestBody = Json.parse(
      s"""
         |{
         |  "vctSubscriptionsItems":[
         |    {
         |      "uniqueInvestmentRef": "VCTREF",
         |      "name": "VCT Fund X",
         |      "dateOfInvestment": "2018-04-16",
         |      "amountInvested": 23312.00,
         |      "reliefClaimed": 1334.00
         |      }
         |  ],
         |  "eisSubscriptionsItems":[
         |    {
         |      "uniqueInvestmentRef": "XTAL",
         |      "name": "EIS Fund X",
         |      "knowledgeIntensive": true,
         |      "dateOfInvestment": "2020-12-12",
         |      "amountInvested": 23312.00,
         |      "reliefClaimed": 43432.00
         |    }
         |  ],
         |  "communityInvestmentItems": [
         |    {
         |      "uniqueInvestmentRef": "CIREF",
         |      "name": "CI X",
         |      "dateOfInvestment": "2020-12-12",
         |      "amountInvested": 6442.00,
         |      "reliefClaimed": 2344.00
         |    }
         |  ],
         |  "seedEnterpriseInvestmentItems": [
         |    {
         |      "uniqueInvestmentRef": "123412/1A",
         |      "companyName": "Company Inc",
         |      "dateOfInvestment": "2020-12-12",
         |      "amountInvested": 123123.22,
         |      "reliefClaimed": 3432.00
         |    }
         |  ],
         |  "socialEnterpriseInvestmentItems": [
         |    {
         |      "uniqueInvestmentRef": "123412/1A",
         |      "socialEnterpriseName": "SE Inc",
         |      "dateOfInvestment": "2020-12-12",
         |      "amountInvested": 123123.22,
         |      "reliefClaimed": 3432.00
         |    }
         |  ]
         |}
         |""".stripMargin
    )

    val responseBody = Json.parse(
      s"""
         |{
         |  "links": [
         |    {
         |      "href": "/individuals/reliefs/investment/$nino/$taxYear",
         |      "method": "GET",
         |      "rel": "self"
         |    },
         |    {
         |      "href": "/individuals/reliefs/investment/$nino/$taxYear",
         |      "method": "PUT",
         |      "rel": "amend-relief-investments"
         |    },
         |    {
         |      "href": "/individuals/reliefs/investment/$nino/$taxYear",
         |      "method": "DELETE",
         |      "rel": "delete-relief-investments"
         |    }
         |  ]
         |}
         |""".stripMargin)

    def uri: String = s"/investment/$nino/$taxYear"

    def desUri: String = s"/reliefs/investment/$nino/$taxYear"

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
          DesStub.onSuccess(DesStub.PUT, desUri, Status.NO_CONTENT, JsObject.empty)
        }

        val response: WSResponse = await(request().put(requestBody))
        response.status shouldBe Status.OK
        response.json shouldBe responseBody
        response.header("X-CorrelationId").nonEmpty shouldBe true
      }
    }
  }
}
