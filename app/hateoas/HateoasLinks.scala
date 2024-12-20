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

package hateoas

import hateoas.RelType._
import shared.config.SharedAppConfig
import shared.hateoas.Link
import shared.hateoas.Method._

trait HateoasLinks {

  // Domain URIs
  private def reliefInvestmentsUri(appConfig: SharedAppConfig, nino: String, taxYear: String): String =
    s"/${appConfig.apiGatewayContext}/investment/$nino/$taxYear"

  private def otherReliefsUri(appConfig: SharedAppConfig, nino: String, taxYear: String): String =
    s"/${appConfig.apiGatewayContext}/other/$nino/$taxYear"

  private def foreignReliefsUri(appConfig: SharedAppConfig, nino: String, taxYear: String): String =
    s"/${appConfig.apiGatewayContext}/foreign/$nino/$taxYear"

  private def pensionsReliefsUri(appConfig: SharedAppConfig, nino: String, taxYear: String): String =
    s"/${appConfig.apiGatewayContext}/pensions/$nino/$taxYear"

  private def charitableGivingTaxReliefsUri(appConfig: SharedAppConfig, nino: String, taxYear: String): String =
    s"/${appConfig.apiGatewayContext}/charitable-giving/$nino/$taxYear"

  // API resource links
  def retrieveReliefInvestments(appConfig: SharedAppConfig, nino: String, taxYear: String): Link =
    Link(href = reliefInvestmentsUri(appConfig, nino, taxYear), method = GET, rel = SELF)

  def createAndAmendReliefInvestments(appConfig: SharedAppConfig, nino: String, taxYear: String): Link =
    Link(href = reliefInvestmentsUri(appConfig, nino, taxYear), method = PUT, rel = CREATE_AMEND_RELIEF_INVESTMENTS)

  def deleteReliefInvestments(appConfig: SharedAppConfig, nino: String, taxYear: String): Link =
    Link(href = reliefInvestmentsUri(appConfig, nino, taxYear), method = DELETE, rel = DELETE_RELIEF_INVESTMENTS)

  def retrieveOtherReliefs(appConfig: SharedAppConfig, nino: String, taxYear: String): Link =
    Link(href = otherReliefsUri(appConfig, nino, taxYear), method = GET, rel = SELF)

  def amendOtherReliefs(appConfig: SharedAppConfig, nino: String, taxYear: String): Link =
    Link(href = otherReliefsUri(appConfig, nino, taxYear), method = PUT, rel = AMEND_RELIEFS_OTHER)

  def deleteOtherReliefs(appConfig: SharedAppConfig, nino: String, taxYear: String): Link =
    Link(href = otherReliefsUri(appConfig, nino, taxYear), method = DELETE, rel = DELETE_RELIEFS_OTHER)

  def retrieveForeignReliefs(appConfig: SharedAppConfig, nino: String, taxYear: String): Link =
    Link(href = foreignReliefsUri(appConfig, nino, taxYear), method = GET, rel = SELF)

  def createAndAmendForeignReliefs(appConfig: SharedAppConfig, nino: String, taxYear: String): Link =
    Link(href = foreignReliefsUri(appConfig, nino, taxYear), method = PUT, rel = CREATE_AMEND_RELIEFS_FOREIGN)

  def deleteForeignReliefs(appConfig: SharedAppConfig, nino: String, taxYear: String): Link =
    Link(href = foreignReliefsUri(appConfig, nino, taxYear), method = DELETE, rel = DELETE_RELIEFS_FOREIGN)

  def retrievePensionsReliefs(appConfig: SharedAppConfig, nino: String, taxYear: String): Link =
    Link(href = pensionsReliefsUri(appConfig, nino, taxYear), method = GET, rel = SELF)

  def amendPensionsReliefs(appConfig: SharedAppConfig, nino: String, taxYear: String): Link =
    Link(href = pensionsReliefsUri(appConfig, nino, taxYear), method = PUT, rel = AMEND_RELIEFS_PENSIONS)

  def deletePensionsReliefs(appConfig: SharedAppConfig, nino: String, taxYear: String): Link =
    Link(href = pensionsReliefsUri(appConfig, nino, taxYear), method = DELETE, rel = DELETE_RELIEFS_PENSIONS)

  def retrieveCharitableGivingTaxRelief(appConfig: SharedAppConfig, nino: String, taxYear: String): Link =
    Link(href = charitableGivingTaxReliefsUri(appConfig, nino, taxYear), method = GET, rel = SELF)

  def createAndAmendCharitableGivingTaxRelief(appConfig: SharedAppConfig, nino: String, taxYear: String): Link =
    Link(href = charitableGivingTaxReliefsUri(appConfig, nino, taxYear), method = PUT, rel = CREATE_AMEND_CHARITABLE_GIVING_TAX_RELIEF)

  def deleteCharitableGivingTaxRelief(appConfig: SharedAppConfig, nino: String, taxYear: String): Link =
    Link(href = charitableGivingTaxReliefsUri(appConfig, nino, taxYear), method = DELETE, rel = DELETE_CHARITABLE_GIVING_TAX_RELIEF)

}
