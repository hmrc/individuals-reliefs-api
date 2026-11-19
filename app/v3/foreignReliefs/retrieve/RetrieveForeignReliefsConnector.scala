/*
 * Copyright 2025 HM Revenue & Customs
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

package v3.foreignReliefs.retrieve

import api.config.AppConfig
import api.connectors.DownstreamUri.IfsUri
import api.connectors.httpparsers.StandardDownstreamHttpParser.*
import api.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import v3.foreignReliefs.retrieve.model.request.RetrieveForeignReliefsRequestData
import v3.foreignReliefs.retrieve.model.response.RetrieveForeignReliefsResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveForeignReliefsConnector @Inject() (val http: HttpClientV2, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def retrieve(request: RetrieveForeignReliefsRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[RetrieveForeignReliefsResponse]] = {

    import request.*

    val downstreamUri = if (taxYear.useTaxYearSpecificApi) {
      IfsUri[RetrieveForeignReliefsResponse](s"income-tax/reliefs/foreign/${taxYear.asTysDownstream}/$nino")
    } else {
      IfsUri[RetrieveForeignReliefsResponse](s"income-tax/reliefs/foreign/$nino/${taxYear.asMtd}")
    }

    get(downstreamUri)

  }

}
