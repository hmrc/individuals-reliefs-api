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

package v1.retrieveCharitableGivingReliefs.model.response

import api.hateoas.{HateoasData, HateoasLinks, HateoasLinksFactory, Link}
import api.models.domain.TaxYear
import config.AppConfig
import play.api.libs.json.OWrites
import shared.utils.JsonWritesUtil
import v1.retrieveCharitableGivingReliefs.def1.model.response.Def1_RetrieveCharitableGivingReliefsResponse
import v1.retrieveCharitableGivingReliefs.def1.model.response.Def1_RetrieveCharitableGivingReliefsResponse.Def1_RetrieveCharitableGivingReliefsLinksFactory
import v1.retrieveCharitableGivingReliefs.def2.model.response.Def2_RetrieveCharitableGivingReliefsResponse
import v1.retrieveCharitableGivingReliefs.def2.model.response.Def2_RetrieveCharitableGivingReliefsResponse.Def2_RetrieveCharitableGivingReliefsLinksFactory

trait RetrieveCharitableGivingReliefsResponse {
  def retrieveCharitableGivingReliefResponse: RetrieveCharitableGivingReliefsResponse
}

object RetrieveCharitableGivingReliefsResponse extends HateoasLinks {

  object RetrieveCharitableGivingReliefsResponse extends JsonWritesUtil {

    implicit val writes: OWrites[RetrieveCharitableGivingReliefsResponse] = writesFrom {
      case def1: Def1_RetrieveCharitableGivingReliefsResponse =>
        implicitly[OWrites[Def1_RetrieveCharitableGivingReliefsResponse]].writes(def1)
      case def2: Def2_RetrieveCharitableGivingReliefsResponse =>
        implicitly[OWrites[Def2_RetrieveCharitableGivingReliefsResponse]].writes(def2)
    }

    implicit object LinksFactory extends HateoasLinksFactory[RetrieveCharitableGivingReliefsResponse, RetrieveCharitableGivingReliefsHateoasData] {

      override def links(appConfig: AppConfig, data: RetrieveCharitableGivingReliefsHateoasData): Seq[Link] = {

        val ifsEnabled = appConfig.featureSwitches.getOptional[Boolean]("desIf_Migration").getOrElse(true)

        data.taxYear match {
          case taxYear if (TaxYear(taxYear).year > 2023) =>
            Def2_RetrieveCharitableGivingReliefsLinksFactory.links(appConfig, data)
          case _ =>
            if (ifsEnabled) { Def2_RetrieveCharitableGivingReliefsLinksFactory.links(appConfig, data) }
            else { Def1_RetrieveCharitableGivingReliefsLinksFactory.links(appConfig, data) }
        }
      }

    }

  }

}

case class RetrieveCharitableGivingReliefsHateoasData(nino: String, taxYear: String) extends HateoasData
