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

package v1.retrieveOtherReliefs.model.response

import api.hateoas.{HateoasData, HateoasLinks, HateoasLinksFactory, Link}
import config.AppConfig
import play.api.libs.json.{Json, OWrites}
import v1.retrieveOtherReliefs.def1.model.response.Def1_RetrieveOtherReliefsResponse.Def1_RetrieveOtherReliefsLinksFactory
import v1.retrieveOtherReliefs.def1.model.response._

trait RetrieveOtherReliefsResponse

object RetrieveOtherReliefsResponse extends HateoasLinks {

  implicit val writes: OWrites[RetrieveOtherReliefsResponse] = OWrites[RetrieveOtherReliefsResponse] { case def1: Def1_RetrieveOtherReliefsResponse =>
    Json.toJsObject(def1)
  }

  implicit object LinksFactory extends HateoasLinksFactory[RetrieveOtherReliefsResponse, RetrieveOtherReliefsHateoasData] {

    override def links(appConfig: AppConfig, data: RetrieveOtherReliefsHateoasData): Seq[Link] = {
      data.taxYear match {
        case _ => Def1_RetrieveOtherReliefsLinksFactory.links(appConfig, data)
      }
    }

  }

}

case class RetrieveOtherReliefsHateoasData(nino: String, taxYear: String) extends HateoasData
