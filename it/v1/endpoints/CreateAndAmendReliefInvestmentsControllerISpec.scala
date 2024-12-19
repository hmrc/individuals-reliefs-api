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

package v1.endpoints

import common.{DateOfInvestmentFormatError, NameFormatError, UniqueInvestmentRefFormatError}
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import shared.models
import shared.models.errors._
import shared.services.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}
import shared.support.IntegrationBaseSpec
import v1.fixtures.CreateAndAmendReliefInvestmentsFixtures._

class CreateAndAmendReliefInvestmentsControllerISpec extends IntegrationBaseSpec {

  "Calling the amend endpoint" should {
    "return a 200 status code" when {
      "any valid request is made" in new NonTysTest {

        override def setupStubs(): Unit = {
          DownstreamStub.onSuccess(DownstreamStub.PUT, downstreamUri, NO_CONTENT, JsObject.empty)
        }

        val response: WSResponse = await(request().put(requestBodyJson))
        response.status shouldBe OK
        response.json shouldBe hateoasResponse(mtdTaxYear)
        response.header("X-CorrelationId") should not be empty
      }

      "any valid request is made for a Tax Year Specific (TYS) tax year" in new TysIfsTest {

        override def setupStubs(): Unit = {
          DownstreamStub.onSuccess(DownstreamStub.PUT, downstreamUri, NO_CONTENT, JsObject.empty)
        }

        val response: WSResponse = await(request().put(requestBodyJson))
        response.status shouldBe OK
        response.json shouldBe hateoasResponse(mtdTaxYear)
        response.header("X-CorrelationId") should not be empty
      }
    }

    "return a 400 with multiple errors" when {
      "all field value validations fail on the request body" in new NonTysTest {

        val allInvalidValueRequestBodyJson: JsValue = Json.parse(
          """
            |{
            |  "vctSubscription":[
            |    {
            |      "uniqueInvestmentRef": "VCT???REF",
            |      "name": "",
            |      "dateOfInvestment": "18-04-16",
            |      "amountInvested": -23312.00,
            |      "reliefClaimed": -1334.00
            |    },
            |    {
            |      "uniqueInvestmentRef": "VCT????REF",
            |      "name": "",
            |      "dateOfInvestment": "18-04-16",
            |      "amountInvested": 999999999993.00,
            |      "reliefClaimed": 999999999993.00
            |    }
            |  ],
            |  "eisSubscription":[
            |    {
            |      "uniqueInvestmentRef": "XT????AL",
            |      "name": "",
            |      "knowledgeIntensive": true,
            |      "dateOfInvestment": "20-12-12",
            |      "amountInvested": -23312.00,
            |      "reliefClaimed": -43432.00
            |    }
            |  ],
            |  "communityInvestment": [
            |    {
            |      "uniqueInvestmentRef": "CIRE/????F",
            |      "name": "",
            |      "dateOfInvestment": "20-12-12",
            |      "amountInvested": 6442.004,
            |      "reliefClaimed": 2344.0204
            |    }
            |  ],
            |  "seedEnterpriseInvestment": [
            |    {
            |      "uniqueInvestmentRef": "123412????/1A",
            |      "companyName": "",
            |      "dateOfInvestment": "20-12-12",
            |      "amountInvested": -123123.22,
            |      "reliefClaimed": -3432.00
            |    }
            |  ],
            |  "socialEnterpriseInvestment": [
            |    {
            |      "uniqueInvestmentRef": "12341????2/1A",
            |      "socialEnterpriseName": "",
            |      "dateOfInvestment": "20-12-12",
            |      "amountInvested": -123123.22,
            |      "reliefClaimed": -3432.00
            |    }
            |  ]
            |}
            |""".stripMargin
        )

        val allInvalidValueRequestError: List[MtdError] = List(
          DateOfInvestmentFormatError.copy(
            paths = Some(
              List(
                "/vctSubscription/0/dateOfInvestment",
                "/vctSubscription/1/dateOfInvestment",
                "/eisSubscription/0/dateOfInvestment",
                "/communityInvestment/0/dateOfInvestment",
                "/seedEnterpriseInvestment/0/dateOfInvestment",
                "/socialEnterpriseInvestment/0/dateOfInvestment"
              ))
          ),
          NameFormatError.copy(
            paths = Some(
              Seq(
                "/vctSubscription/0/name",
                "/vctSubscription/1/name",
                "/eisSubscription/0/name",
                "/communityInvestment/0/name",
                "/seedEnterpriseInvestment/0/companyName",
                "/socialEnterpriseInvestment/0/socialEnterpriseName"
              ))
          ),
          UniqueInvestmentRefFormatError.copy(
            paths = Some(
              List(
                "/vctSubscription/0/uniqueInvestmentRef",
                "/vctSubscription/1/uniqueInvestmentRef",
                "/eisSubscription/0/uniqueInvestmentRef",
                "/communityInvestment/0/uniqueInvestmentRef",
                "/seedEnterpriseInvestment/0/uniqueInvestmentRef",
                "/socialEnterpriseInvestment/0/uniqueInvestmentRef"
              ))
          ),
          ValueFormatError.copy(
            paths = Some(
              List(
                "/vctSubscription/0/amountInvested",
                "/vctSubscription/0/reliefClaimed",
                "/vctSubscription/1/amountInvested",
                "/vctSubscription/1/reliefClaimed",
                "/eisSubscription/0/amountInvested",
                "/eisSubscription/0/reliefClaimed",
                "/communityInvestment/0/amountInvested",
                "/communityInvestment/0/reliefClaimed",
                "/seedEnterpriseInvestment/0/amountInvested",
                "/seedEnterpriseInvestment/0/reliefClaimed",
                "/socialEnterpriseInvestment/0/amountInvested",
                "/socialEnterpriseInvestment/0/reliefClaimed"
              ))
          ),
        )

        val wrappedErrors: ErrorWrapper = ErrorWrapper(
          correlationId = correlationId,
          error = BadRequestError,
          errors = Some(allInvalidValueRequestError)
        )

        val response: WSResponse = await(request().put(allInvalidValueRequestBodyJson))
        response.status shouldBe BAD_REQUEST
        response.json shouldBe Json.toJson(wrappedErrors)
      }
    }

    "return error according to spec" when {

      val allInvalidValueFormatRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |  "vctSubscription":[
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
          |  "eisSubscription":[
          |    {
          |      "uniqueInvestmentRef": "XTAL",
          |      "name": "EIS Fund X",
          |      "knowledgeIntensive": true,
          |      "dateOfInvestment": "2020-12-12",
          |      "amountInvested": -23312.00,
          |      "reliefClaimed": -43432.00
          |    }
          |  ],
          |  "communityInvestment": [
          |    {
          |      "uniqueInvestmentRef": "CIREF",
          |      "name": "CI X",
          |      "dateOfInvestment": "2020-12-12",
          |      "amountInvested": -6442.00,
          |      "reliefClaimed": -2344.00
          |    }
          |  ],
          |  "seedEnterpriseInvestment": [
          |    {
          |      "uniqueInvestmentRef": "1234121A",
          |      "companyName": "Company Inc",
          |      "dateOfInvestment": "2020-12-12",
          |      "amountInvested": -123123.22,
          |      "reliefClaimed": -3432.00
          |    }
          |  ],
          |  "socialEnterpriseInvestment": [
          |    {
          |      "uniqueInvestmentRef": "1234121A",
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
        paths = Some(
          Seq(
            "/vctSubscription/0/amountInvested",
            "/vctSubscription/0/reliefClaimed",
            "/vctSubscription/1/amountInvested",
            "/vctSubscription/1/reliefClaimed",
            "/eisSubscription/0/amountInvested",
            "/eisSubscription/0/reliefClaimed",
            "/communityInvestment/0/amountInvested",
            "/communityInvestment/0/reliefClaimed",
            "/seedEnterpriseInvestment/0/amountInvested",
            "/seedEnterpriseInvestment/0/reliefClaimed",
            "/socialEnterpriseInvestment/0/amountInvested",
            "/socialEnterpriseInvestment/0/reliefClaimed"
          ))
      )

      val allInvalidDateOfInvestmentRequestBodyJson: JsValue = Json.parse(
        """|
           |{
           |  "vctSubscription":[
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
           |  "eisSubscription":[
           |    {
           |      "uniqueInvestmentRef": "XTAL",
           |      "name": "EIS Fund X",
           |      "knowledgeIntensive": true,
           |      "dateOfInvestment": "20-12-12",
           |      "amountInvested": 23312.00,
           |      "reliefClaimed": 43432.00
           |    }
           |  ],
           |  "communityInvestment": [
           |    {
           |      "uniqueInvestmentRef": "CIREF",
           |      "name": "CI X",
           |      "dateOfInvestment": "20-12-12",
           |      "amountInvested": 6442.00,
           |      "reliefClaimed": 2344.00
           |    }
           |  ],
           |  "seedEnterpriseInvestment": [
           |    {
           |      "uniqueInvestmentRef": "1234121A",
           |      "companyName": "Company Inc",
           |      "dateOfInvestment": "0-12-12",
           |      "amountInvested": 123123.22,
           |      "reliefClaimed": 3432.00
           |    }
           |  ],
           |  "socialEnterpriseInvestment": [
           |    {
           |      "uniqueInvestmentRef": "1234121A",
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
        paths = Some(
          List(
            "/vctSubscription/0/dateOfInvestment",
            "/vctSubscription/1/dateOfInvestment",
            "/eisSubscription/0/dateOfInvestment",
            "/communityInvestment/0/dateOfInvestment",
            "/seedEnterpriseInvestment/0/dateOfInvestment",
            "/socialEnterpriseInvestment/0/dateOfInvestment"
          ))
      )

      val allInvalidUniqueInvestmentReferenceRequestBodyJson: JsValue = Json.parse(
        """|{
           |  "vctSubscription":[
           |    {
           |      "uniqueInvestmentRef": "ABC/123",
           |      "name": "VCT Fund X",
           |      "dateOfInvestment": "2018-04-16",
           |      "amountInvested": 23312.00,
           |      "reliefClaimed": 1334.00
           |    },
           |    {
           |      "uniqueInvestmentRef": "ABC/123",
           |      "name": "VCT Fund X",
           |      "dateOfInvestment": "2018-04-16",
           |      "amountInvested": 23312.00,
           |      "reliefClaimed": 1334.00
           |    }
           |  ],
           |  "eisSubscription":[
           |    {
           |      "uniqueInvestmentRef": "ABC/123",
           |      "name": "EIS Fund X",
           |      "knowledgeIntensive": true,
           |      "dateOfInvestment": "2020-12-12",
           |      "amountInvested": 23312.00,
           |      "reliefClaimed": 43432.00
           |    }
           |  ],
           |  "communityInvestment": [
           |    {
           |      "uniqueInvestmentRef": "ABC/123",
           |      "name": "CI X",
           |      "dateOfInvestment": "2020-12-12",
           |      "amountInvested": 6442.00,
           |      "reliefClaimed": 2344.00
           |    }
           |  ],
           |  "seedEnterpriseInvestment": [
           |    {
           |      "uniqueInvestmentRef": "ABC/123",
           |      "companyName": "Company Inc",
           |      "dateOfInvestment": "2020-12-12",
           |      "amountInvested": 123123.22,
           |      "reliefClaimed": 3432.00
           |    }
           |  ],
           |  "socialEnterpriseInvestment": [
           |    {
           |      "uniqueInvestmentRef": "ABC/123",
           |      "socialEnterpriseName": "SE Inc",
           |      "dateOfInvestment": "2020-12-12",
           |      "amountInvested": 123123.22,
           |      "reliefClaimed": 3432.00
           |    }
           |  ]
           |}
           |""".stripMargin
      )

      val allUniqueInvestmentReferenceFormatError: MtdError = UniqueInvestmentRefFormatError.copy(
        paths = Some(
          List(
            "/vctSubscription/0/uniqueInvestmentRef",
            "/vctSubscription/1/uniqueInvestmentRef",
            "/eisSubscription/0/uniqueInvestmentRef",
            "/communityInvestment/0/uniqueInvestmentRef",
            "/seedEnterpriseInvestment/0/uniqueInvestmentRef",
            "/socialEnterpriseInvestment/0/uniqueInvestmentRef"
          ))
      )

      val allInvalidNameRequestBodyJson: JsValue = Json.parse(
        """|{
           |  "vctSubscription":[
           |    {
           |      "uniqueInvestmentRef": "VCTREF",
           |      "name": "",
           |      "dateOfInvestment": "2018-04-16",
           |      "amountInvested": 23312.00,
           |      "reliefClaimed": 1334.00
           |    },
           |    {
           |      "uniqueInvestmentRef": "VCTREF",
           |      "name": "",
           |      "dateOfInvestment": "2018-04-16",
           |      "amountInvested": 23312.00,
           |      "reliefClaimed": 1334.00
           |    }
           |  ],
           |  "eisSubscription":[
           |    {
           |      "uniqueInvestmentRef": "XTAL",
           |      "name": "",
           |      "knowledgeIntensive": true,
           |      "dateOfInvestment": "2020-12-12",
           |      "amountInvested": 23312.00,
           |      "reliefClaimed": 43432.00
           |    }
           |  ],
           |  "communityInvestment": [
           |    {
           |      "uniqueInvestmentRef": "CIREF",
           |      "name": "",
           |      "dateOfInvestment": "2020-12-12",
           |      "amountInvested": 6442.00,
           |      "reliefClaimed": 2344.00
           |    }
           |  ],
           |  "seedEnterpriseInvestment": [
           |    {
           |      "uniqueInvestmentRef": "1234121A",
           |      "companyName": "",
           |      "dateOfInvestment": "2020-12-12",
           |      "amountInvested": 123123.22,
           |      "reliefClaimed": 3432.00
           |    }
           |  ],
           |  "socialEnterpriseInvestment": [
           |    {
           |      "uniqueInvestmentRef": "1234121A",
           |      "socialEnterpriseName": "",
           |      "dateOfInvestment": "2020-12-12",
           |      "amountInvested": 123123.22,
           |      "reliefClaimed": 3432.00
           |    }
           |  ]
           |}
           |""".stripMargin
      )

      val allNameFormatError: MtdError = NameFormatError.copy(
        paths = Some(
          List(
            "/vctSubscription/0/name",
            "/vctSubscription/1/name",
            "/eisSubscription/0/name",
            "/communityInvestment/0/name",
            "/seedEnterpriseInvestment/0/companyName",
            "/socialEnterpriseInvestment/0/socialEnterpriseName"
          ))
      )

      "validation error" when {
        def validationErrorTest(requestNino: String,
                                requestTaxYear: String,
                                requestBody: JsValue,
                                expectedStatus: Int,
                                expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new NonTysTest {

            override val nino: String       = requestNino
            override val mtdTaxYear: String = requestTaxYear

            val response: WSResponse = await(request().put(requestBody))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = Seq(
          ("AA1123A", "2021-22", requestBodyJson, BAD_REQUEST, NinoFormatError),
          ("AA123456A", "20177", requestBodyJson, BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "2017-19", requestBodyJson, BAD_REQUEST, RuleTaxYearRangeInvalidError),
          ("AA123456A", "2019-20", requestBodyJson, BAD_REQUEST, RuleTaxYearNotSupportedError),
          ("AA123456A", "2021-22", allInvalidValueFormatRequestBodyJson, BAD_REQUEST, allValueFormatError),
          ("AA123456A", "2021-22", allInvalidDateOfInvestmentRequestBodyJson, BAD_REQUEST, allDateOfInvestmentFormatError),
          ("AA123456A", "2021-22", allInvalidUniqueInvestmentReferenceRequestBodyJson, BAD_REQUEST, allUniqueInvestmentReferenceFormatError),
          ("AA123456A", "2021-22", allInvalidNameRequestBodyJson, BAD_REQUEST, allNameFormatError)
        )
        input.foreach(args => (validationErrorTest _).tupled(args))
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
          (BAD_REQUEST, "INVALID_PAYLOAD", INTERNAL_SERVER_ERROR, models.errors.InternalError),
          (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, models.errors.InternalError),
          (UNPROCESSABLE_ENTITY, "UNPROCESSABLE_ENTITY", INTERNAL_SERVER_ERROR, models.errors.InternalError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, models.errors.InternalError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, models.errors.InternalError)
        )

        val extraTysErrors = List(
          (BAD_REQUEST, "INVALID_CORRELATION_ID", INTERNAL_SERVER_ERROR, models.errors.InternalError),
          (UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_SUPPORTED", BAD_REQUEST, RuleTaxYearNotSupportedError)
        )

        (errors ++ extraTysErrors).foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }

  private trait Test {

    val nino: String          = "AA123456A"
    val correlationId: String = "X-123"

    def mtdUri: String = s"/investment/$nino/$mtdTaxYear"

    def mtdTaxYear: String
    def downstreamUri: String

    def setupStubs(): Unit = {}

    def request(): WSRequest = {
      AuditStub.audit()
      AuthStub.authorised()
      MtdIdLookupStub.ninoFound(nino)
      setupStubs()

      buildRequest(mtdUri)
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
    def mtdTaxYear: String    = "2021-22"
    def downstreamUri: String = s"/income-tax/reliefs/investment/$nino/2021-22"
  }

  private trait TysIfsTest extends Test {
    def mtdTaxYear: String    = "2023-24"
    def downstreamUri: String = s"/income-tax/reliefs/investment/23-24/$nino"
  }

}
