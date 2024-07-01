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

package v1.RetrieveCharitableGiving

import api.connectors.DownstreamUri.{DesUri, IfsUri, TaxYearSpecificIfsUri}
import api.connectors.httpparsers.StandardDownstreamHttpParser.reads
import api.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import config.AppConfig
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v1.RetrieveCharitableGiving.def1.model.response.Def1_RetrieveCharitableGivingReliefResponse
import v1.RetrieveCharitableGiving.model.request.{Def1_RetrieveCharitableGivingReliefRequestData, RetrieveCharitableGivingReliefRequestData}
import v1.RetrieveCharitableGiving.model.response.RetrieveCharitableGivingReliefResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveCharitableGivingReliefConnector @Inject() (val http: HttpClient, val appConfig: AppConfig) extends BaseDownstreamConnector {

  private def completeDef1Request(request: Def1_RetrieveCharitableGivingReliefRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[Def1_RetrieveCharitableGivingReliefResponse]] = {

    import request._
    def preTysPath = s"income-tax/nino/$nino/income-source/charity/annual/${taxYear.asDownstream}"
    val downstreamUri =
      if (taxYear.useTaxYearSpecificApi) {
        TaxYearSpecificIfsUri[Def1_RetrieveCharitableGivingReliefResponse](
          s"income-tax/${taxYear.asTysDownstream}/$nino/income-source/charity/annual")
      } else if (featureSwitches.isDesIf_MigrationEnabled) {
        IfsUri[Def1_RetrieveCharitableGivingReliefResponse](preTysPath)
      } else {
        DesUri[Def1_RetrieveCharitableGivingReliefResponse](preTysPath)
      }

    val result = get(downstreamUri)
    result

  }

  def retrieve(
      request: RetrieveCharitableGivingReliefRequestData
  )(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String
  ): Future[DownstreamOutcome[RetrieveCharitableGivingReliefResponse]] = {

    request match {
      case def1: Def1_RetrieveCharitableGivingReliefRequestData =>
        completeDef1Request(def1)
      case _ =>
        Future.failed(new IllegalArgumentException("Request type is not known"))
    }
  }

}
