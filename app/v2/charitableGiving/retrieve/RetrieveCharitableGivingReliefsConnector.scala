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

package v2.charitableGiving.retrieve

import shared.config.SharedAppConfig
import shared.connectors.DownstreamUri.IfsUri
import shared.connectors.httpparsers.StandardDownstreamHttpParser._
import shared.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.HeaderCarrier
import v2.charitableGiving.retrieve.model.request.RetrieveCharitableGivingReliefsRequestData
import v2.charitableGiving.retrieve.model.response.RetrieveCharitableGivingReliefsResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveCharitableGivingReliefsConnector @Inject() (val http: HttpClientV2, val appConfig: SharedAppConfig) extends BaseDownstreamConnector {

  def retrieve(request: RetrieveCharitableGivingReliefsRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[RetrieveCharitableGivingReliefsResponse]] = {

    import request._
    def preTysPath = s"income-tax/nino/$nino/income-source/charity/annual/${taxYear.asDownstream}"
    val downstreamUri =
      if (taxYear.useTaxYearSpecificApi) {
        IfsUri[RetrieveCharitableGivingReliefsResponse](s"income-tax/${taxYear.asTysDownstream}/$nino/income-source/charity/annual")
      } else {
        IfsUri[RetrieveCharitableGivingReliefsResponse](preTysPath)
      }

    get(uri = downstreamUri)

  }

//  def retrieve(
//      request: RetrieveCharitableGivingReliefsRequestData
//  )(implicit
//      hc: HeaderCarrier,
//      ec: ExecutionContext,
//      correlationId: String
//  ): Future[DownstreamOutcome[RetrieveCharitableGivingReliefsResponse]] = {
//
//    request match {
//      case def1: Def1_RetrieveCharitableGivingReliefsRequestData =>
//        completeDef1Request(def1)
//    }
//  }

}
