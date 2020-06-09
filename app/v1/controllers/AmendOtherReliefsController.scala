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
 * WITHOUT WARRANTIED OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package v1.controllers

import cats.Inject
import play.api.libs.json.JsValue
import play.api.mvc.{Action, ControllerComponents}
import utils.Logging
import v1.hateoas.HateoasFactory
import v1.models.request.amendOtherReliefs.AmendOtherReliefsRawData
import v1.services.{EnrolmentsAuthService, MtdIdLookupService}

import scala.concurrent.ExecutionContext

@Singleton
class AmendOtherReliefsController @Inject()(val authService: EnrolmentsAuthService,
                                                 val lookupService: MtdIdLookupService,
                                                 amendReliefInvestmentsParser: AmendOtherReliefsRequestParser,
                                                 amendReliefInvestmentsService: AmendOtherReliefsService,
                                                 hateoasFactory: HateoasFactory,
                                                 cc: ControllerComponents)(implicit ec: ExecutionContext)
  extends AuthorisedController(cc) with BaseController with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "AmendOtherReliefsController", endpointName = "amendOtherReliefs")

  def handleRequest(nino: String, taxYear: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      val rawData = AmendOtherReliefsRawData(nino, taxYear, AmendOtherReliefsHatoasData)
    }

}
