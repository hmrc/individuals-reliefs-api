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

package v1.pensionReliefs.retrieve

import api.connectors.DownstreamUri.{DesUri, HipUri, IfsUri}
import api.connectors.httpparsers.StandardDownstreamHttpParser._
import api.connectors.{BaseDownstreamConnector, DownstreamOutcome, DownstreamUri}
import config.{AppConfig, FeatureSwitches}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v1.pensionReliefs.retrieve.model.request.RetrievePensionsReliefsRequestData
import v1.pensionReliefs.retrieve.model.response.RetrievePensionsReliefsResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrievePensionsReliefsConnector @Inject() (val http: HttpClient, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def retrieve(request: RetrievePensionsReliefsRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[RetrievePensionsReliefsResponse]] = {

    import request._
    import schema._

    val downstreamUri: DownstreamUri[DownstreamResp] = taxYear match {
      case ty if ty.useTaxYearSpecificApi =>
        IfsUri(s"income-tax/reliefs/pensions/${taxYear.asTysDownstream}/$nino")
      case _ =>
        val downstreamTaxYearParam = taxYear.asMtd // Supposed to be MTD format for this downstream endpoint
        if (FeatureSwitches(appConfig.featureSwitches).isEnabled("des_hip_migration_1656")) {
          HipUri(s"itsa/income-tax/v1/reliefs/pensions/$nino/$downstreamTaxYearParam")
        } else {
          DesUri(s"income-tax/reliefs/pensions/$nino/$downstreamTaxYearParam")
        }
    }
    get(downstreamUri)

  }

}
