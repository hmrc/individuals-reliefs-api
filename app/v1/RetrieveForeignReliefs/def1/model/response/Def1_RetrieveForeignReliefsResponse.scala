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

package v1.RetrieveForeignReliefs.def1.model.response

import api.hateoas.{HateoasLinks, HateoasLinksFactory, Link}
import api.models.domain.Timestamp
import config.AppConfig
import play.api.libs.json.{Json, OWrites, Reads}
import v1.RetrieveForeignReliefs.model.response.{RetrieveForeignReliefsHateoasData, RetrieveForeignReliefsResponse}

case class Def1_RetrieveForeignReliefsResponse(
    submittedOn: Timestamp,
    foreignTaxCreditRelief: Option[Def1_ForeignTaxCreditRelief],
    foreignIncomeTaxCreditRelief: Option[Seq[Def1_ForeignIncomeTaxCreditRelief]],
    foreignTaxForFtcrNotClaimed: Option[Def1_ForeignTaxForFtcrNotClaimed]
) extends RetrieveForeignReliefsResponse

object Def1_RetrieveForeignReliefsResponse extends HateoasLinks {

  implicit val reads: Reads[Def1_RetrieveForeignReliefsResponse]    = Json.reads[Def1_RetrieveForeignReliefsResponse]
  implicit val writes: OWrites[Def1_RetrieveForeignReliefsResponse] = Json.writes[Def1_RetrieveForeignReliefsResponse]

  implicit object Def1_RetrieveForeignReliefsLinksFactory
      extends HateoasLinksFactory[Def1_RetrieveForeignReliefsResponse, RetrieveForeignReliefsHateoasData] {

    override def links(appConfig: AppConfig, data: RetrieveForeignReliefsHateoasData): Seq[Link] = {
      import data._

      Seq(
        retrieveForeignReliefs(appConfig, nino, taxYear),
        createAndAmendForeignReliefs(appConfig, nino, taxYear),
        deleteForeignReliefs(appConfig, nino, taxYear)
      )
    }

  }

}
