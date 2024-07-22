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

package v1.otherReliefs.retrieve

import api.controllers.RequestContext
import api.models.errors._
import api.services.{BaseService, ServiceOutcome}
import cats.implicits._
import v1.otherReliefs.retrieve.model.request.RetrieveOtherReliefsRequestData
import v1.otherReliefs.retrieve.model.response.RetrieveOtherReliefsResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveOtherReliefsService @Inject() (connector: RetrieveOtherReliefsConnector) extends BaseService {

  def retrieve(request: RetrieveOtherReliefsRequestData)(implicit
      ctx: RequestContext,
      ec: ExecutionContext): Future[ServiceOutcome[RetrieveOtherReliefsResponse]] = {

    connector.retrieve(request).map(_.leftMap(mapDownstreamErrors(errorMap)))
  }

  private val errorMap = {
    val errors = Map(
      "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
      "INVALID_TAX_YEAR"          -> TaxYearFormatError,
      "NO_DATA_FOUND"             -> NotFoundError,
      "INVALID_CORRELATIONID"     -> InternalError,
      "SERVER_ERROR"              -> InternalError,
      "SERVICE_UNAVAILABLE"       -> InternalError
    )

    val extraTysErrors = Map(
      "TAX_YEAR_NOT_SUPPORTED" -> RuleTaxYearNotSupportedError,
      "INVALID_CORRELATION_ID" -> InternalError
    )

    errors ++ extraTysErrors
  }

}
