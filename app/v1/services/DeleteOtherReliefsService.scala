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

package v1.services

import api.controllers.EndpointLogContext
import api.models
import api.models.errors._
import api.support.DownstreamResponseMappingSupport
import cats.data.EitherT
import cats.implicits._
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import v1.connectors.DeleteOtherReliefsConnector
import v1.models.request.deleteOtherReliefs.DeleteOtherReliefsRequest

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeleteOtherReliefsService @Inject() (connector: DeleteOtherReliefsConnector) extends DownstreamResponseMappingSupport with Logging {

  def delete(request: DeleteOtherReliefsRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      logContext: EndpointLogContext,
      correlationId: String): Future[ServiceOutcome[Unit]] = {

    val result = EitherT(connector.delete(request)).leftMap(mapDownstreamErrors(errorMap))

    result.value
  }

  private val errorMap: Map[String, MtdError] = {
    val errors = Map(
      "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
      "FORMAT_TAX_YEAR"           -> TaxYearFormatError,
      "NO_DATA_FOUND"             -> NotFoundError,
      "SERVER_ERROR"              -> models.errors.InternalError,
      "INVALID_CORRELATIONID"     -> models.errors.InternalError,
      "INVALID_TAX_YEAR"          -> TaxYearFormatError,
      "SERVICE_UNAVAILABLE"       -> models.errors.InternalError
    )

    val extraTysErrors = Map(
      "INVALID_CORRELATION_ID" -> models.errors.InternalError,
      "TAX_YEAR_NOT_SUPPORTED" -> RuleTaxYearNotSupportedError
    )

    errors ++ extraTysErrors
  }

}
