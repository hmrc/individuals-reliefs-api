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
import api.mocks.MockIdGenerator
import api.mocks.hateoas.MockHateoasFactory
import api.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.hateoas.HateoasWrapper
import api.models.hateoas.Method.{DELETE, GET, PUT}
import api.models.outcomes.ResponseWrapper
import api.models.{errors, hateoas}
import mocks.MockAppConfig
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import v1.mocks.requestParsers.MockCreateAndAmendCharitableGivingRequestParser
import v1.mocks.services._
import v1.models.request.createAndAmendCharitableGivingTaxRelief._
import v1.models.response.createAndAmendCharitableGivingTaxRelief.CreateAndAmendCharitableGivingTaxReliefHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateAndAmendCharitableGivingControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockCreateAndAmendCharitableGivingService
    with MockCreateAndAmendCharitableGivingRequestParser
    with MockHateoasFactory
    with MockAppConfig
    with MockIdGenerator {

  private val taxYear = "2019-20"
  private val amount  = 1234.56

  private val nonUkCharities =
    NonUkCharities(
      charityNames = Some(Seq("non-UK charity 1", "non-UK charity 2")),
      totalAmount = amount
    )

  private val giftAidPayments =
    GiftAidPayments(
      nonUkCharities = Some(nonUkCharities),
      totalAmount = Some(amount),
      oneOffAmount = Some(amount),
      amountTreatedAsPreviousTaxYear = Some(amount),
      amountTreatedAsSpecifiedTaxYear = Some(amount)
    )

  private val testHateoasLinks = Seq(
    hateoas.Link(href = s"/individuals/reliefs/charitable-giving/$nino/$taxYear", method = GET, rel = "self"),
    hateoas.Link(
      href = s"/individuals/reliefs/charitable-giving/$nino/$taxYear",
      method = PUT,
      rel = "create-and-amend-charitable-giving-tax-relief"),
    hateoas.Link(href = s"/individuals/reliefs/charitable-giving/$nino/$taxYear", method = DELETE, rel = "delete-charitable-giving-tax-relief")
  )

  private val requestJson = Json.parse(
    s"""|{
        |  "giftAidPayments": {
        |    "totalAmount": $amount
        |  }
        |}
        |""".stripMargin
  )

  private val requestBody = CreateAndAmendCharitableGivingTaxReliefBody(
    giftAidPayments = Some(giftAidPayments),
    gifts = None
  )

  val responseBody: JsValue = Json.parse(s"""
                                            |{
                                            |  "links": [
                                            |    {
                                            |      "href": "/individuals/reliefs/charitable-giving/$nino/$taxYear",
                                            |      "method": "GET",
                                            |      "rel": "self"
                                            |    },
                                            |    {
                                            |      "href": "/individuals/reliefs/charitable-giving/$nino/$taxYear",
                                            |      "method": "PUT",
                                            |      "rel": "create-and-amend-charitable-giving-tax-relief"
                                            |    },
                                            |    {
                                            |      "href": "/individuals/reliefs/charitable-giving/$nino/$taxYear",
                                            |      "method": "DELETE",
                                            |      "rel": "delete-charitable-giving-tax-relief"
                                            |    }
                                            |  ]
                                            |}
                                            |""".stripMargin)

  private val rawData     = CreateAndAmendCharitableGivingTaxReliefRawData(nino, taxYear, requestJson)
  private val requestData = CreateAndAmendCharitableGivingTaxReliefRequest(Nino(nino), TaxYear.fromMtd(taxYear), requestBody)

  "handleRequest" should {
    "return a successful response with status 200 (OK)" when {
      "the request received is valid" in new Test {

        MockCreateAndAmendCharitableGivingRequestParser
          .parseRequest(rawData)
          .returns(Right(requestData))

        MockAmendReliefService
          .amend(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        MockHateoasFactory
          .wrap((), CreateAndAmendCharitableGivingTaxReliefHateoasData(nino, taxYear))
          .returns(HateoasWrapper((), testHateoasLinks))

        runOkTestWithAudit(
          expectedStatus = OK,
          maybeAuditRequestBody = Some(requestJson),
          maybeExpectedResponseBody = Some(responseBody),
          maybeAuditResponseBody = Some(responseBody)
        )
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {

        MockCreateAndAmendCharitableGivingRequestParser
          .parseRequest(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError)))

        runErrorTestWithAudit(NinoFormatError, Some(requestJson))

      }

      "the service returns an error" in new Test {

        MockCreateAndAmendCharitableGivingRequestParser
          .parseRequest(rawData)
          .returns(Right(requestData))

        MockAmendReliefService
          .amend(requestData)
          .returns(Future.successful(Left(errors.ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTestWithAudit(RuleTaxYearNotSupportedError, maybeAuditRequestBody = Some(requestJson))
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking {

    val controller = new CreateAndAmendCharitableGivingController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockCreateAmendCharitableGivingRequestParser,
      service = mockService,
      auditService = mockAuditService,
      hateoasFactory = mockHateoasFactory,
      appConfig = mockAppConfig,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.handleRequest(nino, taxYear)(fakePostRequest(requestJson))

    def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "CreateAndAmendCharitableGivingTaxRelief",
        transactionName = "create-and-amend-charitable-giving-tax-relief",
        detail = GenericAuditDetail(
          userType = "Individual",
          agentReferenceNumber = None,
          pathParams = Map("nino" -> nino, "taxYear" -> taxYear),
          queryParams = None,
          requestBody = maybeRequestBody,
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )

  }

}
