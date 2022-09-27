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

import cats.data.EitherT
import cats.implicits._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, ControllerComponents}
import utils.{IdGenerator, Logging}
import v1.controllers.requestParsers.AmendCharitableGivingRequestReliefParser
import v1.hateoas.HateoasFactory
import v1.models.errors._
import v1.models.request.createAndAmendCharitableGivingTaxRelief.CreateAndAmendCharitableGivingTaxReliefRawData
import v1.models.response.createAndAmendCharitableGivingTaxRelief.CreateAndAmendCharitableGivingTaxReliefHateoasData
import v1.models.response.createAndAmendCharitableGivingTaxRelief.CreateAndAmendCharitableGivingTaxReliefResponse.LinksFactory
import v1.services.{AuditService, CreateAndAmendCharitableGivingTaxReliefService, EnrolmentsAuthService, MtdIdLookupService}
import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import v1.models.audit.{AuditEvent, AuditResponse, CharitableGivingReliefAuditDetail}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateAndAmendCharitableGivingController @Inject() (val authService: EnrolmentsAuthService,
                                                          val lookupService: MtdIdLookupService,
                                                          parser: AmendCharitableGivingRequestReliefParser,
                                                          service: CreateAndAmendCharitableGivingTaxReliefService,
                                                          auditService: AuditService,
                                                          hateoasFactory: HateoasFactory,
                                                          cc: ControllerComponents,
                                                          val idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc)
    with BaseController
    with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "CreateAndAmendCharitableGivingController", endpointName = "createAndAmendCharitableGivingReliefs")

  def handleRequest(nino: String, taxYear: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val correlationId: String = idGenerator.getCorrelationId
      logger.info(message = s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
        s"with correlationId : $correlationId")
      val rawData = CreateAndAmendCharitableGivingTaxReliefRawData(nino, taxYear, request.body)
      val result =
        for {
          parsedRequest   <- EitherT.fromEither[Future](parser.parseRequest(rawData))
          serviceResponse <- EitherT(service.amend(parsedRequest))
          vendorResponse <- EitherT.fromEither[Future](
            hateoasFactory
              .wrap(serviceResponse.responseData, CreateAndAmendCharitableGivingTaxReliefHateoasData(nino, taxYear))
              .asRight[ErrorWrapper])
        } yield {
          logger.info(
            s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
              s"Success response received with CorrelationId: ${serviceResponse.correlationId}")

          val response = Json.toJson(vendorResponse)

          auditSubmission(
            CharitableGivingReliefAuditDetail(
              userDetails = request.userDetails,
              nino = nino,
              taxYear = taxYear,
              requestBody = Some(request.body),
              `X-CorrelationId` = serviceResponse.correlationId,
              auditResponse = AuditResponse(httpStatus = OK, response = Right(Some(response)))
            )
          )

          Ok(response)
            .withApiHeaders(serviceResponse.correlationId)
        }

      result.leftMap { errorWrapper =>
        val resCorrelationId = errorWrapper.correlationId
        val result           = errorResult(errorWrapper).withApiHeaders(resCorrelationId)

        logger.warn(
          s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
            s"Error response received with CorrelationId: $resCorrelationId")

        auditSubmission(
          CharitableGivingReliefAuditDetail(
            userDetails = request.userDetails,
            nino = nino,
            taxYear = taxYear,
            requestBody = Some(request.body),
            `X-CorrelationId` = resCorrelationId,
            auditResponse = AuditResponse(httpStatus = result.header.status, response = Left(errorWrapper.auditErrors))
          ))

        result
      }.merge
    }

  private def errorResult(errorWrapper: ErrorWrapper) = {

    errorWrapper.error match {
      case NinoFormatError | BadRequestError | TaxYearFormatError | RuleTaxYearNotSupportedError | RuleTaxYearRangeInvalidError |
          RuleGiftAidNonUkAmountWithoutNamesError | RuleGiftsNonUkAmountWithoutNamesError | MtdErrorWithCustomMessage(StringFormatError.code) |
          MtdErrorWithCustomMessage(ValueFormatError.code) | MtdErrorWithCustomMessage(RuleIncorrectOrEmptyBodyError.code) =>
        BadRequest(Json.toJson(errorWrapper))
      case NotFoundError   => NotFound(Json.toJson(errorWrapper))
      case InternalError => InternalServerError(Json.toJson(errorWrapper))
      case _               => unhandledError(errorWrapper)
    }
  }

  private def auditSubmission(details: CharitableGivingReliefAuditDetail)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AuditResult] = {
    val event = AuditEvent(
      auditType = "CreateAndAmendCharitableGivingTaxRelief",
      transactionName = "create-and-amend-charitable-giving-tax-relief",
      detail = details
    )

    auditService.auditEvent(event)
  }

}
