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

package v2.retrieveForeignReliefs.model.response

import hateoas.HateoasLinks
import play.api.libs.json.{Json, OWrites, Reads}
import shared.config.SharedAppConfig
import shared.hateoas.{HateoasData, HateoasLinksFactory, Link}
import shared.models.domain.Timestamp
import v2.retrieveForeignReliefs.def1.model.response.{
  Def1_ForeignIncomeTaxCreditRelief,
  Def1_ForeignTaxCreditRelief,
  Def1_ForeignTaxForFtcrNotClaimed
}
import v2.retrieveForeignReliefs.model.response.Def1_RetrieveForeignReliefsResponse.Def1_RetrieveForeignReliefsLinksFactory

sealed trait RetrieveForeignReliefsResponse

object RetrieveForeignReliefsResponse extends HateoasLinks {

  implicit val writes: OWrites[RetrieveForeignReliefsResponse] = OWrites[RetrieveForeignReliefsResponse] {
    case def1: Def1_RetrieveForeignReliefsResponse => Json.toJsObject(def1)
  }

  implicit object LinksFactory extends HateoasLinksFactory[RetrieveForeignReliefsResponse, RetrieveForeignReliefsHateoasData] {

    override def links(appConfig: SharedAppConfig, data: RetrieveForeignReliefsHateoasData): Seq[Link] = {
      data.taxYear match {
        case _ => Def1_RetrieveForeignReliefsLinksFactory.links(appConfig, data)
      }
    }

  }

}

case class RetrieveForeignReliefsHateoasData(nino: String, taxYear: String) extends HateoasData

case class Def1_RetrieveForeignReliefsResponse(
    submittedOn: Timestamp,
    foreignTaxCreditRelief: Option[Def1_ForeignTaxCreditRelief],
    foreignIncomeTaxCreditRelief: Option[Seq[Def1_ForeignIncomeTaxCreditRelief]],
    foreignTaxForFtcrNotClaimed: Option[Def1_ForeignTaxForFtcrNotClaimed]
) extends RetrieveForeignReliefsResponse {
  implicit val reads: Reads[Def1_RetrieveForeignReliefsResponse] = Json.reads[Def1_RetrieveForeignReliefsResponse]
}

object Def1_RetrieveForeignReliefsResponse extends HateoasLinks {

  implicit val reads: Reads[Def1_RetrieveForeignReliefsResponse]    = Json.reads[Def1_RetrieveForeignReliefsResponse]
  implicit val writes: OWrites[Def1_RetrieveForeignReliefsResponse] = Json.writes[Def1_RetrieveForeignReliefsResponse]

  implicit object Def1_RetrieveForeignReliefsLinksFactory
      extends HateoasLinksFactory[Def1_RetrieveForeignReliefsResponse, RetrieveForeignReliefsHateoasData] {

    override def links(appConfig: SharedAppConfig, data: RetrieveForeignReliefsHateoasData): Seq[Link] = {
      import data._

      Seq(
        retrieveForeignReliefs(appConfig, nino, taxYear),
        createAndAmendForeignReliefs(appConfig, nino, taxYear),
        deleteForeignReliefs(appConfig, nino, taxYear)
      )
    }

  }

}
