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
import utils.Logging
import v1.controllers.requestParsers.AmendReliefInvestmentsRequestParser
import v1.hateoas.HateoasFactory
import v1.models.errors._
import v1.models.request.amendReliefInvestments.AmendReliefInvestmentsRawData
import v1.models.response.amendReliefInvestments.AmendReliefInvestmentsHateoasData
import v1.models.response.amendReliefInvestments.AmendReliefInvestmentsResponse.AmendOrderLinksFactory
import v1.services.{AmendReliefInvestmentsService, EnrolmentsAuthService, MtdIdLookupService}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AmendReliefInvestmentsController @Inject()(val authService: EnrolmentsAuthService,
                                                 val lookupService: MtdIdLookupService,
                                                 amendReliefInvestmentsParser: AmendReliefInvestmentsRequestParser,
                                                 amendReliefInvestmentsService: AmendReliefInvestmentsService,
                                                 hateoasFactory: HateoasFactory,
                                                 cc: ControllerComponents)(implicit ec: ExecutionContext)
  extends AuthorisedController(cc) with BaseController with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "AmendReliefInvestmentsController", endpointName = "amendReliefInvestments")

  def handleRequest(nino: String, taxYear: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      val rawData = AmendReliefInvestmentsRawData(nino, taxYear, request.body)
      val result =
        for {
          parsedRequest <- EitherT.fromEither[Future](amendReliefInvestmentsParser.parseRequest(rawData))
          serviceResponse <- EitherT(amendReliefInvestmentsService.doServiceThing(parsedRequest))
          vendorResponse <- EitherT.fromEither[Future](
            hateoasFactory.wrap(serviceResponse.responseData, AmendReliefInvestmentsHateoasData(nino, taxYear)).asRight[ErrorWrapper])
        } yield {
          logger.info(
            s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
              s"Success response received with CorrelationId: ${serviceResponse.correlationId}")

          Ok(Json.toJson(vendorResponse))
            .withApiHeaders(serviceResponse.correlationId)
        }

      result.leftMap { errorWrapper =>
        val correlationId = getCorrelationId(errorWrapper)
        errorResult(errorWrapper).withApiHeaders(correlationId)
      }.merge
    }

  private def errorResult(errorWrapper: ErrorWrapper) = {

    (errorWrapper.error: @unchecked) match {
      case NinoFormatError |
           BadRequestError |
           TaxYearFormatError |
           RuleIncorrectOrEmptyBodyError |
           MtdErrorWithCustomMessage(ValueFormatError.code) |
           MtdErrorWithCustomMessage(DateOfInvestmentFormatError.code) |
           MtdErrorWithCustomMessage(NameFormatError.code) |
           MtdErrorWithCustomMessage(InvestmentRefFormatError.code) =>
        BadRequest(Json.toJson(errorWrapper))
      case DownstreamError => InternalServerError(Json.toJson(errorWrapper))
      case NotFoundError => NotFound(Json.toJson(errorWrapper))
      case UnauthorisedError => Unauthorized(Json.toJson(errorWrapper))
    }
  }


}