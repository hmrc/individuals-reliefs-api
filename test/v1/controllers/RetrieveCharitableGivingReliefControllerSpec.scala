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
import api.models.hateoas
import api.models.hateoas.HateoasWrapper
import api.models.hateoas.Method.{DELETE, GET, PUT}
import api.models.outcomes.ResponseWrapper
import play.api.mvc.Result
import v1.mocks.requestParsers.MockRetrieveCharitableGivingReliefRequestParser
import v1.mocks.services.MockRetrieveCharitableGivingReliefService
import v1.models.request.retrieveCharitableGivingTaxRelief._
import v1.models.response.retrieveCharitableGivingTaxRelief._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveCharitableGivingReliefControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockRetrieveCharitableGivingReliefService
    with MockRetrieveCharitableGivingReliefRequestParser
    with MockHateoasFactory
    with RetrieveCharitableGivingReliefFixture {

  private val taxYear     = "2019-20"
  private val rawData     = RetrieveCharitableGivingReliefRawData(nino, taxYear)
  private val requestData = RetrieveCharitableGivingReliefRequest(Nino(nino), TaxYear.fromMtd(taxYear))

  private val hateoasLinks = Seq(
    hateoas.Link(
      href = s"/individuals/reliefs/charitable-giving/$nino/$taxYear",
      method = PUT,
      rel = "create-and-amend-charitable-giving-tax-relief"),
    hateoas.Link(href = s"/individuals/reliefs/charitable-giving/$nino/$taxYear", method = GET, rel = "self"),
    hateoas.Link(href = s"/individuals/reliefs/charitable-giving/$nino/$taxYear", method = DELETE, rel = "delete-charitable-giving-tax-relief")
  )

  private val responseModel = charitableGivingReliefResponse
  private val responseJson  = charitableGivingReliefResponseMtdJsonWithHateoas(nino, taxYear)

  "handleRequest" should {
    "return a successful response with status 200 (OK)" when {
      "given a valid request" in new Test {

        MockRetrieveCharitableGivingReliefRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockRetrieveCharitableGivingReliefService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseModel))))

        MockHateoasFactory
          .wrap(responseModel, RetrieveCharitableGivingReliefHateoasData(nino, taxYear))
          .returns(HateoasWrapper(responseModel, hateoasLinks))

        runOkTest(
          expectedStatus = OK,
          maybeExpectedResponseBody = Some(responseJson)
        )
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {

        MockRetrieveCharitableGivingReliefRequestParser
          .parse(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError)))

        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {

        MockRetrieveCharitableGivingReliefRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockRetrieveCharitableGivingReliefService
          .retrieve(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTest(RuleTaxYearNotSupportedError)
      }
    }
  }

  trait Test extends ControllerTest {

    val controller = new RetrieveCharitableGivingReliefController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockRetrieveCharitableGivingReliefRequestParser,
      service = mockRetrieveCharitableGivingReliefService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.handleRequest(nino, taxYear)(fakeGetRequest)
  }

}
