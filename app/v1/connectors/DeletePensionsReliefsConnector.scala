/*
 * Copyright 2022 HM Revenue & Customs
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

import config.AppConfig
import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.HttpClient
import v1.connectors.DownstreamUri.{DesUri, TaxYearSpecificIfsUri}
import v1.connectors.httpparsers.StandardDownstreamHttpParser._
import v1.models.request.deletePensionsReliefs.DeletePensionsReliefsRequest

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeletePensionsReliefsConnector @Inject() (val http: HttpClient, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def delete(request: DeletePensionsReliefsRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[Unit]] = {

    import request.nino.nino
    import request.taxYear

    val downstreamUri =
      if (taxYear.useTaxYearSpecificApi) {
        TaxYearSpecificIfsUri[Unit](s"income-tax/reliefs/pensions/${taxYear.asTysDownstream}/$nino")
      } else {
        DesUri[Unit](s"income-tax/reliefs/pensions/$nino/${taxYear.asDownstream}")
      }

    delete(
      uri = downstreamUri
    )

  }

}
