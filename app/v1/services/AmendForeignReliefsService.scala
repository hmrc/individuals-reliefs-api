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
import v1.connectors.AmendForeignReliefsConnector
import v1.controllers.EndpointLogContext
import v1.models.errors._
import v1.models.request.amendForeignReliefs.AmendForeignReliefsRequest
import v1.support.DownstreamResponseMappingSupport

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AmendForeignReliefsService @Inject() (connector: AmendForeignReliefsConnector) extends DownstreamResponseMappingSupport with Logging {

  def amend(request: AmendForeignReliefsRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      logContext: EndpointLogContext,
      correlationId: String): Future[ServiceOutcome[Unit]] = {

    val result = for {
      responseWrapper <- EitherT(connector.amend(request)).leftMap(mapDownstreamErrors(desErrorMap))
    } yield responseWrapper

    result.value
  }

  private def desErrorMap =
    Map(
      "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
      "FORMAT_TAX_YEAR"           -> TaxYearFormatError,
      "SERVER_ERROR"              -> InternalError,
      "SERVICE_UNAVAILABLE"       -> InternalError
    )

}
