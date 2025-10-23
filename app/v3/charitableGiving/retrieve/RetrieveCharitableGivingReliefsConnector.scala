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

package v3.charitableGiving.retrieve

import shared.config.SharedAppConfig
import shared.connectors.DownstreamUri.*
import shared.connectors.*
import shared.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.HeaderCarrier
import v3.charitableGiving.retrieve.model.request.RetrieveCharitableGivingReliefsRequestData
import v3.charitableGiving.retrieve.model.response.RetrieveCharitableGivingReliefsResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveCharitableGivingReliefsConnector @Inject() (val http: HttpClientV2, val appConfig: SharedAppConfig) extends BaseDownstreamConnector {

  import shared.connectors.httpparsers.StandardDownstreamHttpParser.*

  def retrieve(request: RetrieveCharitableGivingReliefsRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[RetrieveCharitableGivingReliefsResponse]] = {

    import request.*
    import schema._

    def preTysPath = s"income-tax/nino/$nino/income-source/charity/annual/${taxYear.asDownstream}"
    val downstreamUri: DownstreamUri[DownstreamResp] =
      if (taxYear.useTaxYearSpecificApi) {
        IfsUri(s"income-tax/${taxYear.asTysDownstream}/$nino/income-source/charity/annual")
      } else {
        IfsUri(preTysPath)
      }

    get(uri = downstreamUri)

  }

}
