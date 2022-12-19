/*
 * Copyright 2022 HM Revenue & Customs
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

import cats.data.EitherT
import cats.implicits._

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import v1.connectors.RetrieveOtherReliefsConnector
import v1.controllers.EndpointLogContext
import v1.models.errors.{InternalError, NinoFormatError, NotFoundError, RuleTaxYearNotSupportedError, TaxYearFormatError}
import v1.models.request.retrieveOtherReliefs.RetrieveOtherReliefsRequest
import v1.models.response.retrieveOtherReliefs.RetrieveOtherReliefsResponse
import v1.support.DownstreamResponseMappingSupport

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveOtherReliefsService @Inject() (connector: RetrieveOtherReliefsConnector) extends DownstreamResponseMappingSupport with Logging {

  def retrieve(request: RetrieveOtherReliefsRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      logContext: EndpointLogContext,
      correlationId: String): Future[ServiceOutcome[RetrieveOtherReliefsResponse]] = {

    val result = for {
      responseWrapper <- EitherT(connector.retrieve(request)).leftMap(mapDownstreamErrors(errorMap))
    } yield responseWrapper

    result.value
  }

  private val errorMap = {
    val errors = Map(
      "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
      "FORMAT_TAX_YEAR"           -> TaxYearFormatError,
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
