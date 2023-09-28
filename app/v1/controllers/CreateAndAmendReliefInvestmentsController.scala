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

import api.controllers._
import api.hateoas.HateoasFactory
import api.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import play.api.libs.json.JsValue
import play.api.mvc.{Action, ControllerComponents}
import routing.{Version, Version1}
import utils.IdGenerator
import v1.controllers.validators.CreateAndAmendReliefInvestmentsValidatorFactory
import v1.models.response.createAndAmendReliefInvestments.CreateAndAmendReliefInvestmentsHateoasData
import v1.models.response.createAndAmendReliefInvestments.CreateAndAmendReliefInvestmentsResponse.LinksFactory
import v1.services.CreateAndAmendReliefInvestmentsService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class CreateAndAmendReliefInvestmentsController @Inject() (val authService: EnrolmentsAuthService,
                                                           val lookupService: MtdIdLookupService,
                                                           validatorFactory: CreateAndAmendReliefInvestmentsValidatorFactory,
                                                           service: CreateAndAmendReliefInvestmentsService,
                                                           auditService: AuditService,
                                                           hateoasFactory: HateoasFactory,
                                                           cc: ControllerComponents,
                                                           val idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc) {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "CreateAndAmendReliefInvestmentsController", endpointName = "createAndAmendReliefInvestments")

  def handleRequest(nino: String, taxYear: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val validator = validatorFactory.validator(nino, taxYear, request.body)

      val requestHandler = RequestHandler
        .withValidator(validator)
        .withService(service.amend)
        .withAuditing(AuditHandler(
          auditService,
          "CreateAmendReliefsInvestment",
          "create-amend-reliefs-investment",
          Version.from(request, orElse = Version1),
          Map("nino" -> nino, "taxYear" -> taxYear),
          Some(request.body),
          includeResponse = true
        ))
        .withHateoasResult(hateoasFactory)(CreateAndAmendReliefInvestmentsHateoasData(nino, taxYear))

      requestHandler.handleRequest()
    }

}
