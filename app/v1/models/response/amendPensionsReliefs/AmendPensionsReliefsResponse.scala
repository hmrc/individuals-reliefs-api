/*
 * Copyright 2020 HM Revenue & Customs
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

package v1.models.response.amendPensionsReliefs

import config.AppConfig
import v1.hateoas.{HateoasLinks, HateoasLinksFactory}
import v1.models.hateoas.{HateoasData, Link}

object AmendPensionsReliefsResponse extends HateoasLinks {

  implicit object AmendOrderLinksFactory extends HateoasLinksFactory[Unit, AmendPensionsReliefsHateoasData] {
    override def links(appConfig: AppConfig, data: AmendPensionsReliefsHateoasData): Seq[Link] = {
      import data._
      Seq(
        retrievePensionsReliefs(appConfig, nino, taxYear),
        amendPensionsReliefs(appConfig, nino, taxYear),
        deletePensionsReliefs(appConfig, nino, taxYear)
      )
    }
  }
}

case class AmendPensionsReliefsHateoasData(nino: String, taxYear: String) extends HateoasData
