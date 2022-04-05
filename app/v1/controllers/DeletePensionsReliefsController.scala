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
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import utils.{IdGenerator, Logging}
import v1.controllers.requestParsers.DeletePensionsReliefsRequestParser
import v1.models.audit.{AuditEvent, AuditResponse, DeletePensionsReliefsAuditDetail}
import v1.models.errors._
import v1.models.request.deletePensionsReliefs.DeletePensionsReliefsRawData
import v1.services.{AuditService, DeletePensionsReliefsService, EnrolmentsAuthService, MtdIdLookupService}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeletePensionsReliefsController @Inject() (val authService: EnrolmentsAuthService,
                                                 val lookupService: MtdIdLookupService,
                                                 parser: DeletePensionsReliefsRequestParser,
                                                 service: DeletePensionsReliefsService,
                                                 auditService: AuditService,
                                                 cc: ControllerComponents,
                                                 val idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc)
    with BaseController
    with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "DeletePensionsReliefsController", endpointName = "deletePensionsReliefs")

  def handleRequest(nino: String, taxYear: String): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      implicit val correlationId: String = idGenerator.getCorrelationId
      logger.info(message = s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
        s"with correlationId : $correlationId")
      val rawData = DeletePensionsReliefsRawData(nino, taxYear)
      val result =
        for {
          parsedRequest   <- EitherT.fromEither[Future](parser.parseRequest(rawData))
          serviceResponse <- EitherT(service.delete(parsedRequest))
        } yield {
          logger.info(
            s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
              s"Success response received with CorrelationId: ${serviceResponse.correlationId}")

          auditSubmission(
            DeletePensionsReliefsAuditDetail(
              request.userDetails,
              nino,
              taxYear,
              serviceResponse.correlationId,
              AuditResponse(NO_CONTENT, Right(None))))

          NoContent
            .withApiHeaders(serviceResponse.correlationId)
        }
      result.leftMap { errorWrapper =>
        val resCorrelationId = errorWrapper.correlationId
        val result           = errorResult(errorWrapper).withApiHeaders(resCorrelationId)

        logger.warn(
          s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
            s"Error response received with CorrelationId: $resCorrelationId")

        auditSubmission(
          DeletePensionsReliefsAuditDetail(
            request.userDetails,
            nino,
            taxYear,
            correlationId,
            AuditResponse(result.header.status, Left(errorWrapper.auditErrors))))
        result
      }.merge
    }

  private def errorResult(errorWrapper: ErrorWrapper) = {
    errorWrapper.error match {
      case NinoFormatError | BadRequestError | TaxYearFormatError | RuleTaxYearRangeInvalidError | RuleTaxYearNotSupportedError =>
        BadRequest(Json.toJson(errorWrapper))
      case DownstreamError => InternalServerError(Json.toJson(errorWrapper))
      case NotFoundError   => NotFound(Json.toJson(errorWrapper))
      case _               => unhandledError(errorWrapper)
    }
  }

  private def auditSubmission(details: DeletePensionsReliefsAuditDetail)(implicit hc: HeaderCarrier, ec: ExecutionContext) = {
    val event = AuditEvent("DeleteReliefPension", "delete-reliefs-pensions", details)
    auditService.auditEvent(event)
  }

}
