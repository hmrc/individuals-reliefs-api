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

package v1.models.des

import config.AppConfig
import play.api.libs.json.{Json, OWrites}
import v1.hateoas.{HateoasLinks, HateoasLinksFactory}
import v1.models.hateoas.{HateoasData, Link}

case class RetrieveReliefInvestmentsResponse()

object RetrieveReliefInvestmentsResponse extends HateoasLinks {
  implicit val writes: OWrites[RetrieveReliefInvestmentsResponse] = OWrites[RetrieveReliefInvestmentsResponse](_ => Json.obj())

  implicit object RetrieveOrderLinksFactory extends HateoasLinksFactory[RetrieveReliefInvestmentsResponse, RetrieveReliefInvestmentsHateoasData] {
    override def links(appConfig: AppConfig, data: RetrieveReliefInvestmentsHateoasData): Seq[Link] = {
      import data._
      Seq(
        retrieveReliefInvestments(appConfig, nino, taxYear),
        amendReliefInvestments(appConfig, nino, taxYear),
        deleteReliefInvestments(appConfig, nino, taxYear)
      )
    }
  }
}

case class RetrieveReliefInvestmentsHateoasData(nino: String, taxYear: String) extends HateoasData
