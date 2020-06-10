/*
 * Copyright 2020 HM Revenue & Customs
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

package v1.hateoas

import config.AppConfig
import v1.models.hateoas.Link
import v1.models.hateoas.Method._
import v1.models.hateoas.RelType._

trait HateoasLinks {

  //Domain URIs
  private def sampleUri(appConfig: AppConfig, nino: String): String =
    s"/${appConfig.apiGatewayContext}/$nino/sample-endpoint"

  private def reliefInvestmentsUri(appConfig: AppConfig, nino: String, taxYear: String): String =
    s"/${appConfig.apiGatewayContext}/investment/$nino/$taxYear"

  //API resource links
  def sampleLink(appConfig: AppConfig, nino: String): Link =
    Link(href = sampleUri(appConfig, nino), method = GET, rel = SAMPLE_ENDPOINT_REL)

  def retrieveReliefInvestments(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(href = reliefInvestmentsUri(appConfig, nino, taxYear), method = GET, rel = SELF)

  def amendReliefInvestments(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(href = reliefInvestmentsUri(appConfig, nino, taxYear), method = PUT, rel = AMEND_RELIEF_INVESTMENTS)

  def deleteReliefInvestments(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(href = reliefInvestmentsUri(appConfig, nino, taxYear), method = DELETE, rel = DELETE_RELIEF_INVESTMENTS)

  def retrieveOtherReliefs(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(href = reliefInvestmentsUri(appConfig, nino, taxYear), method = GET, rel = SELF)

  def amendOtherReliefs(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(href = reliefInvestmentsUri(appConfig, nino, taxYear), method = PUT, rel = AMEND_OTHER_RELIEFS)

  def deleteOtherReliefs(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(href = reliefInvestmentsUri(appConfig, nino, taxYear), method = DELETE, rel = DELETE_OTHER_RELIEFS)
}
