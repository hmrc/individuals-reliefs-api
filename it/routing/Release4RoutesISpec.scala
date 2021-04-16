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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package routing

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import support.IntegrationBaseSpec
import v1.stubs.{AuditStub, AuthStub, DesStub, MtdIdLookupStub}

class Release4RoutesISpec extends IntegrationBaseSpec {

  override def servicesConfig: Map[String, String] = Map(
    "microservice.services.des.host" -> mockHost,
    "microservice.services.des.port" -> mockPort,
    "microservice.services.ifs.host" -> mockHost,
    "microservice.services.ifs.port" -> mockPort,
    "microservice.services.mtd-id-lookup.host" -> mockHost,
    "microservice.services.mtd-id-lookup.port" -> mockPort,
    "microservice.services.auth.host" -> mockHost,
    "microservice.services.auth.port" -> mockPort,
    "auditing.consumer.baseUri.port" -> mockPort,
    "feature-switch.all-endpoints.enabled" -> "false"
  )

  private trait Test {

    val nino: String = "AA123456A"
    val taxYear: String = "2021-22"

    def amount: BigDecimal = 5000.99

    def setupStubs(): StubMapping

    def uri: String = s"/pensions/$nino/$taxYear"

    def desUri: String = s"/income-tax/reliefs/pensions/$nino/$taxYear"

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
    }
  }

  "Calling the 'amend benefit amounts' endpoint (a release 4 endpoint)" should {

    "return a 200 status code" when {
      "the feature switch is turned off to point to release 4 routes only" in new Test {

        def requestBodyJson: JsValue = Json.parse(
          s"""
             |{
             |  "pensionReliefs": {
             |    "regularPensionContributions": $amount,
             |    "oneOffPensionContributionsPaid": $amount,
             |    "retirementAnnuityPayments": $amount,
             |    "paymentToEmployersSchemeNoTaxRelief": $amount,
             |    "overseasPensionSchemeContributions": $amount
             |  }
             |}
             |""".stripMargin
        )

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.PUT, desUri, NO_CONTENT, JsObject.empty)
        }

        val response: WSResponse = await(request().put(requestBodyJson))
        response.status shouldBe OK
      }
    }
  }

  "Calling the 'amend benefit' endpoint (not a release 4 endpoint)" should {

    "return a 404 status code" when {
      "the feature switch is turned off to point to release 4 routes only" in new Test {

        val requestBodyJson: JsValue = Json.parse(
          s"""
             |{
             |  "foreignTaxCreditRelief": {
             |    "amount": $amount
             |  }
             |}
             |""".stripMargin
        )

        override def uri: String = s"/reliefs/foreign/$nino/$taxYear"

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
        }

        val response: WSResponse = await(request().put(requestBodyJson))
        response.status shouldBe NOT_FOUND
      }
    }
  }

}
