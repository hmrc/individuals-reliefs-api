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
import config.AppConfig
import play.api.libs.json.JsValue
import play.api.mvc.{Action, ControllerComponents}
import utils.{IdGenerator, Logging}
import v1.controllers.requestParsers.AmendPensionsReliefsRequestParser
import v1.models.request.amendPensionsReliefs.AmendPensionsReliefsRawData
import v1.models.response.amendPensionsReliefs.AmendPensionsReliefsHateoasData
import v1.models.response.amendPensionsReliefs.AmendPensionsReliefsResponse.LinksFactory
import v1.services.AmendPensionsReliefsService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class AmendPensionsReliefsController @Inject() (val authService: EnrolmentsAuthService,
                                                val lookupService: MtdIdLookupService,
                                                parser: AmendPensionsReliefsRequestParser,
                                                service: AmendPensionsReliefsService,
                                                auditService: AuditService,
                                                appConfig: AppConfig,
                                                hateoasFactory: HateoasFactory,
                                                cc: ControllerComponents,
                                                idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc)
    with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "AmendPensionsReliefsController", endpointName = "amendPensionsReliefs")

  def handleRequest(nino: String, taxYear: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val rawData = AmendPensionsReliefsRawData(nino, taxYear, request.body)

      val requestHandler = RequestHandler
        .withParser(parser)
        .withService(service.amend)
        .withAuditing(AuditHandler(
          auditService = auditService,
          auditType = "CreateAmendReliefPension",
          transactionName = "create-amend-reliefs-pensions",
          pathParams = Map("nino" -> nino, "taxYear" -> taxYear),
          requestBody = Some(request.body),
          includeResponse = true
        ))
        .withHateoasResult(hateoasFactory)(AmendPensionsReliefsHateoasData(nino, taxYear))

      requestHandler.handleRequest(rawData)

    }

}
