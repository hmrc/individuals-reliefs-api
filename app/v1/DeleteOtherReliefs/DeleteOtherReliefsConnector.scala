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

package v1.DeleteOtherReliefs

import api.connectors.DownstreamUri.{IfsUri, TaxYearSpecificIfsUri}
import api.connectors.httpparsers.StandardDownstreamHttpParser._
import api.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import api.models.domain.{Nino, TaxYear}
import config.AppConfig
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v1.DeleteOtherReliefs.model.{Def1_DeleteOtherReliefsRequestData, DeleteOtherReliefsRequestData}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeleteOtherReliefsConnector @Inject() (val http: HttpClient, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def delete(request: DeleteOtherReliefsRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[Unit]] = {

    request match {
      case def1: Def1_DeleteOtherReliefsRequestData =>
        import def1._
        completeRequest(nino, taxYear)
    }
  }

  private def completeRequest(nino: Nino, taxYear: TaxYear)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[Unit]] = {

    val downstreamUri = if (taxYear.useTaxYearSpecificApi) {
      TaxYearSpecificIfsUri[Unit](s"income-tax/reliefs/other/${taxYear.asTysDownstream}/$nino")
    } else {
      IfsUri[Unit](s"income-tax/reliefs/other/$nino/${taxYear.asMtd}")
    }

    delete(uri = downstreamUri)

  }

}
