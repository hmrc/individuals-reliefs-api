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

package v1.retrieveCharitableGivingReliefs.def2.model.response

import api.hateoas.{HateoasLinks, HateoasLinksFactory, Link}
import config.AppConfig
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json._
import v1.retrieveCharitableGivingReliefs.model.response.{RetrieveCharitableGivingReliefsHateoasData, RetrieveCharitableGivingReliefsResponse}

case class Def2_RetrieveCharitableGivingReliefsResponse(giftAidPayments: Option[Def2_GiftAidPayments], gifts: Option[Def2_Gifts])
    extends RetrieveCharitableGivingReliefsResponse {

  def retrieveCharitableGivingReliefResponse: Def2_RetrieveCharitableGivingReliefsResponse = this
}

object Def2_RetrieveCharitableGivingReliefsResponse extends HateoasLinks {
  implicit val formats: OFormat[Def2_RetrieveCharitableGivingReliefsResponse] = Json.format[Def2_RetrieveCharitableGivingReliefsResponse]

  implicit val reads: Reads[Def2_RetrieveCharitableGivingReliefsResponse] = (
    (JsPath \ "charitableGivingAnnual" \ "giftAidPayments").readNullable[Def2_GiftAidPayments] and
      (JsPath \ "charitableGivingAnnual" \ "gifts").readNullable[Def2_Gifts]
  )(Def2_RetrieveCharitableGivingReliefsResponse.apply _)

  implicit object Def2_RetrieveCharitableGivingReliefsLinksFactory
      extends HateoasLinksFactory[Def2_RetrieveCharitableGivingReliefsResponse, RetrieveCharitableGivingReliefsHateoasData] {

    override def links(appConfig: AppConfig, data: RetrieveCharitableGivingReliefsHateoasData): Seq[Link] = {
      import data._
      Seq(
        createAndAmendCharitableGivingTaxRelief(appConfig, nino, taxYear),
        retrieveCharitableGivingTaxRelief(appConfig, nino, taxYear),
        deleteCharitableGivingTaxRelief(appConfig, nino, taxYear)
      )
    }

  }

}
