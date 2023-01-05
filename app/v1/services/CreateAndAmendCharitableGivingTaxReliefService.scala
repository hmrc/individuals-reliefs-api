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
import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import v1.connectors.CreateAndAmendCharitableGivingTaxReliefConnector
import v1.controllers.EndpointLogContext
import v1.models.errors._
import v1.models.request.createAndAmendCharitableGivingTaxRelief.CreateAndAmendCharitableGivingTaxReliefRequest
import v1.support.DownstreamResponseMappingSupport

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateAndAmendCharitableGivingTaxReliefService @Inject() (connector: CreateAndAmendCharitableGivingTaxReliefConnector)
    extends DownstreamResponseMappingSupport
    with Logging {

  def amend(request: CreateAndAmendCharitableGivingTaxReliefRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      logContext: EndpointLogContext,
      correlationId: String): Future[ServiceOutcome[Unit]] = {

    val result = for {
      responseWrapper <- EitherT(connector.createAmend(request)).leftMap(mapDownstreamErrors(downstreamErrorMap))
    } yield responseWrapper

    result.value
  }

  private val downstreamErrorMap: Map[String, MtdError] = {
    val errors = Map(
      "INVALID_NINO"                      -> NinoFormatError,
      "INVALID_TYPE"                      -> InternalError,
      "INVALID_TAXYEAR"                   -> TaxYearFormatError,
      "INVALID_PAYLOAD"                   -> InternalError,
      "NOT_FOUND_INCOME_SOURCE"           -> NotFoundError,
      "MISSING_CHARITIES_NAME_GIFT_AID"   -> RuleGiftAidNonUkAmountWithoutNamesError,
      "MISSING_GIFT_AID_AMOUNT"           -> InternalError,
      "MISSING_CHARITIES_NAME_INVESTMENT" -> RuleGiftsNonUkAmountWithoutNamesError,
      "MISSING_INVESTMENT_AMOUNT"         -> InternalError,
      "INVALID_ACCOUNTING_PERIOD"         -> RuleTaxYearNotSupportedError,
      "SERVER_ERROR"                      -> InternalError,
      "SERVICE_UNAVAILABLE"               -> InternalError,
      "GONE"                              -> InternalError,
      "NOT_FOUND"                         -> NotFoundError
    )

    val extraTysErrors = Map(
      "INVALID_INCOMESOURCE_TYPE" -> InternalError,
      "INVALID_TAX_YEAR" -> TaxYearFormatError,
      "INVALID_CORRELATIONID" -> InternalError,
      "INCOME_SOURCE_NOT_FOUND" -> NotFoundError,
      "INCOMPATIBLE_INCOME_SOURCE" -> InternalError,
      "TAX_YEAR_NOT_SUPPORTED" -> RuleTaxYearNotSupportedError
    )

    errors ++ extraTysErrors
  }


}
