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

package v1.retrieveCharitableGivingReliefs.model.response

import hateoas.HateoasLinks
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import shared.config.SharedAppConfig
import shared.hateoas.{HateoasData, HateoasLinksFactory, Link}
import v1.retrieveCharitableGivingReliefs.def1.model.response.{Def1_GiftAidPayments, Def1_Gifts}
import v1.retrieveCharitableGivingReliefs.model.response.Def1_RetrieveCharitableGivingReliefsResponse.Def1_RetrieveCharitableGivingReliefsLinksFactory

sealed trait RetrieveCharitableGivingReliefsResponse {
  def retrieveCharitableGivingReliefResponse: RetrieveCharitableGivingReliefsResponse
}

object RetrieveCharitableGivingReliefsResponse extends HateoasLinks {

  implicit val writes: OWrites[RetrieveCharitableGivingReliefsResponse] = { case def1: Def1_RetrieveCharitableGivingReliefsResponse =>
    Json.toJsObject(def1)
  }

  implicit object LinksFactory extends HateoasLinksFactory[RetrieveCharitableGivingReliefsResponse, RetrieveCharitableGivingReliefsHateoasData] {

    override def links(appConfig: SharedAppConfig, data: RetrieveCharitableGivingReliefsHateoasData): Seq[Link] = {
      data.taxYear match {
        case _ => Def1_RetrieveCharitableGivingReliefsLinksFactory.links(appConfig, data)
      }
    }

  }

}

case class RetrieveCharitableGivingReliefsHateoasData(nino: String, taxYear: String) extends HateoasData

case class Def1_RetrieveCharitableGivingReliefsResponse(giftAidPayments: Option[Def1_GiftAidPayments], gifts: Option[Def1_Gifts])
    extends RetrieveCharitableGivingReliefsResponse {

  implicit val reads: Reads[Def1_RetrieveCharitableGivingReliefsResponse]                  = Json.reads[Def1_RetrieveCharitableGivingReliefsResponse]
  def retrieveCharitableGivingReliefResponse: Def1_RetrieveCharitableGivingReliefsResponse = this
}

object Def1_RetrieveCharitableGivingReliefsResponse extends HateoasLinks {
  implicit val writes: OWrites[Def1_RetrieveCharitableGivingReliefsResponse] = Json.writes

  implicit val reads: Reads[Def1_RetrieveCharitableGivingReliefsResponse] = {
    val defaultReads: Reads[Def1_RetrieveCharitableGivingReliefsResponse] = Json.reads

    val ifsReads = (JsPath \ "charitableGivingAnnual").read(defaultReads)

    ifsReads orElse defaultReads
  }

  implicit object Def1_RetrieveCharitableGivingReliefsLinksFactory
      extends HateoasLinksFactory[Def1_RetrieveCharitableGivingReliefsResponse, RetrieveCharitableGivingReliefsHateoasData] {

    override def links(appConfig: SharedAppConfig, data: RetrieveCharitableGivingReliefsHateoasData): Seq[Link] = {
      import data._
      Seq(
        createAndAmendCharitableGivingTaxRelief(appConfig, nino, taxYear),
        retrieveCharitableGivingTaxRelief(appConfig, nino, taxYear),
        deleteCharitableGivingTaxRelief(appConfig, nino, taxYear)
      )
    }

  }

}
