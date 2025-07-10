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

package v1.reliefInvestments.createAmend

import config.ReliefsFeatureSwitches
import shared.config.SharedAppConfig
import shared.connectors.DownstreamUri.{HipUri, IfsUri}
import shared.connectors.httpparsers.StandardDownstreamHttpParser._
import shared.connectors.{BaseDownstreamConnector, DownstreamOutcome, DownstreamUri}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import v1.reliefInvestments.createAmend.model.request.CreateAndAmendReliefInvestmentsRequestData

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateAndAmendReliefInvestmentsConnector @Inject() (val http: HttpClientV2, val appConfig: SharedAppConfig) extends BaseDownstreamConnector {

  def amend(request: CreateAndAmendReliefInvestmentsRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[Unit]] = {

    import request._

    lazy val downstreamUri1924: DownstreamUri[Unit] = {
      val downstreamTaxYearParam = taxYear.asTysDownstream
      if (ReliefsFeatureSwitches().isIfsHipMigration1924Enabled) {
        HipUri(
          s"itsa/income-tax/v1/$downstreamTaxYearParam/reliefs/investment/$nino"
        )
      } else {
        IfsUri(
          s"income-tax/reliefs/investment/$nino/${taxYear.asMtd}"
        )
      }
    }

    put(body, downstreamUri1924)

  }

}
