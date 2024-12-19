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

package v1.createAndAmendForeignReliefs

import shared.config.SharedAppConfig
import shared.connectors.DownstreamUri.IfsUri
import shared.connectors.httpparsers.StandardDownstreamHttpParser._
import shared.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v1.createAndAmendForeignReliefs.def1.model.request.Def1_CreateAndAmendForeignReliefsRequestData
import v1.createAndAmendForeignReliefs.model.request.CreateAndAmendForeignReliefsRequestData

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateAndAmendForeignReliefsConnector @Inject() (val http: HttpClient, val appConfig: SharedAppConfig) extends BaseDownstreamConnector {

  def createAndAmend(request: CreateAndAmendForeignReliefsRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[Unit]] = {

    import request._

    val url = if (taxYear.useTaxYearSpecificApi) {
      IfsUri[Unit](s"income-tax/reliefs/foreign/${taxYear.asTysDownstream}/$nino")
    } else {
      IfsUri[Unit](s"income-tax/reliefs/foreign/$nino/${taxYear.asMtd}")
    }

    request.asInstanceOf[Def1_CreateAndAmendForeignReliefsRequestData] match {
      case def1: Def1_CreateAndAmendForeignReliefsRequestData =>
        import def1._

        put(body, url)
    }

  }

}
