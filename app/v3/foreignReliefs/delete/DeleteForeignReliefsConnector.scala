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

package v3.foreignReliefs.delete

import shared.config.SharedAppConfig
import shared.connectors.DownstreamUri.IfsUri
import shared.connectors.httpparsers.StandardDownstreamHttpParser._
import shared.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import shared.models.domain.{Nino, TaxYear}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.HeaderCarrier
import v3.foreignReliefs.delete.model.{Def1_DeleteForeignReliefsRequestData, DeleteForeignReliefsRequestData}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeleteForeignReliefsConnector @Inject() (val http: HttpClientV2, val appConfig: SharedAppConfig) extends BaseDownstreamConnector {

  private def completeRequest(nino: Nino, taxYear: TaxYear)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[Unit]] = {

    val downstreamUri = if (taxYear.useTaxYearSpecificApi) {
      IfsUri[Unit](s"income-tax/reliefs/foreign/${taxYear.asTysDownstream}/$nino")
    } else {
      IfsUri[Unit](s"income-tax/reliefs/foreign/$nino/${taxYear.asMtd}")
    }

    delete(
      downstreamUri
    )
  }

  def delete(request: DeleteForeignReliefsRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[Unit]] =
    request match {
      case def1: Def1_DeleteForeignReliefsRequestData =>
        import def1._
        completeRequest(nino, taxYear)
    }

}
