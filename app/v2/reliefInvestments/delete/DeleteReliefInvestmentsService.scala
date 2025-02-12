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

package v2.reliefInvestments.delete

import cats.implicits._
import common.RuleOutsideAmendmentWindowError
import shared.controllers.RequestContext
import shared.models.errors._
import shared.services.{BaseService, ServiceOutcome}
import v2.reliefInvestments.delete.model.DeleteReliefInvestmentsRequestData

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeleteReliefInvestmentsService @Inject() (connector: DeleteReliefInvestmentsConnector) extends BaseService {

  def delete(request: DeleteReliefInvestmentsRequestData)(implicit ctx: RequestContext, ec: ExecutionContext): Future[ServiceOutcome[Unit]] = {

    connector.delete(request).map(_.leftMap(mapDownstreamErrors(errorMap)))
  }

  private val errorMap: Map[String, MtdError] = {
    val errors: Map[String, MtdError] = Map(
      "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
      "FORMAT_TAX_YEAR"           -> TaxYearFormatError,
      "OUTSIDE_AMENDMENT_WINDOW"  -> RuleOutsideAmendmentWindowError,
      "NO_DATA_FOUND"             -> NotFoundError,
      "SERVER_ERROR"              -> InternalError,
      "SERVICE_UNAVAILABLE"       -> InternalError
    )

    val extraTysErrors: Map[String, MtdError] = Map(
      "INVALID_CORRELATION_ID" -> InternalError,
      "TAX_YEAR_NOT_SUPPORTED" -> RuleTaxYearNotSupportedError
    )

    errors ++ extraTysErrors
  }

}
