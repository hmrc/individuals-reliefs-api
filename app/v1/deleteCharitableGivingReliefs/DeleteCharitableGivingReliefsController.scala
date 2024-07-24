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

package v1.deleteCharitableGivingReliefs

import api.controllers._
import api.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import routing.{Version, Version1}
import utils.IdGenerator

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class DeleteCharitableGivingReliefsController @Inject() (val authService: EnrolmentsAuthService,
                                                         val lookupService: MtdIdLookupService,
                                                         validatorFactory: DeleteCharitableGivingValidatorReliefsFactory,
                                                         service: DeleteCharitableGivingTaxReliefsService,
                                                         auditService: AuditService,
                                                         cc: ControllerComponents,
                                                         val idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc) {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "DeleteCharitableGivingController", endpointName = "deleteCharitableGiving")

  def handleRequest(nino: String, taxYear: String): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val validator = validatorFactory.validator(nino, taxYear)

      val requestHandler = RequestHandler
        .withValidator(validator)
        .withService(service.delete)
        .withAuditing(
          AuditHandler(
            auditService = auditService,
            auditType = "DeleteCharitableGivingTaxRelief",
            transactionName = "delete-charitable-giving-tax-relief",
            apiVersion = Version.from(request, orElse = Version1),
            params = Map("nino" -> nino, "taxYear" -> taxYear)
          )
        )

      requestHandler.handleRequest()
    }

}
