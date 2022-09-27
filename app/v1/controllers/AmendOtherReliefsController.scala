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
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import utils.{IdGenerator, Logging}
import v1.controllers.requestParsers.AmendOtherReliefsRequestParser
import v1.hateoas.HateoasFactory
import v1.models.audit.{AmendOtherReliefsAuditDetail, AuditEvent, AuditResponse}
import v1.models.errors.{ErrorWrapper, _}
import v1.models.request.amendOtherReliefs.AmendOtherReliefsRawData
import v1.models.response.amendOtherReliefs.AmendOtherReliefsHateoasData
import v1.models.response.amendOtherReliefs.AmendOtherReliefsResponse.LinksFactory
import v1.services.{AmendOtherReliefsService, AuditService, EnrolmentsAuthService, MtdIdLookupService}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AmendOtherReliefsController @Inject() (val authService: EnrolmentsAuthService,
                                             val lookupService: MtdIdLookupService,
                                             parser: AmendOtherReliefsRequestParser,
                                             service: AmendOtherReliefsService,
                                             auditService: AuditService,
                                             hateoasFactory: HateoasFactory,
                                             cc: ControllerComponents,
                                             val idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc)
    with BaseController
    with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "AmendOtherReliefsController", endpointName = "amendOtherReliefs")

  def handleRequest(nino: String, taxYear: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val correlationId: String = idGenerator.getCorrelationId
      logger.info(message = s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
        s"with correlationId : $correlationId")

      val rawData = AmendOtherReliefsRawData(nino, taxYear, request.body)
      val result =
        for {
          parsedRequest   <- EitherT.fromEither[Future](parser.parseRequest(rawData))
          serviceResponse <- EitherT(service.amend(parsedRequest))
          vendorResponse <- EitherT.fromEither[Future](
            hateoasFactory.wrap(serviceResponse.responseData, AmendOtherReliefsHateoasData(nino, taxYear)).asRight[ErrorWrapper]
          )
        } yield {
          logger.info(
            s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
              s"Sucess response recieved with CorrelationId: ${serviceResponse.correlationId}")

          val response = Json.toJson(vendorResponse)

          auditSubmission(
            AmendOtherReliefsAuditDetail(
              request.userDetails,
              nino,
              taxYear,
              request.body,
              serviceResponse.correlationId,
              AuditResponse(OK, Right(Some(response)))))

          Ok(Json.toJson(vendorResponse))
            .withApiHeaders(serviceResponse.correlationId)
        }

      result.leftMap { errorWrapper =>
        val resCorrelationId = errorWrapper.correlationId
        val result           = errorResult(errorWrapper).withApiHeaders(resCorrelationId)

        logger.warn(
          s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
            s"Error response received with CorrelationId: $resCorrelationId")

        auditSubmission(
          AmendOtherReliefsAuditDetail(
            request.userDetails,
            nino,
            taxYear,
            request.body,
            correlationId,
            AuditResponse(result.header.status, Left(errorWrapper.auditErrors))))

        result
      }.merge
    }

  private def errorResult(errorWrapper: ErrorWrapper) = {
    errorWrapper.error match {
      case NinoFormatError | BadRequestError | TaxYearFormatError | RuleIncorrectOrEmptyBodyError | RuleTaxYearRangeInvalidError |
          RuleTaxYearNotSupportedError | MtdErrorWithCustomMessage(ValueFormatError.code) | MtdErrorWithCustomMessage(DateFormatError.code) |
          MtdErrorWithCustomMessage(CustomerReferenceFormatError.code) | MtdErrorWithCustomMessage(ExSpouseNameFormatError.code) |
          MtdErrorWithCustomMessage(BusinessNameFormatError.code) | MtdErrorWithCustomMessage(NatureOfTradeFormatError.code) |
          MtdErrorWithCustomMessage(IncomeSourceFormatError.code) | MtdErrorWithCustomMessage(LenderNameFormatError.code) =>
        BadRequest(Json.toJson(errorWrapper: ErrorWrapper))
      case RuleSubmissionFailedError => Forbidden(Json.toJson(errorWrapper))
      case InternalError           => InternalServerError(Json.toJson(errorWrapper))
      case _                         => unhandledError(errorWrapper)
    }
  }

  private def auditSubmission(details: AmendOtherReliefsAuditDetail)(implicit hc: HeaderCarrier, ec: ExecutionContext) = {
    val event = AuditEvent("CreateAmendOtherReliefs", "create-amend-other-reliefs", details)
    auditService.auditEvent(event)
  }

}
