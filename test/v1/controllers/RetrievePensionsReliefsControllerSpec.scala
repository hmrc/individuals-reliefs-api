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
import api.hateoas.Method._
import api.hateoas.{HateoasWrapper, Link, MockHateoasFactory}
import api.models.domain.{Nino, TaxYear, Timestamp}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import v1.controllers.validators.MockRetrievePensionsReliefsValidatorFactory
import v1.mocks.services._
import v1.models.request.retrievePensionsReliefs.RetrievePensionsReliefsRequestData
import v1.models.response.retrievePensionsReliefs._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrievePensionsReliefsControllerSpec
  extends ControllerBaseSpec
    with ControllerTestRunner
    with MockRetrievePensionsReliefsService
    with MockRetrievePensionsReliefsValidatorFactory
    with MockHateoasFactory {

  private val taxYear = "2019-20"
  private val requestData = RetrievePensionsReliefsRequestData(Nino(nino), TaxYear.fromMtd(taxYear))

  private val testHateoasLink = Link(href = s"individuals/reliefs/pensions/$nino/$taxYear", method = GET, rel = "self")

  private val responseBody = RetrievePensionsReliefsResponse(
    Timestamp("2019-04-04T01:01:01.000Z"),
    PensionsReliefs(
      Some(1999.99),
      Some(1999.99),
      Some(1999.99),
      Some(1999.99),
      Some(1999.99)
    )
  )

  val mtdResponseJson: JsValue = Json
    .parse(
      s"""
         |{
         |   "submittedOn":"2019-04-04T01:01:01.000Z",
         |   "pensionReliefs":{
         |      "regularPensionContributions":1999.99,
         |      "oneOffPensionContributionsPaid":1999.99,
         |      "retirementAnnuityPayments":1999.99,
         |      "paymentToEmployersSchemeNoTaxRelief":1999.99,
         |      "overseasPensionSchemeContributions":1999.99
         |   },
         |   "links":[
         |      {
         |         "href":"individuals/reliefs/pensions/AA123456A/2019-20",
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

        MockRetrievePensionsReliefsService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseBody))))

        MockHateoasFactory
          .wrap(responseBody, RetrievePensionsReliefsHateoasData(nino, taxYear))
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

        MockRetrievePensionsReliefsService
          .retrieve(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTest(RuleTaxYearNotSupportedError)
      }
    }
  }

  trait Test extends ControllerTest {

    val controller = new RetrievePensionsReliefsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockValidatorFactory,
      service = mockService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.handleRequest(nino, taxYear)(fakeGetRequest)
  }

}
