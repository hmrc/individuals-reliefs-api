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

package v1.models.response.retrieveForeignReliefs

import config.AppConfig
import play.api.libs.json.{Json, OFormat}
import v1.hateoas.{HateoasLinks, HateoasLinksFactory}
import v1.models.hateoas.{HateoasData, Link}

case class RetrieveForeignReliefsResponse(
                                           submittedOn: String,
                                           foreignTaxCreditRelief: Option[ForeignTaxCreditRelief],
                                           foreignIncomeTaxCreditRelief: Option[Seq[ForeignIncomeTaxCreditRelief]],
                                           foreignTaxForFtcrNotClaimed: Option[ForeignTaxForFtcrNotClaimed]
                                         )


object RetrieveForeignReliefsResponse extends HateoasLinks {
  implicit val format: OFormat[RetrieveForeignReliefsResponse] = Json.format[RetrieveForeignReliefsResponse]

  implicit object LinksFactory extends HateoasLinksFactory[RetrieveForeignReliefsResponse, RetrieveForeignReliefsHateoasData] {
    override def links(appConfig: AppConfig, data: RetrieveForeignReliefsHateoasData): Seq[Link] = {
      import data._
      Seq(
        retrieveForeignReliefs(appConfig, nino, taxYear),
        amendForeignReliefs(appConfig, nino, taxYear),
        deleteForeignReliefs(appConfig, nino, taxYear)
      )
    }
  }

}


case class RetrieveForeignReliefsHateoasData(nino: String, taxYear: String) extends HateoasData
