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

package v1.RetrieveForeignReliefs.model.response

import api.hateoas.{HateoasData, HateoasLinks, HateoasLinksFactory, Link}
import config.AppConfig
import play.api.libs.json.{Json, OWrites}
import v1.RetrieveForeignReliefs.def1.model.response.Def1_RetrieveForeignReliefsResponse
import v1.RetrieveForeignReliefs.def1.model.response.Def1_RetrieveForeignReliefsResponse.Def1_RetrieveForeignReliefsLinksFactory

trait RetrieveForeignReliefsResponse

object RetrieveForeignReliefsResponse extends HateoasLinks {

  implicit val writes: OWrites[RetrieveForeignReliefsResponse] = OWrites[RetrieveForeignReliefsResponse] {
    case def1: Def1_RetrieveForeignReliefsResponse => Json.toJsObject(def1)
    case _                                         => throw new IllegalArgumentException("Unknown type")
  }

  implicit object LinksFactory extends HateoasLinksFactory[RetrieveForeignReliefsResponse, RetrieveForeignReliefsHateoasData] {

    override def links(appConfig: AppConfig, data: RetrieveForeignReliefsHateoasData): Seq[Link] = {
      data.taxYear match {
        case _ => Def1_RetrieveForeignReliefsLinksFactory.links(appConfig, data)
      }
    }

  }

}

case class RetrieveForeignReliefsHateoasData(nino: String, taxYear: String) extends HateoasData
