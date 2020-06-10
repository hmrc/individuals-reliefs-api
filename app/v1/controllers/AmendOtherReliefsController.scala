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
import v1.controllers.requestParsers.AmendOtherReliefsRequestParser
import v1.hateoas.HateoasFactory
import v1.models.errors._
import v1.models.request.amendOtherReliefs.AmendOtherReliefsRawData
import v1.models.response.amendOtherReliefs.AmendOtherReliefsHateoasData
import v1.models.response.amendOtherReliefs.AmendOtherReliefsResponse.AmendOtherLinksFactory
import v1.services.{AmendOtherReliefsService, EnrolmentsAuthService, MtdIdLookupService}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AmendOtherReliefsController @Inject()(val authService: EnrolmentsAuthService,
                                            val lookupService: MtdIdLookupService,
                                            parser: AmendOtherReliefsRequestParser,
                                            service: AmendOtherReliefsService,
                                            hateoasFactory: HateoasFactory,
                                            cc: ControllerComponents)(implicit ec: ExecutionContext)
  extends AuthorisedController(cc) with BaseController with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "AmendOtherReliefsController", endpointName = "amendOtherReliefs")

  def handleRequest(nino: String, taxYear: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      val rawData = AmendOtherReliefsRawData(nino, taxYear, request.body)
      val result =
        for {
          parsedRequest <- EitherT.fromEither[Future](parser.parseRequest(rawData))
          serviceResponse <- EitherT(service.amend(parsedRequest))
          vendorResponse <- EitherT.fromEither[Future](
<<<<<<< ad68f2112883a35cd843257bd392929d83a048b3
            hateoasFactory.wrap(serviceResponse.responseData, AmendOtherReliefsHateoasData(nino, taxYear)).asRight[ErrorWrapper]
          )
=======
            hateoasFactory.wrap(serviceResponse.responseData, AmendOtherReliefsHateoasData(nino, taxYear)).asRight[ErrorWrapper])
>>>>>>> continued amend other reliefs ITs
        } yield {
          logger.info(
            s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
              s"Sucess response recieved with CorrelationId: ${serviceResponse.correlationId}")

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
           RuleTaxYearRangeInvalidError |
           MtdErrorWithCustomMessage(ValueFormatError.code) |
<<<<<<< ad68f2112883a35cd843257bd392929d83a048b3
           MtdErrorWithCustomMessage(ReliefDateFormatError.code) |
           MtdErrorWithCustomMessage(CustomerReferenceFormatError.code) =>
=======
           MtdErrorWithCustomMessage(ReliefDateFormatError.code) =>
>>>>>>> continued amend other reliefs ITs
        BadRequest(Json.toJson(errorWrapper: ErrorWrapper))
      case DownstreamError => InternalServerError(Json.toJson(errorWrapper))
      case NotFoundError => NotFound(Json.toJson(errorWrapper))
      case UnauthorisedError => Unauthorized(Json.toJson(errorWrapper))
    }
  }
}
