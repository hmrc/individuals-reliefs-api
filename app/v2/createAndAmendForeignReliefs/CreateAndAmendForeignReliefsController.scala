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

package v2.createAndAmendForeignReliefs

import play.api.libs.json.JsValue
import play.api.mvc.{Action, ControllerComponents}
import shared.config.SharedAppConfig
import shared.controllers._
import shared.hateoas.HateoasFactory
import shared.routing.Version2
import shared.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import shared.utils.IdGenerator
import v2.createAndAmendForeignReliefs.model.response.CreateAndAmendForeignReliefsHateoasData
import v2.createAndAmendForeignReliefs.model.response.CreateAndAmendForeignReliefsResponse.LinksFactory

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class CreateAndAmendForeignReliefsController @Inject() (val authService: EnrolmentsAuthService,
                                                        val lookupService: MtdIdLookupService,
                                                        validatorFactory: CreateAndAmendForeignReliefsValidatorFactory,
                                                        service: CreateAndAmendForeignReliefsService,
                                                        auditService: AuditService,
                                                        hateoasFactory: HateoasFactory,
                                                        cc: ControllerComponents,
                                                        val idGenerator: IdGenerator)(implicit appConfig: SharedAppConfig, ec: ExecutionContext)
    extends AuthorisedController(cc) {

  val endpointName = "create-amend-foreign-reliefs"

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "CreateAndAmendForeignReliefsController", endpointName = "createAndAmendForeignReliefs")

  def handleRequest(nino: String, taxYear: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val validator = validatorFactory.validator(nino, taxYear, request.body)

      val requestHandler = RequestHandler
        .withValidator(validator)
        .withService(service.createAndAmend)
        .withAuditing(AuditHandler(
          auditService,
          "CreateAmendForeignReliefs",
          "create-amend-foreign-reliefs",
          Version2,
          Map("nino" -> nino, "taxYear" -> taxYear),
          Some(request.body),
          includeResponse = true
        ))
        .withHateoasResult(hateoasFactory)(CreateAndAmendForeignReliefsHateoasData(nino, taxYear))

      requestHandler.handleRequest()
    }

}
