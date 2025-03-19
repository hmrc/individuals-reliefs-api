/*
 * Copyright 2024 HM Revenue & Customs
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

package v2.otherReliefs.retrieve

import shared.config.SharedAppConfig
import shared.connectors.DownstreamUri.IfsUri
import shared.connectors.httpparsers.StandardDownstreamHttpParser._
import shared.connectors.{BaseDownstreamConnector, DownstreamOutcome, DownstreamUri}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.HeaderCarrier
import v2.otherReliefs.retrieve.model.request.RetrieveOtherReliefsRequestData
import v2.otherReliefs.retrieve.model.response.RetrieveOtherReliefsResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveOtherReliefsConnector @Inject() (val http: HttpClientV2, val appConfig: SharedAppConfig) extends BaseDownstreamConnector {

  def retrieve(request: RetrieveOtherReliefsRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[RetrieveOtherReliefsResponse]] = {

    import request._
    import schema._

    val url: DownstreamUri[DownstreamResp] =
      if (taxYear.useTaxYearSpecificApi) {
        IfsUri[DownstreamResp](s"income-tax/reliefs/other/${taxYear.asTysDownstream}/$nino")
      } else {
        // Note: endpoint uses mtd tax year format
        IfsUri[DownstreamResp](s"income-tax/reliefs/other/$nino/${taxYear.asMtd}")
      }

    get(url)
  }

}
