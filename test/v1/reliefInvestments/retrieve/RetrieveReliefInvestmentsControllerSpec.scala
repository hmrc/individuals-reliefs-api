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

package v1.reliefInvestments.retrieve

import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import shared.config.MockSharedAppConfig
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.hateoas.Method._
import shared.hateoas.{HateoasWrapper, Link, MockHateoasFactory}
import shared.models.domain.TaxYear
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import v1.fixtures.RetrieveReliefInvestmentsFixtures.responseModel
import v1.reliefInvestments.retrieve.def1.model.request.Def1_RetrieveReliefInvestmentsRequestData
import v1.reliefInvestments.retrieve.model.response.RetrieveReliefInvestmentsHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveReliefInvestmentsControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockRetrieveReliefInvestmentsService
    with MockRetrieveReliefInvestmentsValidatorFactory
    with MockHateoasFactory
    with MockSharedAppConfig {

  private val taxYear         = "2019-20"
  private val requestData     = Def1_RetrieveReliefInvestmentsRequestData(parsedNino, TaxYear.fromMtd(taxYear))
  private val testHateoasLink = Link(href = s"individuals/reliefs/investment/$validNino/$taxYear", method = GET, rel = "self")

  val mtdResponseJson: JsValue = Json
    .parse(
      s"""
         |{
         |   "submittedOn":"2020-06-17T10:53:38.000Z",
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
        willUseValidator(returningSuccess(requestData))

        MockRetrieveReliefService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseModel))))

        MockHateoasFactory
          .wrap(responseModel, RetrieveReliefInvestmentsHateoasData(validNino, taxYear))
          .returns(HateoasWrapper(responseModel, Seq(testHateoasLink)))

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(mtdResponseJson))
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))
        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

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
      validatorFactory = mockRetrieveReliefInvestmentsValidatorFactory,
      service = mockService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedSharedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = controller.handleRequest(validNino, taxYear)(fakeGetRequest)
  }

}
