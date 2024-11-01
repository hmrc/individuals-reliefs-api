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

package v1.retrieveCharitableGivingReliefs

import api.connectors.DownstreamUri.{IfsUri, TaxYearSpecificIfsUri}
import api.connectors.httpparsers.StandardDownstreamHttpParser.reads
import api.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import config.AppConfig
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v1.retrieveCharitableGivingReliefs.model.request.{Def1_RetrieveCharitableGivingReliefsRequestData, RetrieveCharitableGivingReliefsRequestData}
import v1.retrieveCharitableGivingReliefs.model.response.{Def1_RetrieveCharitableGivingReliefsResponse, RetrieveCharitableGivingReliefsResponse}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveCharitableGivingReliefsConnector @Inject() (val http: HttpClient, val appConfig: AppConfig) extends BaseDownstreamConnector {

  private def completeDef1Request(request: Def1_RetrieveCharitableGivingReliefsRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[Def1_RetrieveCharitableGivingReliefsResponse]] = {

    import request._
    def preTysPath = s"income-tax/nino/$nino/income-source/charity/annual/${taxYear.asDownstream}"
    val downstreamUri =
      if (taxYear.useTaxYearSpecificApi) {
        TaxYearSpecificIfsUri[Def1_RetrieveCharitableGivingReliefsResponse](
          s"income-tax/${taxYear.asTysDownstream}/$nino/income-source/charity/annual")
      } else {
        IfsUri[Def1_RetrieveCharitableGivingReliefsResponse](preTysPath)
      }

    val result = get(downstreamUri)
    result

  }

  def retrieve(
      request: RetrieveCharitableGivingReliefsRequestData
  )(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String
  ): Future[DownstreamOutcome[RetrieveCharitableGivingReliefsResponse]] = {

    request match {
      case def1: Def1_RetrieveCharitableGivingReliefsRequestData =>
        completeDef1Request(def1)
    }
  }
}
