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

import cats.data.EitherT
import cats.implicits._
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import utils.{IdGenerator, Logging}
import v1.controllers.requestParsers.CreateAndAmendForeignReliefsRequestParser
import v1.hateoas.HateoasFactory
import v1.models.audit.{CreateAndAmendForeignReliefsAuditDetail, AuditEvent, AuditResponse}
import v1.models.errors._
import v1.models.request.createAndAmendForeignReliefs.CreateAndAmendForeignReliefsRawData
import v1.models.response.createAndAmendForeignReliefs.CreateAndAmendForeignReliefsHateoasData
import v1.models.response.createAndAmendForeignReliefs.CreateAndAmendForeignReliefsResponse.LinksFactory
import v1.services.{CreateAndAmendForeignReliefsService, AuditService, EnrolmentsAuthService, MtdIdLookupService}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateAndAmendForeignReliefsController @Inject() (val authService: EnrolmentsAuthService,
                                                        val lookupService: MtdIdLookupService,
                                                        parser: CreateAndAmendForeignReliefsRequestParser,
                                                        service: CreateAndAmendForeignReliefsService,
                                                        auditService: AuditService,
                                                        hateoasFactory: HateoasFactory,
                                                        cc: ControllerComponents,
                                                        val idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc)
    with BaseController
    with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "CreateAndAmendForeignReliefsController", endpointName = "createAndAmendForeignReliefs")

  def handleRequest(nino: String, taxYear: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val correlationId: String = idGenerator.getCorrelationId
      logger.info(message = s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
        s"with correlationId : $correlationId")
      val rawData = CreateAndAmendForeignReliefsRawData(nino, taxYear, request.body)
      val result =
        for {
          parsedRequest   <- EitherT.fromEither[Future](parser.parseRequest(rawData))
          serviceResponse <- EitherT(service.createAndAmend(parsedRequest))
        } yield {
          val vendorResponse = hateoasFactory.wrap(serviceResponse.responseData, CreateAndAmendForeignReliefsHateoasData(nino, taxYear))

          logger.info(
            s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
              s"Success response received with CorrelationId: ${serviceResponse.correlationId}")

          val response = Json.toJson(vendorResponse)

          auditSubmission(
            CreateAndAmendForeignReliefsAuditDetail(
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
          CreateAndAmendForeignReliefsAuditDetail(
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
      case _
          if errorWrapper.containsAnyOf(
            NinoFormatError,
            BadRequestError,
            TaxYearFormatError,
            RuleIncorrectOrEmptyBodyError,
            RuleTaxYearNotSupportedError,
            RuleTaxYearRangeInvalidError,
            ValueFormatError,
            CountryCodeFormatError,
            RuleCountryCodeError
          ) =>
        BadRequest(Json.toJson(errorWrapper))
      case InternalError => InternalServerError(Json.toJson(errorWrapper))
      case _             => unhandledError(errorWrapper)
    }
  }

  private def auditSubmission(details: CreateAndAmendForeignReliefsAuditDetail)(implicit hc: HeaderCarrier, ec: ExecutionContext) = {
    val event = AuditEvent("CreateAmendForeignReliefs", "create-amend-foreign-reliefs", details)
    auditService.auditEvent(event)
  }

}
