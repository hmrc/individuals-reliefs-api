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

package v1.pensionReliefs.createAmend

import api.controllers._
import api.hateoas.HateoasFactory
import api.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import config.AppConfig
import play.api.libs.json.JsValue
import play.api.mvc.{Action, ControllerComponents}
import routing.{Version, Version1}
import utils.IdGenerator
import v1.pensionReliefs.createAmend.model.response.CreateAmendPensionsReliefsHateoasData
import v1.pensionReliefs.createAmend.model.response.CreateAmendPensionsReliefsResponse.LinksFactory

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class CreateAmendPensionsReliefsController @Inject() (val authService: EnrolmentsAuthService,
                                                      val lookupService: MtdIdLookupService,
                                                      validatorFactory: CreateAmendPensionsReliefsValidatorFactory,
                                                      service: CreateAmendPensionsReliefsService,
                                                      auditService: AuditService,
                                                      hateoasFactory: HateoasFactory,
                                                      cc: ControllerComponents,
                                                      idGenerator: IdGenerator)(implicit appConfig: AppConfig,ec: ExecutionContext)
    extends AuthorisedController(cc) {

  val endpointName = "create-amend-pensions-reliefs"

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "AmendPensionsReliefsController", endpointName = "amendPensionsReliefs")

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
            "CreateAmendReliefPension",
            "create-amend-reliefs-pensions",
            Version.from(request, orElse = Version1),
            Map("nino" -> nino, "taxYear" -> taxYear),
            Some(request.body),
            includeResponse = true
          )
        )
        .withHateoasResult(hateoasFactory)(CreateAmendPensionsReliefsHateoasData(nino, taxYear))

      requestHandler.handleRequest()

    }

}
