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

package v1.retrieveForeignReliefs

import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import shared.config.MockSharedAppConfig
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.hateoas.Method._
import shared.hateoas.{HateoasWrapper, Link, MockHateoasFactory}
import shared.models.domain.{TaxYear, Timestamp}
import shared.models.errors
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import v1.retrieveForeignReliefs.def1.model.response._
import v1.retrieveForeignReliefs.model.request.Def1_RetrieveForeignReliefsRequestData
import v1.retrieveForeignReliefs.model.response.{Def1_RetrieveForeignReliefsResponse, RetrieveForeignReliefsHateoasData}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveForeignReliefsControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockRetrieveForeignReliefsService
    with MockRetrieveForeignReliefsValidatorFactory
    with MockHateoasFactory
    with MockSharedAppConfig {

  private val taxYear         = "2019-20"
  private val requestData     = Def1_RetrieveForeignReliefsRequestData(parsedNino, TaxYear.fromMtd(taxYear))
  private val testHateoasLink = Link(href = s"individuals/reliefs/foreign/$validNino/$taxYear", method = GET, rel = "self")

  private val responseBody = Def1_RetrieveForeignReliefsResponse(
    Timestamp("2020-06-17T10:53:38.000Z"),
    Some(Def1_ForeignTaxCreditRelief(2309.95)),
    Some(
      Seq(
        Def1_ForeignIncomeTaxCreditRelief(
          "FRA",
          Some(1640.32),
          1204.78,
          employmentLumpSum = false
        ))),
    Some(Def1_ForeignTaxForFtcrNotClaimed(1749.98))
  )

  val mtdResponseJson: JsValue = Json
    .parse(
      s"""
         |{
         |   "submittedOn":"2020-06-17T10:53:38.000Z",
         |   "foreignTaxCreditRelief":{
         |      "amount":2309.95
         |   },
         |   "foreignIncomeTaxCreditRelief":[
         |      {
         |         "countryCode":"FRA",
         |         "foreignTaxPaid":1640.32,
         |         "taxableAmount":1204.78,
         |         "employmentLumpSum":false
         |      }
         |   ],
         |   "foreignTaxForFtcrNotClaimed":{
         |      "amount":1749.98
         |   },
         |   "links":[
         |      {
         |         "href":"individuals/reliefs/foreign/AA123456A/2019-20",
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
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseBody))))

        MockHateoasFactory
          .wrap(responseBody, RetrieveForeignReliefsHateoasData(validNino, taxYear))
          .returns(HateoasWrapper(responseBody, Seq(testHateoasLink)))

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
          .returns(Future.successful(Left(errors.ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTest(RuleTaxYearNotSupportedError)
      }
    }
  }

  trait Test extends ControllerTest {

    val controller = new RetrieveForeignReliefsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockRetrieveForeignReliefsValidatorFactory,
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
