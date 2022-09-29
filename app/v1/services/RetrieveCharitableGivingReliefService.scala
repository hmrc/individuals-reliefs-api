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
import v1.connectors.RetrieveCharitableGivingReliefConnector
import v1.controllers.EndpointLogContext
import v1.models.errors._
import v1.models.request.retrieveCharitableGivingTaxRelief.RetrieveCharitableGivingReliefRequest
import v1.models.response.retrieveCharitableGivingTaxRelief.RetrieveCharitableGivingReliefResponse
import v1.support.DownstreamResponseMappingSupport

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveCharitableGivingReliefService @Inject() (connector: RetrieveCharitableGivingReliefConnector)
    extends DownstreamResponseMappingSupport
    with Logging {

  def retrieve(request: RetrieveCharitableGivingReliefRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      logContext: EndpointLogContext,
      correlationId: String): Future[ServiceOutcome[RetrieveCharitableGivingReliefResponse]] = {

    val result = for {
      responseWrapper <- EitherT(connector.retrieve(request)).leftMap(mapDownstreamErrors(downstreamErrorMap))
    } yield responseWrapper

    result.value
  }

  private def downstreamErrorMap: Map[String, MtdError] = {
    val errors = Map(
      "INVALID_NINO"                 -> NinoFormatError,
      "INVALID_TYPE"                 -> InternalError,
      "INVALID_TAXYEAR"              -> TaxYearFormatError,
      "INVALID_INCOME_SOURCE"        -> InternalError,
      "NOT_FOUND_PERIOD"             -> NotFoundError,
      "NOT_FOUND_INCOME_SOURCE"      -> NotFoundError,
      "SERVER_ERROR"                 -> InternalError,
      "SERVICE_UNAVAILABLE"          -> InternalError
    )

    val extraTysErrors = Map(
      "INVALID_TAX_YEAR"             -> TaxYearFormatError,
      "INVALID_CORRELATION_ID"       -> InternalError,
      "INVALID_INCOMESOURCE_ID"      -> InternalError,
      "INVALID_INCOMESOURCE_TYPE"    -> InternalError,
      "SUBMISSION_PERIOD_NOT_FOUND"  -> NotFoundError,
      "INCOME_DATA_SOURCE_NOT_FOUND" -> NotFoundError,
      "TAX_YEAR_NOT_SUPPORTED"       -> RuleTaxYearNotSupportedError,
    )
    errors ++ extraTysErrors
  }
}
