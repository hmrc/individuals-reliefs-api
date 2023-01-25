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

package v1.connectors

import api.connectors.BaseDownstreamConnector
import api.connectors.DownstreamUri.{DesUri, TaxYearSpecificIfsUri}
import api.connectors.connectors.DownstreamOutcome
import api.connectors.httpparsers.StandardDownstreamHttpParser._
import config.AppConfig
import play.api.http.Status.OK
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v1.models.request.createAndAmendCharitableGivingTaxRelief.CreateAndAmendCharitableGivingTaxReliefRequest

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateAndAmendCharitableGivingTaxReliefConnector @Inject() (val http: HttpClient, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def createAmend(request: CreateAndAmendCharitableGivingTaxReliefRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[Unit]] = {

    implicit val successCode: SuccessCode = SuccessCode(OK)

    val downstreamUri = if (request.taxYear.useTaxYearSpecificApi) {
      TaxYearSpecificIfsUri[Unit](s"income-tax/${request.taxYear.asTysDownstream}/${request.nino}/income-source/charity/annual")
    } else {
      DesUri[Unit](s"income-tax/nino/${request.nino}/income-source/charity/annual/${request.taxYear.asDownstream}")
    }

    post(
      body = request.body,
      uri = downstreamUri
    )
  }

}
