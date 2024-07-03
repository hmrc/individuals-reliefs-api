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

package v1.RetrieveCharitableGivingReliefs.model.response

import api.hateoas.{HateoasData, HateoasLinks, HateoasLinksFactory, Link}
import config.AppConfig
import play.api.libs.json.{Json, OWrites}
import v1.RetrieveCharitableGivingReliefs.def1.model.response.Def1_RetrieveCharitableGivingReliefsResponse
import v1.RetrieveCharitableGivingReliefs.def1.model.response.Def1_RetrieveCharitableGivingReliefsResponse.Def1_RetrieveCharitableGivingReliefsLinksFactory

trait RetrieveCharitableGivingReliefsResponse {
  def retrieveCharitableGivingReliefResponse: RetrieveCharitableGivingReliefsResponse
}

object RetrieveCharitableGivingReliefsResponse extends HateoasLinks {

  implicit val writes: OWrites[RetrieveCharitableGivingReliefsResponse] = {
    case def1: Def1_RetrieveCharitableGivingReliefsResponse =>
      Json.toJsObject(def1)
    case _ => throw new IllegalArgumentException("Request type is not known")
  }

  implicit object LinksFactory extends HateoasLinksFactory[RetrieveCharitableGivingReliefsResponse, RetrieveCharitableGivingReliefsHateoasData] {

    override def links(appConfig: AppConfig, data: RetrieveCharitableGivingReliefsHateoasData): Seq[Link] = {
      data.taxYear match {
        case _ => Def1_RetrieveCharitableGivingReliefsLinksFactory.links(appConfig, data)
      }
    }

  }

}

case class RetrieveCharitableGivingReliefsHateoasData(nino: String, taxYear: String) extends HateoasData
