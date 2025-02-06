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

package v2.pensionReliefs.createAmend.model.response

import play.api.libs.json.OWrites
import shared.config.SharedAppConfig
import shared.hateoas.{HateoasData, HateoasLinksFactory, Link}
import shared.utils.JsonWritesUtil
import v2.pensionReliefs.createAmend.def1.model.response.Def1_CreateAmendPensionsReliefsResponse
import v2.pensionReliefs.createAmend.def1.model.response.Def1_CreateAmendPensionsReliefsResponse.{
  amendPensionsReliefs,
  deletePensionsReliefs,
  retrievePensionsReliefs
}

trait CreateAmendPensionsReliefsResponse

object CreateAmendPensionsReliefsResponse extends JsonWritesUtil {

  implicit val writes: OWrites[CreateAmendPensionsReliefsResponse] = writesFrom { case def1: Def1_CreateAmendPensionsReliefsResponse =>
    implicitly[OWrites[Def1_CreateAmendPensionsReliefsResponse]].writes(def1)
  }

  implicit object LinksFactory extends HateoasLinksFactory[Unit, CreateAmendPensionsReliefsHateoasData] {

    override def links(appConfig: SharedAppConfig, data: CreateAmendPensionsReliefsHateoasData): Seq[Link] = {
      import data._
      Seq(
        retrievePensionsReliefs(appConfig, nino, taxYear),
        amendPensionsReliefs(appConfig, nino, taxYear),
        deletePensionsReliefs(appConfig, nino, taxYear)
      )
    }

  }

}

case class CreateAmendPensionsReliefsHateoasData(nino: String, taxYear: String) extends HateoasData
