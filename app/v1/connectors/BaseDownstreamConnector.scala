/*
 * Copyright 2021 HM Revenue & Customs
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
import play.api.libs.json.Writes
import uk.gov.hmrc.http.logging.Authorization
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads}
import uk.gov.hmrc.http.HttpClient
import utils.Logging

import scala.concurrent.{ExecutionContext, Future}

trait BaseDownstreamConnector extends Logging {
  val http: HttpClient
  val appConfig: AppConfig

  lazy val downstreamService: DownstreamService = if (appConfig.ifsEnabled) {
    DownstreamService(appConfig.ifsBaseUrl, appConfig.ifsEnv, appConfig.ifsToken)
  } else {
    DownstreamService(appConfig.desBaseUrl, appConfig.desEnv, appConfig.desToken)
  }

  private[connectors] def desHeaderCarrier(implicit hc: HeaderCarrier, correlationId: String): HeaderCarrier =
    hc.copy(authorization = Some(Authorization(s"Bearer ${downstreamService.token}")))
      .withExtraHeaders("Environment" -> downstreamService.environment, "CorrelationId" -> correlationId)

  def post[Body: Writes, Resp](body: Body, uri: DownstreamUri[Resp])(implicit ec: ExecutionContext,
                                                                     hc: HeaderCarrier,
                                                                     httpReads: HttpReads[DesOutcome[Resp]],
                                                                     correlationId: String): Future[DesOutcome[Resp]] = {

    def doPost(implicit hc: HeaderCarrier): Future[DesOutcome[Resp]] = {
      http.POST(s"${downstreamService.baseUrl}/${uri.value}", body)
    }

    doPost(desHeaderCarrier(hc, correlationId))
  }

  def put[Body: Writes, Resp](body: Body, uri: DownstreamUri[Resp])(implicit ec: ExecutionContext,
                                                                    hc: HeaderCarrier,
                                                                    httpReads: HttpReads[DesOutcome[Resp]],
                                                                    correlationId: String): Future[DesOutcome[Resp]] = {

    def doPut(implicit hc: HeaderCarrier): Future[DesOutcome[Resp]] = {
      http.PUT(s"${downstreamService.baseUrl}/${uri.value}", body)
    }

    doPut(desHeaderCarrier(hc, correlationId))
  }

  def get[Resp](uri: DownstreamUri[Resp])(implicit ec: ExecutionContext,
                                          hc: HeaderCarrier,
                                          httpReads: HttpReads[DesOutcome[Resp]],
                                          correlationId: String): Future[DesOutcome[Resp]] = {

    def doGet(implicit hc: HeaderCarrier): Future[DesOutcome[Resp]] =
      http.GET(s"${downstreamService.baseUrl}/${uri.value}")

    doGet(desHeaderCarrier(hc, correlationId))
  }

  def delete[Resp](uri: DownstreamUri[Resp])(implicit ec: ExecutionContext,
                                             hc: HeaderCarrier,
                                             httpReads: HttpReads[DesOutcome[Resp]],
                                             correlationId: String): Future[DesOutcome[Resp]] = {

    def doDelete(implicit hc: HeaderCarrier): Future[DesOutcome[Resp]] =
      http.DELETE(s"${downstreamService.baseUrl}/${uri.value}")

    doDelete(desHeaderCarrier(hc, correlationId))
  }

}
