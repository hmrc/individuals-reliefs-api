/*
 * Copyright 2023 HM Revenue & Customs
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

package v1.controllers

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.mocks.hateoas.MockHateoasFactory
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.hateoas.HateoasWrapper
import api.models.hateoas.Method.GET
import api.models.outcomes.ResponseWrapper
import api.models.{errors, hateoas}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import v1.fixtures.RetrieveReliefInvestmentsFixtures.responseModel
import v1.mocks.requestParsers.MockRetrieveInvestmentsRequestParser
import v1.mocks.services._
import v1.models.request.retrieveReliefInvestments.{RetrieveReliefInvestmentsRawData, RetrieveReliefInvestmentsRequest}
import v1.models.response.retrieveReliefInvestments._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveReliefInvestmentsControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockRetrieveReliefInvestmentsService
    with MockRetrieveInvestmentsRequestParser
    with MockHateoasFactory {

  private val taxYear         = "2019-20"
  private val rawData         = RetrieveReliefInvestmentsRawData(nino, taxYear)
  private val requestData     = RetrieveReliefInvestmentsRequest(Nino(nino), TaxYear.fromMtd(taxYear))
  private val testHateoasLink = hateoas.Link(href = s"individuals/reliefs/investment/$nino/$taxYear", method = GET, rel = "self")

  val mtdResponseJson: JsValue = Json
    .parse(
      s"""
         |{
         |   "submittedOn":"2020-06-17T10:53:38Z",
         |   "vctSubscription":[
         |      {
         |         "uniqueInvestmentRef":"VCTREF",
         |         "name":"VCT Fund X",
         |         "dateOfInvestment":"2018-04-16",
         |         "amountInvested":23312,
         |         "reliefClaimed":1334
         |      }
         |   ],
         |   "eisSubscription":[
         |      {
         |         "uniqueInvestmentRef":"XTAL",
         |         "name":"EIS Fund X",
         |         "knowledgeIntensive":true,
         |         "dateOfInvestment":"2020-12-12",
         |         "amountInvested":23312,
         |         "reliefClaimed":43432
         |      }
         |   ],
         |   "communityInvestment":[
         |      {
         |         "uniqueInvestmentRef":"VCTREF",
         |         "name":"VCT Fund X",
         |         "dateOfInvestment":"2018-04-16",
         |         "amountInvested":23312,
         |         "reliefClaimed":1334
         |      }
         |   ],
         |   "seedEnterpriseInvestment":[
         |      {
         |         "uniqueInvestmentRef":"123412/1A",
         |         "companyName":"Company Inc",
         |         "dateOfInvestment":"2020-12-12",
         |         "amountInvested":123123.22,
         |         "reliefClaimed":3432
         |      }
         |   ],
         |   "socialEnterpriseInvestment":[
         |      {
         |         "uniqueInvestmentRef":"123412/1A",
         |         "socialEnterpriseName":"SE Inc",
         |         "dateOfInvestment":"2020-12-12",
         |         "amountInvested":123123.22,
         |         "reliefClaimed":3432
         |      }
         |   ],
         |   "links":[
         |      {
         |         "href":"individuals/reliefs/investment/AA123456A/2019-20",
         |         "method":"GET",
         |         "rel":"self"
         |      }
         |   ]
         |}
        """.stripMargin
    )

  "handleRequest" should {
    "return Ok" when {
      "the request received is valid" in new Test {
        MockRetrieveReliefInvestmentsRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockRetrieveReliefService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseModel))))

        MockHateoasFactory
          .wrap(responseModel, RetrieveReliefInvestmentsHateoasData(nino, taxYear))
          .returns(HateoasWrapper(responseModel, Seq(testHateoasLink)))

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(mtdResponseJson))
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        MockRetrieveReliefInvestmentsRequestParser
          .parse(rawData)
          .returns(Left(errors.ErrorWrapper(correlationId, NinoFormatError)))

        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        MockRetrieveReliefInvestmentsRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockRetrieveReliefService
          .retrieve(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTest(RuleTaxYearNotSupportedError)
      }
    }
  }

  trait Test extends ControllerTest {

    val controller = new RetrieveReliefInvestmentsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockRequestDataParser,
      service = mockService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.handleRequest(nino, taxYear)(fakeGetRequest)
  }

}
