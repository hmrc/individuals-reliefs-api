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

package v2.retrieveCharitableGivingReliefs

import play.api.Configuration
import play.api.mvc.Result
import shared.config.MockSharedAppConfig
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.hateoas.Method._
import shared.hateoas.{HateoasWrapper, Link, MockHateoasFactory}
import shared.models.domain.TaxYear
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import v2.retrieveCharitableGivingReliefs.def1.model.request.RetrieveCharitableGivingReliefsFixture
import v2.retrieveCharitableGivingReliefs.model.request.Def1_RetrieveCharitableGivingReliefsRequestData
import v2.retrieveCharitableGivingReliefs.model.response.RetrieveCharitableGivingReliefsHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveCharitableGivingReliefsControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockRetrieveCharitableGivingReliefsService
    with MockRetrieveCharitableGivingReliefsValidatorFactory
    with MockHateoasFactory
    with RetrieveCharitableGivingReliefsFixture
    with MockSharedAppConfig {

  private val taxYear     = "2019-20"
  private val requestData = Def1_RetrieveCharitableGivingReliefsRequestData(parsedNino, TaxYear.fromMtd(taxYear))

  private val hateoasLinks = Seq(
    Link(href = s"/individuals/reliefs/charitable-giving/$validNino/$taxYear", method = PUT, rel = "create-and-amend-charitable-giving-tax-relief"),
    Link(href = s"/individuals/reliefs/charitable-giving/$validNino/$taxYear", method = GET, rel = "self"),
    Link(href = s"/individuals/reliefs/charitable-giving/$validNino/$taxYear", method = DELETE, rel = "delete-charitable-giving-tax-relief")
  )

  private val responseModel = charitableGivingReliefsResponse
  private val responseJson  = charitableGivingReliefsResponseMtdJsonWithHateoas(validNino, taxYear)

  "handleRequest" should {
    "return a successful response with status 200 (OK)" when {
      "given a valid request" in new Test {

        willUseValidator(returningSuccess(requestData))

        MockRetrieveCharitableGivingReliefsService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseModel))))

        MockHateoasFactory
          .wrap(responseModel, RetrieveCharitableGivingReliefsHateoasData(validNino, taxYear))
          .returns(HateoasWrapper(responseModel, hateoasLinks))

        runOkTest(
          expectedStatus = OK,
          maybeExpectedResponseBody = Some(responseJson)
        )
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))
        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {

        willUseValidator(returningSuccess(requestData))

        MockRetrieveCharitableGivingReliefsService
          .retrieve(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTest(RuleTaxYearNotSupportedError)
      }
    }
  }

  trait Test extends ControllerTest {

    val controller = new RetrieveCharitableGivingReliefsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockRetrieveCharitableGivingReliefValidatorFactory,
      service = mockRetrieveCharitableGivingReliefsService,
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
