/*
 * Copyright 2024 HM Revenue & Customs
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

package v2.createAndAmendCharitableGivingReliefs

import play.api.libs.json.JsValue
import play.api.mvc.{Action, ControllerComponents}
import shared.config.SharedAppConfig
import shared.controllers._
import shared.hateoas.HateoasFactory
import shared.routing.Version1
import shared.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import shared.utils.IdGenerator
import v2.createAndAmendCharitableGivingReliefs.model.response.CreateAndAmendCharitableGivingTaxReliefsHateoasData
import v2.createAndAmendCharitableGivingReliefs.model.response.CreateAndAmendCharitableGivingTaxReliefsResponse.LinksFactory

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class CreateAndAmendCharitableGivingReliefsController @Inject() (val authService: EnrolmentsAuthService,
                                                                 val lookupService: MtdIdLookupService,
                                                                 validatorFactory: CreateAndAmendCharitableGivingReliefsValidatorFactory,
                                                                 service: CreateAndAmendCharitableGivingTaxReliefsService,
                                                                 auditService: AuditService,
                                                                 hateoasFactory: HateoasFactory,
                                                                 cc: ControllerComponents,
                                                                 val idGenerator: IdGenerator)(implicit appConfig: SharedAppConfig, ec: ExecutionContext)
    extends AuthorisedController(cc) {

  val endpointName = "create-amend-charitable-giving-reliefs"

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "CreateAndAmendCharitableGivingController", endpointName = "createAndAmendCharitableGivingReliefs")

  def handleRequest(nino: String, taxYear: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val validator = validatorFactory.validator(nino, taxYear, request.body)

      val requestHandler = RequestHandler
        .withValidator(validator)
        .withService(service.amend)
        .withAuditing(
          AuditHandler(
            auditService,
            "CreateAndAmendCharitableGivingTaxRelief",
            "create-and-amend-charitable-giving-tax-relief",
            Version1,
            Map("nino" -> nino, "taxYear" -> taxYear),
            Some(request.body),
            includeResponse = true
          )
        )
        .withHateoasResult(hateoasFactory)(CreateAndAmendCharitableGivingTaxReliefsHateoasData(nino, taxYear))

      requestHandler.handleRequest()

    }

}
