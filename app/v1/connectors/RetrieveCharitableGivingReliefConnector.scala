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

import api.connectors.DownstreamUri.{DesUri, IfsUri, TaxYearSpecificIfsUri}
import api.connectors.httpparsers.StandardDownstreamHttpParser._
import api.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import config.AppConfig
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v1.models.request.retrieveCharitableGivingTaxRelief.RetrieveCharitableGivingReliefRequestData
import v1.models.response.retrieveCharitableGivingTaxRelief.RetrieveCharitableGivingReliefResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveCharitableGivingReliefConnector @Inject() (val http: HttpClient, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def retrieve(
                request: RetrieveCharitableGivingReliefRequestData
              )(implicit
                hc: HeaderCarrier,
                ec: ExecutionContext,
                correlationId: String
              ): Future[DownstreamOutcome[RetrieveCharitableGivingReliefResponse]] = {

    import request._

    def preTysPath = s"income-tax/nino/$nino/income-source/charity/annual/${taxYear.asDownstream}"

    val downstreamUri =
      if (taxYear.useTaxYearSpecificApi) {
        TaxYearSpecificIfsUri[RetrieveCharitableGivingReliefResponse](s"income-tax/${taxYear.asTysDownstream}/$nino/income-source/charity/annual")
      } else if (featureSwitches.isDesIf_MigrationEnabled) {
        IfsUri[RetrieveCharitableGivingReliefResponse](preTysPath)
      } else {
        DesUri[RetrieveCharitableGivingReliefResponse](preTysPath)
      }

    get(downstreamUri)
  }

}
