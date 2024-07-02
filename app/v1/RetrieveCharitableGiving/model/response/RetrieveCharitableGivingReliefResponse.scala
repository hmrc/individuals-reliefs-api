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

package v1.RetrieveCharitableGiving.model.response

import api.hateoas.{HateoasData, HateoasLinks, HateoasLinksFactory, Link}
import config.AppConfig
import play.api.libs.json.{Json, OWrites}
import v1.RetrieveCharitableGiving.def1.model.response.Def1_RetrieveCharitableGivingReliefResponse
import v1.RetrieveCharitableGiving.def1.model.response.Def1_RetrieveCharitableGivingReliefResponse.Def1_RetrieveCharitableGivingReliefLinksFactory

trait RetrieveCharitableGivingReliefResponse {
  def retrieveCharitableGivingReliefResponse: RetrieveCharitableGivingReliefResponse
}

object RetrieveCharitableGivingReliefResponse extends HateoasLinks {

  implicit val writes: OWrites[RetrieveCharitableGivingReliefResponse] = {
    case def1: Def1_RetrieveCharitableGivingReliefResponse =>
      Json.toJsObject(def1)
    case _ => throw new IllegalArgumentException("Request type is not known")
  }

  implicit object LinksFactory extends HateoasLinksFactory[RetrieveCharitableGivingReliefResponse, RetrieveCharitableGivingReliefHateoasData] {

    override def links(appConfig: AppConfig, data: RetrieveCharitableGivingReliefHateoasData): Seq[Link] = {
      data.taxYear match {
        case _ => Def1_RetrieveCharitableGivingReliefLinksFactory.links(appConfig, data)
      }
    }

  }

}

case class RetrieveCharitableGivingReliefHateoasData(nino: String, taxYear: String) extends HateoasData
