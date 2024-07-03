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

package v1.RetrieveCharitableGivingReliefs.def1.model.response

import api.hateoas.{HateoasLinks, HateoasLinksFactory, Link}
import config.AppConfig
import play.api.libs.json.{Json, OFormat}
import v1.RetrieveCharitableGivingReliefs.model.response.{RetrieveCharitableGivingReliefsHateoasData, RetrieveCharitableGivingReliefsResponse}

case class Def1_RetrieveCharitableGivingReliefsResponse(giftAidPayments: Option[Def1_GiftAidPayments], gifts: Option[Def1_Gifts])
    extends RetrieveCharitableGivingReliefsResponse {

  def retrieveCharitableGivingReliefResponse: Def1_RetrieveCharitableGivingReliefsResponse = this
}

object Def1_RetrieveCharitableGivingReliefsResponse extends HateoasLinks {
  implicit val formats: OFormat[Def1_RetrieveCharitableGivingReliefsResponse] = Json.format[Def1_RetrieveCharitableGivingReliefsResponse]

  implicit object Def1_RetrieveCharitableGivingReliefsLinksFactory
      extends HateoasLinksFactory[Def1_RetrieveCharitableGivingReliefsResponse, RetrieveCharitableGivingReliefsHateoasData] {

    override def links(appConfig: AppConfig, data: RetrieveCharitableGivingReliefsHateoasData): Seq[Link] = {
      import data._
      Seq(
        createAndAmendCharitableGivingTaxRelief(appConfig, nino, taxYear),
        retrieveCharitableGivingTaxRelief(appConfig, nino, taxYear),
        deleteCharitableGivingTaxRelief(appConfig, nino, taxYear)
      )
    }
  }
}
