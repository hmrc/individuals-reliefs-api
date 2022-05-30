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

package v1.hateoas

import config.AppConfig
import v1.models.hateoas.Link
import v1.models.hateoas.Method._
import v1.models.hateoas.RelType._

trait HateoasLinks {

  // Domain URIs
  private def reliefInvestmentsUri(appConfig: AppConfig, nino: String, taxYear: String): String =
    s"/${appConfig.apiGatewayContext}/investment/$nino/$taxYear"

  private def otherReliefsUri(appConfig: AppConfig, nino: String, taxYear: String): String =
    s"/${appConfig.apiGatewayContext}/other/$nino/$taxYear"

  private def foreignReliefsUri(appConfig: AppConfig, nino: String, taxYear: String): String =
    s"/${appConfig.apiGatewayContext}/foreign/$nino/$taxYear"

  private def pensionsReliefsUri(appConfig: AppConfig, nino: String, taxYear: String): String =
    s"/${appConfig.apiGatewayContext}/pensions/$nino/$taxYear"

  private def charitableGivingTaxReliefsUri(appConfig: AppConfig, nino: String, taxYear: String): String =
    s"/${appConfig.apiGatewayContext}/charitable-giving/$nino/$taxYear"

  // API resource links
  def retrieveReliefInvestments(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(href = reliefInvestmentsUri(appConfig, nino, taxYear), method = GET, rel = SELF)

  def amendReliefInvestments(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(href = reliefInvestmentsUri(appConfig, nino, taxYear), method = PUT, rel = AMEND_RELIEF_INVESTMENTS)

  def deleteReliefInvestments(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(href = reliefInvestmentsUri(appConfig, nino, taxYear), method = DELETE, rel = DELETE_RELIEF_INVESTMENTS)

  def retrieveOtherReliefs(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(href = otherReliefsUri(appConfig, nino, taxYear), method = GET, rel = SELF)

  def amendOtherReliefs(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(href = otherReliefsUri(appConfig, nino, taxYear), method = PUT, rel = AMEND_RELIEFS_OTHER)

  def deleteOtherReliefs(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(href = otherReliefsUri(appConfig, nino, taxYear), method = DELETE, rel = DELETE_RELIEFS_OTHER)

  def retrieveForeignReliefs(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(href = foreignReliefsUri(appConfig, nino, taxYear), method = GET, rel = SELF)

  def amendForeignReliefs(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(href = foreignReliefsUri(appConfig, nino, taxYear), method = PUT, rel = AMEND_RELIEFS_FOREIGN)

  def deleteForeignReliefs(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(href = foreignReliefsUri(appConfig, nino, taxYear), method = DELETE, rel = DELETE_RELIEFS_FOREIGN)

  def retrievePensionsReliefs(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(href = pensionsReliefsUri(appConfig, nino, taxYear), method = GET, rel = SELF)

  def amendPensionsReliefs(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(href = pensionsReliefsUri(appConfig, nino, taxYear), method = PUT, rel = AMEND_RELIEFS_PENSIONS)

  def deletePensionsReliefs(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(href = pensionsReliefsUri(appConfig, nino, taxYear), method = DELETE, rel = DELETE_RELIEFS_PENSIONS)

  def retrieveCharitableGivingTaxRelief(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(href = charitableGivingTaxReliefsUri(appConfig, nino, taxYear), method = GET, rel = SELF)

  def createAndAmendCharitableGivingTaxRelief(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(href = charitableGivingTaxReliefsUri(appConfig, nino, taxYear), method = PUT, rel = CREATE_AMEND_CHARITABLE_GIVING_TAX_RELIEF)

  def deleteCharitableGivingTaxRelief(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(href = charitableGivingTaxReliefsUri(appConfig, nino, taxYear), method = DELETE, rel = DELETE_CHARITABLE_GIVING_TAX_RELIEF)

}
