/*
 * Copyright 2020 HM Revenue & Customs
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
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import utils.{IdGenerator, Logging}
import v1.controllers.requestParsers.AmendReliefInvestmentsRequestParser
import v1.hateoas.HateoasFactory
import v1.models.audit.{AmendReliefInvestmentsAuditDetail, AuditEvent, AuditResponse}
import v1.models.errors._
import v1.models.request.amendReliefInvestments.AmendReliefInvestmentsRawData
import v1.models.response.amendReliefInvestments.AmendReliefInvestmentsHateoasData
import v1.models.response.amendReliefInvestments.AmendReliefInvestmentsResponse.LinksFactory
import v1.services.{AmendReliefInvestmentsService, AuditService, EnrolmentsAuthService, MtdIdLookupService}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AmendReliefInvestmentsController @Inject()(val authService: EnrolmentsAuthService,
                                                 val lookupService: MtdIdLookupService,
                                                 parser: AmendReliefInvestmentsRequestParser,
                                                 service: AmendReliefInvestmentsService,
                                                 auditService: AuditService,
                                                 hateoasFactory: HateoasFactory,
                                                 cc: ControllerComponents,
                                                 val idGenerator: IdGenerator)(implicit ec: ExecutionContext)
  extends AuthorisedController(cc) with BaseController with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "AmendReliefInvestmentsController", endpointName = "amendReliefInvestments")

  def handleRequest(nino: String, taxYear: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val correlationId: String = idGenerator.getCorrelationId
      logger.info(message = s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
        s"with correlationId : $correlationId")
      val rawData = AmendReliefInvestmentsRawData(nino, taxYear, request.body)
      val result =
        for {
          parsedRequest <- EitherT.fromEither[Future](parser.parseRequest(rawData))
          serviceResponse <- EitherT(service.amend(parsedRequest))
          vendorResponse <- EitherT.fromEither[Future](
            hateoasFactory.wrap(serviceResponse.responseData, AmendReliefInvestmentsHateoasData(nino, taxYear)).asRight[ErrorWrapper])
        } yield {
          logger.info(
            s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
              s"Success response received with CorrelationId: ${serviceResponse.correlationId}")

          val response = Json.toJson(vendorResponse)

          auditSubmission(AmendReliefInvestmentsAuditDetail(request.userDetails, nino, taxYear, request.body,
            serviceResponse.correlationId, AuditResponse(OK, Right(Some(response)))))

          Ok(Json.toJson(vendorResponse))
            .withApiHeaders(serviceResponse.correlationId)
        }

      result.leftMap { errorWrapper =>
        val resCorrelationId = errorWrapper.correlationId
        val result = errorResult(errorWrapper).withApiHeaders(resCorrelationId)

        logger.warn(
          s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
            s"Error response received with CorrelationId: $resCorrelationId")

        auditSubmission(AmendReliefInvestmentsAuditDetail(request.userDetails, nino, taxYear, request.body,
          correlationId, AuditResponse(result.header.status, Left(errorWrapper.auditErrors))))

        result
      }.merge
    }

  private def errorResult(errorWrapper: ErrorWrapper) = {

    (errorWrapper.error: @unchecked) match {
      case NinoFormatError |
           BadRequestError |
           TaxYearFormatError |
           RuleIncorrectOrEmptyBodyError |
           RuleTaxYearNotSupportedError |
           RuleTaxYearRangeInvalidError |
           MtdErrorWithCustomMessage(ValueFormatError.code) |
           MtdErrorWithCustomMessage(DateOfInvestmentFormatError.code) |
           MtdErrorWithCustomMessage(NameFormatError.code) |
           MtdErrorWithCustomMessage(UniqueInvestmentRefFormatError.code) =>
        BadRequest(Json.toJson(errorWrapper))
      case DownstreamError                                                => InternalServerError(Json.toJson(errorWrapper))
    }
  }

  private def auditSubmission(details: AmendReliefInvestmentsAuditDetail)
                             (implicit hc: HeaderCarrier,
                              ec: ExecutionContext) = {
    val event = AuditEvent("CreateAmendReliefsInvestment", "create-amend-reliefs-investment", details)
    auditService.auditEvent(event)
  }


}