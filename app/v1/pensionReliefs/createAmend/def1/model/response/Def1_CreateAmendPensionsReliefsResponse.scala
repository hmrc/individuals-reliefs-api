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

package v1.pensionReliefs.createAmend.def1.model.response

import api.hateoas.{HateoasData, HateoasLinks, HateoasLinksFactory, Link}
import config.AppConfig
import play.api.libs.json._
import v1.pensionReliefs.createAmend.model.response.CreateAmendPensionsReliefsResponse

trait Def1_CreateAmendPensionsReliefsResponse extends CreateAmendPensionsReliefsResponse

object Def1_CreateAmendPensionsReliefsResponse extends HateoasLinks {

  implicit val reads: Reads[Def1_CreateAmendPensionsReliefsResponse] = (_: JsValue) => {
    JsSuccess(new Def1_CreateAmendPensionsReliefsResponse {})
  }

  implicit val writes: OWrites[Def1_CreateAmendPensionsReliefsResponse] = (_: Def1_CreateAmendPensionsReliefsResponse) => {
    Json.obj()
  }

  implicit object LinksFactory extends HateoasLinksFactory[Unit, CreateAmendPensionsReliefsHateoasData] {

    override def links(appConfig: AppConfig, data: CreateAmendPensionsReliefsHateoasData): Seq[Link] = {
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
