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

package v2.charitableGiving.delete

import api.config.AppConfig
import api.connectors.DownstreamUri.IfsUri
import api.connectors.httpparsers.StandardDownstreamHttpParser.*
import api.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import api.models.domain.{Nino, TaxYear}
import config.ReliefsFeatureSwitches
import play.api.libs.json.JsObject
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import v2.charitableGiving.delete.model.request.{Def1_DeleteCharitableGivingTaxReliefsRequestData, DeleteCharitableGivingTaxReliefsRequestData}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeleteCharitableGivingReliefConnector @Inject() (val http: HttpClientV2, val appConfig: AppConfig) extends BaseDownstreamConnector {

  private def completeRequest(nino: Nino, taxYear: TaxYear)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[Unit]] = {

    val intent = if (ReliefsFeatureSwitches().isPassDeleteIntentEnabled) Some("DELETE") else None

    if (taxYear.useTaxYearSpecificApi) {
      val downstreamUri = IfsUri[Unit](s"income-tax/${taxYear.asTysDownstream}/$nino/income-source/charity/annual")
      delete(downstreamUri, maybeIntent = intent)
    } else {
      val downstreamUri = IfsUri[Unit](s"income-tax/nino/$nino/income-source/charity/annual/${taxYear.asDownstream}")
      post(JsObject.empty, downstreamUri, intent)
    }
  }

  def delete(request: DeleteCharitableGivingTaxReliefsRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[Unit]] = {

    request match {
      case def1: Def1_DeleteCharitableGivingTaxReliefsRequestData =>
        import def1.*
        completeRequest(nino, taxYear)
    }
  }

}
