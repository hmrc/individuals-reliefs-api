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

package v2.reliefInvestments.createAmend.model.response

import hateoas.HateoasLinks
import shared.config.SharedAppConfig
import shared.hateoas.{HateoasData, HateoasLinksFactory, Link}

object CreateAndAmendReliefInvestmentsResponse extends HateoasLinks {

  implicit object LinksFactory extends HateoasLinksFactory[Unit, CreateAndAmendReliefInvestmentsHateoasData] {

    override def links(appConfig: SharedAppConfig, data: CreateAndAmendReliefInvestmentsHateoasData): Seq[Link] = {
      import data._
      Seq(
        retrieveReliefInvestments(appConfig, nino, taxYear),
        createAndAmendReliefInvestments(appConfig, nino, taxYear),
        deleteReliefInvestments(appConfig, nino, taxYear)
      )
    }

  }

}

case class CreateAndAmendReliefInvestmentsHateoasData(nino: String, taxYear: String) extends HateoasData
