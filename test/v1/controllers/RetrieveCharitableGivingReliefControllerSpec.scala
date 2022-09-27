/*
 * Copyright 2022 HM Revenue & Customs
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

import play.api.libs.json.Json
import play.api.mvc.Result
import uk.gov.hmrc.http.HeaderCarrier
import v1.mocks.MockIdGenerator
import v1.mocks.hateoas.MockHateoasFactory
import v1.mocks.requestParsers.MockRetrieveCharitableGivingReliefRequestParser
import v1.mocks.services.{MockEnrolmentsAuthService, MockMtdIdLookupService, MockRetrieveCharitableGivingReliefService}
import v1.models.domain.Nino
import v1.models.errors._
import v1.models.hateoas.Method._
import v1.models.hateoas.{HateoasWrapper, Link}
import v1.models.outcomes.ResponseWrapper
import v1.models.request.TaxYear
import v1.models.request.retrieveCharitableGivingTaxRelief._
import v1.models.response.retrieveCharitableGivingTaxRelief._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveCharitableGivingReliefControllerSpec
  extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockRetrieveCharitableGivingReliefService
    with MockRetrieveCharitableGivingReliefRequestParser
    with MockHateoasFactory
    with MockIdGenerator
    with RetrieveCharitableGivingReliefFixture {

  private val nino          = "AA123456A"
  private val taxYear       = "2019-20"
  private val correlationId = "X-123"

  private val rawData     = RetrieveCharitableGivingReliefRawData(nino, taxYear)
  private val requestData = RetrieveCharitableGivingReliefRequest(Nino(nino), TaxYear.fromMtd(taxYear))

  private val hateoasLinks = Seq(
    Link(href = s"/individuals/reliefs/charitable-giving/$nino/$taxYear", method = PUT, rel = "create-and-amend-charitable-giving-tax-relief"),
    Link(href = s"/individuals/reliefs/charitable-giving/$nino/$taxYear", method = GET, rel = "self"),
    Link(href = s"/individuals/reliefs/charitable-giving/$nino/$taxYear", method = DELETE, rel = "delete-charitable-giving-tax-relief")
  )

  private val responseModel = charitableGivingReliefResponse
  private val responseJson  = charitableGivingReliefResponseMtdJsonWithHateoas(nino, taxYear)

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new RetrieveCharitableGivingReliefController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockRetrieveCharitableGivingReliefRequestParser,
      service = mockRetrieveCharitableGivingReliefService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.getCorrelationId.returns(correlationId)
  }

  "handleRequest" should {
    "return OK" when {
      "the request received is valid" in new Test {

        MockRetrieveCharitableGivingReliefRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockRetrieveCharitableGivingReliefService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseModel))))

        MockHateoasFactory
          .wrap(responseModel, RetrieveCharitableGivingReliefHateoasData(nino, taxYear))
          .returns(HateoasWrapper(responseModel, hateoasLinks))

        val result: Future[Result] = controller.handleRequest(nino, taxYear)(fakeRequest)

        status(result) shouldBe OK
        contentAsJson(result) shouldBe responseJson
        header("X-CorrelationId", result) shouldBe Some(correlationId)
      }
    }

    "return the error as per spec" when {
      "parser errors occur" should {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockRetrieveCharitableGivingReliefRequestParser
              .parse(rawData)
              .returns(Left(ErrorWrapper(correlationId, error, None)))

            val result: Future[Result] = controller.handleRequest(nino, taxYear)(fakeRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (BadRequestError, BAD_REQUEST),
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (RuleTaxYearNotSupportedError, BAD_REQUEST),
          (RuleTaxYearRangeInvalidError, BAD_REQUEST)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" should {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockRetrieveCharitableGivingReliefRequestParser
              .parse(rawData)
              .returns(Right(requestData))

            MockRetrieveCharitableGivingReliefService
              .retrieve(requestData)
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

            val result: Future[Result] = controller.handleRequest(nino, taxYear)(fakeRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (NotFoundError, NOT_FOUND),
          (InternalError, INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }

}
