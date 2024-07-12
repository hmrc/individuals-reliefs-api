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

package v1.AmendOtherReliefs.model.response

import api.hateoas.{HateoasData, HateoasLinks, HateoasLinksFactory, Link}
import config.AppConfig

object AmendOtherReliefsResponse extends HateoasLinks {

  implicit object LinksFactory extends HateoasLinksFactory[Unit, AmendOtherReliefsHateoasData] {

    override def links(appConfig: AppConfig, data: AmendOtherReliefsHateoasData): Seq[Link] = {
      import data._
      Seq(
        amendOtherReliefs(appConfig, nino, taxYear),
        retrieveOtherReliefs(appConfig, nino, taxYear),
        deleteOtherReliefs(appConfig, nino, taxYear)
      )
    }

  }

}

case class AmendOtherReliefsHateoasData(nino: String, taxYear: String) extends HateoasData
