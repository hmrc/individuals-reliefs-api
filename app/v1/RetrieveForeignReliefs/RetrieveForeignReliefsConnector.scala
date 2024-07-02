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

package v1.RetrieveForeignReliefs

import api.connectors.DownstreamUri.{IfsUri, TaxYearSpecificIfsUri}
import api.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import api.connectors.httpparsers.StandardDownstreamHttpParser._
import config.AppConfig
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v1.RetrieveForeignReliefs.def1.model.response.Def1_RetrieveForeignReliefsResponse
import v1.RetrieveForeignReliefs.model.request.{Def1_RetrieveForeignReliefsRequestData, RetrieveForeignReliefsRequestData}
import v1.RetrieveForeignReliefs.model.response.RetrieveForeignReliefsResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveForeignReliefsConnector @Inject() (val http: HttpClient, val appConfig: AppConfig) extends BaseDownstreamConnector {

  private def completeDef1Request(request: Def1_RetrieveForeignReliefsRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[Def1_RetrieveForeignReliefsResponse]] = {

    import request._

    val downstreamUri = if (taxYear.useTaxYearSpecificApi) {
      TaxYearSpecificIfsUri[Def1_RetrieveForeignReliefsResponse](s"income-tax/reliefs/foreign/${taxYear.asTysDownstream}/$nino")
    } else {
      IfsUri[Def1_RetrieveForeignReliefsResponse](s"income-tax/reliefs/foreign/$nino/${taxYear.asMtd}")
    }

    get(downstreamUri)

  }

  def retrieve(request: RetrieveForeignReliefsRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[RetrieveForeignReliefsResponse]] = {

    request match {
      case def1: Def1_RetrieveForeignReliefsRequestData => completeDef1Request(def1)
    }
  }

}
