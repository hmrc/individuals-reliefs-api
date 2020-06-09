/*
 * Copyright 2020 HM Revenue & Customs
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
import v1.connectors.RetrieveReliefInvestmentsConnector
import v1.controllers.EndpointLogContext
import v1.models.errors._
import v1.models.request.retrieveReliefInvestments.RetrieveReliefInvestmentsRequest
import v1.support.DesResponseMappingSupport

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveReliefInvestmentsService @Inject()(connector: RetrieveReliefInvestmentsConnector) extends DesResponseMappingSupport with Logging {

  def retrieve(request: RetrieveReliefInvestmentsRequest)(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext,
    logContext: EndpointLogContext): Future[RetrieveReliefInvestmentsServiceOutcome] = {

    val result = for {
      desResponseWrapper <- EitherT(connector.retrieve(request)).leftMap(mapDesErrors(desErrorMap))
    } yield desResponseWrapper

    result.value
  }

  private def desErrorMap =
    Map(
      "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
      "FORMAT_NINO" -> NinoFormatError,
      "FORMAT_TAX_YEAR" -> TaxYearFormatError,
      "CLIENT_OR_AGENT_NOT_AUTHORISED" -> UnauthorisedError,
      "NOT_FOUND" -> NotFoundError,
      "SERVER_ERROR" -> DownstreamError,
      "SERVICE_UNAVAILABLE" -> DownstreamError
    )
}
