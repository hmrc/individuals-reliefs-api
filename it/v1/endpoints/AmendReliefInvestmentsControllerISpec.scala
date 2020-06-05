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
import play.api.http.Status._
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import support.IntegrationBaseSpec
import v1.models.errors._
import v1.stubs.{AuditStub, AuthStub, DesStub, MtdIdLookupStub}

class AmendReliefInvestmentsControllerISpec extends IntegrationBaseSpec {

  private trait Test {

    val nino: String = "AA123456A"
    val taxYear: String = "2019-20"
    val correlationId: String = "X-123"

    val requestBodyJson = Json.parse(
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
          DesStub.onSuccess(DesStub.PUT, desUri, NO_CONTENT, JsObject.empty)
        }

        val response: WSResponse = await(request().put(requestBodyJson))
        response.status shouldBe OK
        response.json shouldBe responseBody
        response.header("X-CorrelationId").nonEmpty shouldBe true
      }
    }
    "return a 400 with multiple errors" when {
      "all field value validations fail on the request body" in new Test {

        val allInvalidValueRequestBodyJson: JsValue = Json.parse(
          """
            |{
            |  "vctSubscriptionsItems":[
            |    {
            |      "uniqueInvestmentRef": "VCT???REF",
            |      "name": "VCT F!!!und X",
            |      "dateOfInvestment": "18-04-16",
            |      "amountInvested": -23312.00,
            |      "reliefClaimed": -1334.00
            |    },
            |    {
            |      "uniqueInvestmentRef": "VCT????REF",
            |      "name": "VCT Fund!!! X",
            |      "dateOfInvestment": "18-04-16",
            |      "amountInvested": 999999999993.00,
            |      "reliefClaimed": 999999999993.00
            |    }
            |  ],
            |  "eisSubscriptionsItems":[
            |    {
            |      "uniqueInvestmentRef": "XT????AL",
            |      "name": "EIS Fun!!!d X",
            |      "knowledgeIntensive": true,
            |      "dateOfInvestment": "20-12-12",
            |      "amountInvested": -23312.00,
            |      "reliefClaimed": -43432.00
            |    }
            |  ],
            |  "communityInvestmentItems": [
            |    {
            |      "uniqueInvestmentRef": "CIRE/????F",
            |      "name": "CI !!!X",
            |      "dateOfInvestment": "20-12-12",
            |      "amountInvested": 6442.004,
            |      "reliefClaimed": 2344.0204
            |    }
            |  ],
            |  "seedEnterpriseInvestmentItems": [
            |    {
            |      "uniqueInvestmentRef": "123412????/1A",
            |      "companyName": "C!!!!ompany Inc",
            |      "dateOfInvestment": "20-12-12",
            |      "amountInvested": -123123.22,
            |      "reliefClaimed": -3432.00
            |    }
            |  ],
            |  "socialEnterpriseInvestmentItems": [
            |    {
            |      "uniqueInvestmentRef": "12341????2/1A",
            |      "socialEnterpriseName": "SE I!!!!nc",
            |      "dateOfInvestment": "20-12-12",
            |      "amountInvested": -123123.22,
            |      "reliefClaimed": -3432.00
            |    }
            |  ]
            |}
            |""".stripMargin
        )

        val allInvalidValueRequestError: List[MtdError] = List(
          ValueFormatError.copy(
            message = "The field should be between 1 and 99999999999.99",
            paths = Some(List(
              "/vctSubscriptionsItems/0/amountInvested",
              "/vctSubscriptionsItems/0/reliefClaimed",
              "/vctSubscriptionsItems/1/amountInvested",
              "/vctSubscriptionsItems/1/reliefClaimed",
              "/eisSubscriptionsItems/0/amountInvested",
              "/eisSubscriptionsItems/0/reliefClaimed",
              "/communityInvestmentItems/0/amountInvested",
              "/communityInvestmentItems/0/reliefClaimed",
              "/seedEnterpriseInvestmentItems/0/amountInvested",
              "/seedEnterpriseInvestmentItems/0/reliefClaimed",
              "/socialEnterpriseInvestmentItems/0/amountInvested",
              "/socialEnterpriseInvestmentItems/0/reliefClaimed"
            ))
          ),
          DateOfInvestmentFormatError.copy(
            message = "The format of the investment date is invalid",
              paths = Some(List(
              "/vctSubscriptionsItems/0/dateOfInvestment",
              "/vctSubscriptionsItems/1/dateOfInvestment",
              "/eisSubscriptionsItems/0/dateOfInvestment",
              "/communityInvestmentItems/0/dateOfInvestment",
              "/seedEnterpriseInvestmentItems/0/dateOfInvestment",
              "/socialEnterpriseInvestmentItems/0/dateOfInvestment"
            ))
          ),
          InvestmentRefFormatError.copy(
            message = "The format of unique investment reference is invalid",
            paths = Some(List(
              "/vctSubscriptionsItems/0/uniqueInvestmentRef",
              "/vctSubscriptionsItems/1/uniqueInvestmentRef",
              "/eisSubscriptionsItems/0/uniqueInvestmentRef",
              "/communityInvestmentItems/0/uniqueInvestmentRef",
              "/seedEnterpriseInvestmentItems/0/uniqueInvestmentRef",
              "/socialEnterpriseInvestmentItems/0/uniqueInvestmentRef"
            ))
          ),
          NameFormatError.copy(
            message = "The format of the name is invalid",
            paths = Some(List(
              "/vctSubscriptionsItems/0/name",
              "/vctSubscriptionsItems/1/name",
              "/eisSubscriptionsItems/0/name",
              "/communityInvestmentItems/0/name",
              "/seedEnterpriseInvestmentItems/0/companyName",
              "/socialEnterpriseInvestmentItems/0/socialEnterpriseName"
            ))
          )
        )

        val wrappedErrors: ErrorWrapper = ErrorWrapper(
          correlationId = Some(correlationId),
          error = BadRequestError,
          errors = Some(allInvalidValueRequestError)
        )

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
        }

        val response: WSResponse = await(request().put(allInvalidValueRequestBodyJson))
        response.status shouldBe BAD_REQUEST
        response.json shouldBe Json.toJson(wrappedErrors)
      }
    }

    "return error according to spec" when {

      val validRequestBodyJson: JsValue = Json.parse(
        """|{
           |  "vctSubscriptionsItems":[
           |    {
           |      "uniqueInvestmentRef": "VCTREF",
           |      "name": "VCT Fund X",
           |      "dateOfInvestment": "2018-04-16",
           |      "amountInvested": 23312.00,
           |      "reliefClaimed": 1334.00
           |    },
           |    {
           |      "uniqueInvestmentRef": "VCTREF",
           |      "name": "VCT Fund X",
           |      "dateOfInvestment": "2018-04-16",
           |      "amountInvested": 23312.00,
           |      "reliefClaimed": 1334.00
           |    }
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

      val allInvalidvalueFormatRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |  "vctSubscriptionsItems":[
          |    {
          |      "uniqueInvestmentRef": "VCTREF",
          |      "name": "VCT Fund X",
          |      "dateOfInvestment": "2018-04-16",
          |      "amountInvested": -23312.00,
          |      "reliefClaimed": -1334.00
          |    },
          |    {
          |      "uniqueInvestmentRef": "VCTREF",
          |      "name": "VCT Fund X",
          |      "dateOfInvestment": "2018-04-16",
          |      "amountInvested": -23312.00,
          |      "reliefClaimed": -1334.00
          |    }
          |  ],
          |  "eisSubscriptionsItems":[
          |    {
          |      "uniqueInvestmentRef": "XTAL",
          |      "name": "EIS Fund X",
          |      "knowledgeIntensive": true,
          |      "dateOfInvestment": "2020-12-12",
          |      "amountInvested": -23312.00,
          |      "reliefClaimed": -43432.00
          |    }
          |  ],
          |  "communityInvestmentItems": [
          |    {
          |      "uniqueInvestmentRef": "CIREF",
          |      "name": "CI X",
          |      "dateOfInvestment": "2020-12-12",
          |      "amountInvested": -6442.00,
          |      "reliefClaimed": -2344.00
          |    }
          |  ],
          |  "seedEnterpriseInvestmentItems": [
          |    {
          |      "uniqueInvestmentRef": "123412/1A",
          |      "companyName": "Company Inc",
          |      "dateOfInvestment": "2020-12-12",
          |      "amountInvested": -123123.22,
          |      "reliefClaimed": -3432.00
          |    }
          |  ],
          |  "socialEnterpriseInvestmentItems": [
          |    {
          |      "uniqueInvestmentRef": "123412/1A",
          |      "socialEnterpriseName": "SE Inc",
          |      "dateOfInvestment": "2020-12-12",
          |      "amountInvested": -123123.22,
          |      "reliefClaimed": -3432.00
          |    }
          |  ]
          |}
          |""".stripMargin
      )

      val allValueFormatError: MtdError = ValueFormatError.copy(
        message = "The field should be between 1 and 99999999999.99",
        paths = Some(Seq(
          "/vctSubscriptionsItems/0/amountInvested",
          "/vctSubscriptionsItems/0/reliefClaimed",
          "/vctSubscriptionsItems/1/amountInvested",
          "/vctSubscriptionsItems/1/reliefClaimed",
          "/eisSubscriptionsItems/0/amountInvested",
          "/eisSubscriptionsItems/0/reliefClaimed",
          "/communityInvestmentItems/0/amountInvested",
          "/communityInvestmentItems/0/reliefClaimed",
          "/seedEnterpriseInvestmentItems/0/amountInvested",
          "/seedEnterpriseInvestmentItems/0/reliefClaimed",
          "/socialEnterpriseInvestmentItems/0/amountInvested",
          "/socialEnterpriseInvestmentItems/0/reliefClaimed"
        ))
      )

      val allInvalidDateOfInvestmentrequestBodyJson: JsValue = Json.parse(
        """|
 |{
           |  "vctSubscriptionsItems":[
           |    {
           |      "uniqueInvestmentRef": "VCTREF",
           |      "name": "VCT Fund X",
           |      "dateOfInvestment": "18-04-16",
           |      "amountInvested": 23312.00,
           |      "reliefClaimed": 1334.00
           |    },
           |    {
           |      "uniqueInvestmentRef": "VCTREF",
           |      "name": "VCT Fund X",
           |      "dateOfInvestment": "18-04-16",
           |      "amountInvested": 23312.00,
           |      "reliefClaimed": 1334.00
           |    }
           |  ],
           |  "eisSubscriptionsItems":[
           |    {
           |      "uniqueInvestmentRef": "XTAL",
           |      "name": "EIS Fund X",
           |      "knowledgeIntensive": true,
           |      "dateOfInvestment": "20-12-12",
           |      "amountInvested": 23312.00,
           |      "reliefClaimed": 43432.00
           |    }
           |  ],
           |  "communityInvestmentItems": [
           |    {
           |      "uniqueInvestmentRef": "CIREF",
           |      "name": "CI X",
           |      "dateOfInvestment": "20-12-12",
           |      "amountInvested": 6442.00,
           |      "reliefClaimed": 2344.00
           |    }
           |  ],
           |  "seedEnterpriseInvestmentItems": [
           |    {
           |      "uniqueInvestmentRef": "123412/1A",
           |      "companyName": "Company Inc",
           |      "dateOfInvestment": "0-12-12",
           |      "amountInvested": 123123.22,
           |      "reliefClaimed": 3432.00
           |    }
           |  ],
           |  "socialEnterpriseInvestmentItems": [
           |    {
           |      "uniqueInvestmentRef": "123412/1A",
           |      "socialEnterpriseName": "SE Inc",
           |      "dateOfInvestment": "20-12-12",
           |      "amountInvested": 123123.22,
           |      "reliefClaimed": 3432.00
           |    }
           |  ]
           |}
           |""".stripMargin
      )

      val allDateOfInvestmentFormatError: MtdError = DateOfInvestmentFormatError.copy(
        message = "The format of the investment date is invalid",
        paths = Some(List(
          "/vctSubscriptionsItems/0/dateOfInvestment",
          "/vctSubscriptionsItems/1/dateOfInvestment",
          "/eisSubscriptionsItems/0/dateOfInvestment",
          "/communityInvestmentItems/0/dateOfInvestment",
          "/seedEnterpriseInvestmentItems/0/dateOfInvestment",
          "/socialEnterpriseInvestmentItems/0/dateOfInvestment"
        ))
      )
      val allInvalidNameFormatRequestBodyJson: JsValue = Json.parse(
        """|{
           |  "vctSubscriptionsItems":[
           |    {
           |      "uniqueInvestmentRef": "VCTREF",
           |      "name": "VCT Fund!!!! X",
           |      "dateOfInvestment": "2018-04-16",
           |      "amountInvested": 23312.00,
           |      "reliefClaimed": 1334.00
           |    },
           |    {
           |      "uniqueInvestmentRef": "VCTREF",
           |      "name": "VCT Fund X!!!!",
           |      "dateOfInvestment": "2018-04-16",
           |      "amountInvested": 23312.00,
           |      "reliefClaimed": 1334.00
           |    }
           |  ],
           |  "eisSubscriptionsItems":[
           |    {
           |      "uniqueInvestmentRef": "XTAL",
           |      "name": "EIS Fu!!!!nd X",
           |      "knowledgeIntensive": true,
           |      "dateOfInvestment": "2020-12-12",
           |      "amountInvested": 23312.00,
           |      "reliefClaimed": 43432.00
           |    }
           |  ],
           |  "communityInvestmentItems": [
           |    {
           |      "uniqueInvestmentRef": "CIREF",
           |      "name": "CI !!!X",
           |      "dateOfInvestment": "2020-12-12",
           |      "amountInvested": 6442.00,
           |      "reliefClaimed": 2344.00
           |    }
           |  ],
           |  "seedEnterpriseInvestmentItems": [
           |    {
           |      "uniqueInvestmentRef": "123412/1A",
           |      "companyName": "Company I!!!nc",
           |      "dateOfInvestment": "2020-12-12",
           |      "amountInvested": 123123.22,
           |      "reliefClaimed": 3432.00
           |    }
           |  ],
           |  "socialEnterpriseInvestmentItems": [
           |    {
           |      "uniqueInvestmentRef": "123412/1A",
           |      "socialEnterpriseName": "S!!!!E Inc",
           |      "dateOfInvestment": "2020-12-12",
           |      "amountInvested": 123123.22,
           |      "reliefClaimed": 3432.00
           |    }
           |  ]
           |}
           |""".stripMargin
      )

      val allNameFormatError: MtdError = NameFormatError.copy(
        message = "The format of the name is invalid",
        paths = Some(List(
          "/vctSubscriptionsItems/0/name",
          "/vctSubscriptionsItems/1/name",
          "/eisSubscriptionsItems/0/name",
          "/communityInvestmentItems/0/name",
          "/seedEnterpriseInvestmentItems/0/companyName",
          "/socialEnterpriseInvestmentItems/0/socialEnterpriseName"
        ))
      )
      val allInvalidInvestmentRefRequestBodyJson: JsValue = Json.parse(
        """|{
           |  "vctSubscriptionsItems":[
           |    {
           |      "uniqueInvestmentRef": "VC???TREF",
           |      "name": "VCT Fund X",
           |      "dateOfInvestment": "2018-04-16",
           |      "amountInvested": 23312.00,
           |      "reliefClaimed": 1334.00
           |    },
           |    {
           |      "uniqueInvestmentRef": "VC???TREF",
           |      "name": "VCT Fund X",
           |      "dateOfInvestment": "2018-04-16",
           |      "amountInvested": 23312.00,
           |      "reliefClaimed": 1334.00
           |    }
           |  ],
           |  "eisSubscriptionsItems":[
           |    {
           |      "uniqueInvestmentRef": "X????TAL",
           |      "name": "EIS Fund X",
           |      "knowledgeIntensive": true,
           |      "dateOfInvestment": "2020-12-12",
           |      "amountInvested": 23312.00,
           |      "reliefClaimed": 43432.00
           |    }
           |  ],
           |  "communityInvestmentItems": [
           |    {
           |      "uniqueInvestmentRef": "CIRE??F",
           |      "name": "CI X",
           |      "dateOfInvestment": "2020-12-12",
           |      "amountInvested": 6442.00,
           |      "reliefClaimed": 2344.00
           |    }
           |  ],
           |  "seedEnterpriseInvestmentItems": [
           |    {
           |      "uniqueInvestmentRef": "1234/??12/1A",
           |      "companyName": "Company Inc",
           |      "dateOfInvestment": "2020-12-12",
           |      "amountInvested": 123123.22,
           |      "reliefClaimed": 3432.00
           |    }
           |  ],
           |  "socialEnterpriseInvestmentItems": [
           |    {
           |      "uniqueInvestmentRef": "123412???/1A",
           |      "socialEnterpriseName": "SE Inc",
           |      "dateOfInvestment": "2020-12-12",
           |      "amountInvested": 123123.22,
           |      "reliefClaimed": 3432.00
           |    }
           |  ]
           |}
           |""".stripMargin
      )

      val allInvalidInvestmentRefRequestError: MtdError = InvestmentRefFormatError.copy(
        message = "The format of unique investment reference is invalid",
        paths = Some(List(
          "/vctSubscriptionsItems/0/uniqueInvestmentRef",
          "/vctSubscriptionsItems/1/uniqueInvestmentRef",
          "/eisSubscriptionsItems/0/uniqueInvestmentRef",
          "/communityInvestmentItems/0/uniqueInvestmentRef",
          "/seedEnterpriseInvestmentItems/0/uniqueInvestmentRef",
          "/socialEnterpriseInvestmentItems/0/uniqueInvestmentRef"
        ))
      )

      "validation error" when {
        def validationErrorTest(requestNino: String, requestTaxYear: String, requestBody: JsValue, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new Test {

            override val nino: String = requestNino
            override val taxYear: String = requestTaxYear
            override val requestBodyJson: JsValue = requestBody

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
            }

            val response: WSResponse = await(request().put(requestBodyJson))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = Seq(
          ("AA1123A", "2017-18", validRequestBodyJson, BAD_REQUEST, NinoFormatError),
          ("AA123456A", "20177", validRequestBodyJson,  BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "2017-18", allInvalidvalueFormatRequestBodyJson, BAD_REQUEST, allValueFormatError),
          ("AA123456A", "2017-18", allInvalidDateOfInvestmentrequestBodyJson, BAD_REQUEST, allDateOfInvestmentFormatError),
          ("AA123456A", "2017-18", allInvalidNameFormatRequestBodyJson, BAD_REQUEST, allNameFormatError),
          ("AA123456A", "2017-18", allInvalidInvestmentRefRequestBodyJson, BAD_REQUEST, allInvalidInvestmentRefRequestError))

        input.foreach(args => (validationErrorTest _).tupled(args))
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
          (BAD_REQUEST, "FORMAT_STATUS", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "FORMAT_TAX_YEAR", BAD_REQUEST, TaxYearFormatError),
          (BAD_REQUEST, "NOT_FOUND", NOT_FOUND, NotFoundError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, DownstreamError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, DownstreamError))

        input.foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }
}