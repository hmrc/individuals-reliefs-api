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
import api.connectors.DownstreamUri.{DesUri, TaxYearSpecificIfsUri}
import api.connectors.httpparsers.StandardDownstreamHttpParser._
import config.{AppConfig, FeatureSwitches}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v1.models.request.retrieveCharitableGivingTaxRelief.RetrieveCharitableGivingReliefRequest
import v1.models.response.retrieveCharitableGivingTaxRelief.RetrieveCharitableGivingReliefResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveCharitableGivingReliefConnector @Inject() (val http: HttpClient, val appConfig: AppConfig)(implicit
    val featureSwitches: FeatureSwitches)
    extends BaseDownstreamConnector {

  def retrieve(request: RetrieveCharitableGivingReliefRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[RetrieveCharitableGivingReliefResponse]] = {

    val downstreamUri =
      if (request.taxYear.useTaxYearSpecificApi) {
        TaxYearSpecificIfsUri[RetrieveCharitableGivingReliefResponse](
          s"income-tax/${request.taxYear.asTysDownstream}/${request.nino}/income-source/charity/annual")
      } else {
        DesUri[RetrieveCharitableGivingReliefResponse](
          s"income-tax/nino/${request.nino}/income-source/charity/annual/${request.taxYear.asDownstream}")
      }

    get(downstreamUri)
  }

}
