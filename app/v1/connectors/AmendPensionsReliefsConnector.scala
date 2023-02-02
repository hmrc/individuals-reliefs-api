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
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v1.models.request.amendPensionsReliefs.AmendPensionsReliefsRequest

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AmendPensionsReliefsConnector @Inject() (val http: HttpClient, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def createOrAmendPensionsRelief(request: AmendPensionsReliefsRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[Unit]] = {

    val downstreamUri =
      if (request.taxYear.useTaxYearSpecificApi) {
        TaxYearSpecificIfsUri[Unit](s"income-tax/reliefs/pensions/${request.taxYear.asTysDownstream}/${request.nino}")
      } else {
        DesUri[Unit](s"income-tax/reliefs/pensions/${request.nino}/${request.taxYear.asMtd}")
      }

    put(
      body = request.body,
      uri = downstreamUri
    )
  }

}
