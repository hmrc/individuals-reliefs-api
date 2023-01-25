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
import api.models.errors.{NinoFormatError, RuleTaxYearNotSupportedError, TaxYearFormatError}
import api.support.DownstreamResponseMappingSupport
import cats.implicits._
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import v1.connectors.CreateAndAmendReliefInvestmentsConnector
import v1.models.request.createAndAmendReliefInvestments.CreateAndAmendReliefInvestmentsRequest

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateAndAmendReliefInvestmentsService @Inject() (connector: CreateAndAmendReliefInvestmentsConnector)
    extends DownstreamResponseMappingSupport
    with Logging {

  def amend(request: CreateAndAmendReliefInvestmentsRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      logContext: EndpointLogContext,
      correlationId: String): Future[ServiceOutcome[Unit]] = {

    connector.amend(request).map(_.leftMap(mapDownstreamErrors(downstreamErrorMap)))
  }

  private val downstreamErrorMap = {
    val errors = Map(
      "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
      "INVALID_TAX_YEAR"          -> TaxYearFormatError,
      "INVALID_PAYLOAD"           -> models.errors.InternalError,
      "INVALID_CORRELATIONID"     -> models.errors.InternalError,
      "UNPROCESSABLE_ENTITY"      -> models.errors.InternalError,
      "SERVER_ERROR"              -> models.errors.InternalError,
      "SERVICE_UNAVAILABLE"       -> models.errors.InternalError
    )

    val extraTysErrors = Map(
      "INVALID_CORRELATION_ID" -> models.errors.InternalError,
      "TAX_YEAR_NOT_SUPPORTED" -> RuleTaxYearNotSupportedError
    )

    errors ++ extraTysErrors
  }

}
