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

package v1.retrieveCharitableGivingReliefs

import play.api.mvc.{Action, AnyContent, ControllerComponents}
import shared.config.SharedAppConfig
import shared.controllers._
import shared.hateoas.HateoasFactory
import shared.services.{EnrolmentsAuthService, MtdIdLookupService}
import shared.utils.IdGenerator
import v1.retrieveCharitableGivingReliefs.model.response.RetrieveCharitableGivingReliefsHateoasData

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class RetrieveCharitableGivingReliefsController @Inject() (val authService: EnrolmentsAuthService,
                                                           val lookupService: MtdIdLookupService,
                                                           validatorFactory: RetrieveCharitableGivingReliefsValidatorFactory,
                                                           service: RetrieveCharitableGivingReliefsService,
                                                           hateoasFactory: HateoasFactory,
                                                           cc: ControllerComponents,
                                                           val idGenerator: IdGenerator)(implicit appConfig: SharedAppConfig, ec: ExecutionContext)
    extends AuthorisedController(cc) {

  val endpointName = "retrieve-charitable-giving-reliefs"

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "RetrieveCharitableGivingReliefController", endpointName = "retrieveCharitableGivingTaxRelief")

  def handleRequest(nino: String, taxYear: String): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val validator = validatorFactory.validator(nino, taxYear)

      val requestHandler = RequestHandler
        .withValidator(validator)
        .withService(service.retrieve)
        .withHateoasResult(hateoasFactory)(RetrieveCharitableGivingReliefsHateoasData(nino, taxYear))

      requestHandler.handleRequest()
    }

}
