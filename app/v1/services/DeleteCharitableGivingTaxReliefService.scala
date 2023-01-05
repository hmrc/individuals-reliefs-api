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

import cats.data.EitherT
import cats.implicits._
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import v1.connectors.DeleteCharitableGivingTaxReliefConnector
import v1.controllers.EndpointLogContext
import v1.models.errors._
import v1.models.request.deleteCharitableGivingTaxRelief.DeleteCharitableGivingTaxReliefRequest
import v1.support.DownstreamResponseMappingSupport

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeleteCharitableGivingTaxReliefService @Inject() (connector: DeleteCharitableGivingTaxReliefConnector)
    extends DownstreamResponseMappingSupport
    with Logging {

  def delete(request: DeleteCharitableGivingTaxReliefRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      logContext: EndpointLogContext,
      correlationId: String): Future[DeleteCharitableGivingTaxReliefServiceOutcome] = {
    val result = for {
      responseWrapper <- EitherT(connector.delete(request)).leftMap(mapDownstreamErrors(downstreamErrorMap))
    } yield responseWrapper
    result.value
  }

  private val downstreamErrorMap = {
    val errors = Map(
      "INVALID_NINO"                      -> NinoFormatError,
      "INVALID_TYPE"                      -> InternalError,
      "INVALID_TAXYEAR"                   -> TaxYearFormatError,
      "INVALID_PAYLOAD"                   -> InternalError,
      "NOT_FOUND_INCOME_SOURCE"           -> NotFoundError,
      "MISSING_CHARITIES_NAME_GIFT_AID"   -> InternalError,
      "MISSING_CHARITIES_NAME_INVESTMENT" -> InternalError,
      "MISSING_INVESTMENT_AMOUNT"         -> InternalError,
      "INVALID_ACCOUNTING_PERIOD"         -> RuleTaxYearNotSupportedError,
      "SERVER_ERROR"                      -> InternalError,
      "SERVICE_UNAVAILABLE"               -> InternalError,
      "GONE"                              -> NotFoundError,
      "NOT_FOUND"                         -> NotFoundError
    )

    val extraTysErrors = Map(
      "INVALID_INCOMESOURCE_ID"      -> InternalError,
      "INVALID_INCOMESOURCE_TYPE"    -> InternalError,
      "INVALID_TAX_YEAR"             -> TaxYearFormatError,
      "TAX_YEAR_NOT_SUPPORTED"       -> RuleTaxYearNotSupportedError,
      "PERIOD_NOT_FOUND"             -> NotFoundError,
      "PERIOD_ALREADY_DELETED"       -> NotFoundError,
      "INCOME_SOURCE_DATA_NOT_FOUND" -> NotFoundError,
      "INVALID_CORRELATION_ID"       -> InternalError
    )

    errors ++ extraTysErrors
  }

}
