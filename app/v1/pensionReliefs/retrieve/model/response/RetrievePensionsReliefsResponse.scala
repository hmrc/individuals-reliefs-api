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

package v1.pensionReliefs.retrieve.model.response

import api.hateoas.{HateoasData, HateoasLinks, HateoasLinksFactory, Link}
import config.AppConfig
import play.api.libs.json.OWrites
import shared.utils.JsonWritesUtil.writesFrom
import v1.pensionReliefs.retrieve.def1.model.response.Def1_RetrievePensionsReliefsResponse

trait RetrievePensionsReliefsResponse

object RetrievePensionsReliefsResponse extends HateoasLinks {

  implicit val writes: OWrites[RetrievePensionsReliefsResponse] = writesFrom { case def1: Def1_RetrievePensionsReliefsResponse =>
    implicitly[OWrites[Def1_RetrievePensionsReliefsResponse]].writes(def1)
  }

  implicit object LinksFactory extends HateoasLinksFactory[RetrievePensionsReliefsResponse, RetrievePensionsReliefsHateoasData] {

    override def links(appConfig: AppConfig, data: RetrievePensionsReliefsHateoasData): Seq[Link] = {
      import data._
      Seq(
        amendPensionsReliefs(appConfig, nino, taxYear),
        retrievePensionsReliefs(appConfig, nino, taxYear),
        deletePensionsReliefs(appConfig, nino, taxYear)
      )
    }

  }

}

case class RetrievePensionsReliefsHateoasData(nino: String, taxYear: String) extends HateoasData
