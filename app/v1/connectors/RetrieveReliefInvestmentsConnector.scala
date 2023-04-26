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

import api.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import api.connectors.DownstreamUri.{IfsUri, TaxYearSpecificIfsUri}
import api.connectors.httpparsers.StandardDownstreamHttpParser._
import config.AppConfig
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v1.models.request.retrieveReliefInvestments.RetrieveReliefInvestmentsRequest
import v1.models.response.retrieveReliefInvestments._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveReliefInvestmentsConnector @Inject() (val http: HttpClient, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def retrieve(request: RetrieveReliefInvestmentsRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[RetrieveReliefInvestmentsResponse]] = {

    import request._

    val url = if (taxYear.useTaxYearSpecificApi) {
      TaxYearSpecificIfsUri[RetrieveReliefInvestmentsResponse](s"income-tax/reliefs/investment/${taxYear.asTysDownstream}/$nino")
    } else {
      IfsUri[RetrieveReliefInvestmentsResponse](s"income-tax/reliefs/investment/$nino/${taxYear.asMtd}")
    }

    get(url)
  }

}
