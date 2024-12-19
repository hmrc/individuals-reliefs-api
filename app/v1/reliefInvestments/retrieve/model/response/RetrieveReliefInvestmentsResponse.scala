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

package v1.reliefInvestments.retrieve.model.response

import hateoas.HateoasLinks
import play.api.libs.json.OWrites
import shared.config.SharedAppConfig
import shared.hateoas.{HateoasData, HateoasLinksFactory, Link}
import shared.utils.JsonWritesUtil.writesFrom
import v1.reliefInvestments.retrieve.def1.model.response.Def1_RetrieveReliefInvestmentsResponse

trait RetrieveReliefInvestmentsResponse

object RetrieveReliefInvestmentsResponse extends HateoasLinks {

  implicit val writes: OWrites[RetrieveReliefInvestmentsResponse] = writesFrom { case def1: Def1_RetrieveReliefInvestmentsResponse =>
    implicitly[OWrites[Def1_RetrieveReliefInvestmentsResponse]].writes(def1)
  }

  implicit object LinksFactory extends HateoasLinksFactory[RetrieveReliefInvestmentsResponse, RetrieveReliefInvestmentsHateoasData] {

    override def links(appConfig: SharedAppConfig, data: RetrieveReliefInvestmentsHateoasData): Seq[Link] = {
      import data._
      Seq(
        retrieveReliefInvestments(appConfig, nino, taxYear),
        createAndAmendReliefInvestments(appConfig, nino, taxYear),
        deleteReliefInvestments(appConfig, nino, taxYear)
      )
    }

  }

}

case class RetrieveReliefInvestmentsHateoasData(nino: String, taxYear: String) extends HateoasData

