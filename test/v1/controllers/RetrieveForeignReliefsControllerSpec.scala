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
import api.models.errors
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import v1.controllers.validators.MockRetrieveForeignReliefsValidatorFactory
import v1.models.request.retrieveForeignReliefs.RetrieveForeignReliefsRequestData
import v1.models.response.retrieveForeignReliefs._
import v1.services.MockRetrieveForeignReliefsService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveForeignReliefsControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockRetrieveForeignReliefsService
    with MockRetrieveForeignReliefsValidatorFactory
    with MockHateoasFactory {

  private val taxYear         = "2019-20"
  private val requestData     = RetrieveForeignReliefsRequestData(Nino(nino), TaxYear.fromMtd(taxYear))
  private val testHateoasLink = Link(href = s"individuals/reliefs/foreign/$nino/$taxYear", method = GET, rel = "self")

  private val responseBody = RetrieveForeignReliefsResponse(
    Timestamp("2020-06-17T10:53:38.000Z"),
    Some(ForeignTaxCreditRelief(2309.95)),
    Some(
      Seq(
        ForeignIncomeTaxCreditRelief(
          "FRA",
          Some(1640.32),
          1204.78,
          employmentLumpSum = false
        ))),
    Some(ForeignTaxForFtcrNotClaimed(1749.98))
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
          .wrap(responseBody, RetrieveForeignReliefsHateoasData(nino, taxYear))
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

    protected def callController(): Future[Result] = controller.handleRequest(nino, taxYear)(fakeGetRequest)
  }

}
