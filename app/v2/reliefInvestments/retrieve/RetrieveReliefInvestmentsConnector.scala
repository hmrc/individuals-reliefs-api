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

package v2.reliefInvestments.retrieve

import shared.config.SharedAppConfig
import shared.connectors.DownstreamUri.{HipUri, IfsUri}
import shared.connectors.httpparsers.StandardDownstreamHttpParser.*
import shared.connectors.{BaseDownstreamConnector, DownstreamOutcome, DownstreamUri}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import v2.reliefInvestments.retrieve.model.request.RetrieveReliefInvestmentsRequestData
import v2.reliefInvestments.retrieve.model.response.RetrieveReliefInvestmentsResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveReliefInvestmentsConnector @Inject() (val http: HttpClientV2, val appConfig: SharedAppConfig) extends BaseDownstreamConnector {

  def retrieve(request: RetrieveReliefInvestmentsRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[RetrieveReliefInvestmentsResponse]] = {

    import request.*
    import schema.*

    lazy val downstreamUri1925: DownstreamUri[DownstreamResp] =
      HipUri(s"itsa/income-tax/v1/${taxYear.asTysDownstream}/reliefs/investment/$nino")

    lazy val downstreamUri1630: DownstreamUri[DownstreamResp] =
      IfsUri(s"income-tax/reliefs/investment/$nino/${taxYear.asMtd}")

    val downstreamUri: DownstreamUri[DownstreamResp] =
      if (taxYear.useTaxYearSpecificApi) downstreamUri1925 else downstreamUri1630

    get(downstreamUri)

  }

}
