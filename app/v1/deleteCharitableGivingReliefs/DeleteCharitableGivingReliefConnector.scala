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

package v1.deleteCharitableGivingReliefs

import config.ReliefsFeatureSwitches
import play.api.libs.json.JsObject
import shared.config.SharedAppConfig
import shared.connectors.DownstreamUri.IfsUri
import shared.connectors.httpparsers.StandardDownstreamHttpParser._
import shared.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import shared.models.domain.{Nino, TaxYear}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.HeaderCarrier
import v1.deleteCharitableGivingReliefs.model.request.{Def1_DeleteCharitableGivingTaxReliefsRequestData, DeleteCharitableGivingTaxReliefsRequestData}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeleteCharitableGivingReliefConnector @Inject() (val http: HttpClientV2, val appConfig: SharedAppConfig) extends BaseDownstreamConnector {

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
        import def1._
        completeRequest(nino, taxYear)
    }
  }

}
