/*
 * Copyright 2025 HM Revenue & Customs
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

package v3.reliefInvestments.createAmend

import shared.controllers.validators.resolvers.ResolveTaxYear
import shared.models.domain.TaxYear
import scala.math.Ordered.orderingToOrdered

sealed trait CreateAndAmendReliefInvestmentsSchema

object CreateAndAmendReliefInvestmentsSchema {
  case object Def1 extends CreateAndAmendReliefInvestmentsSchema
  case object Def2 extends CreateAndAmendReliefInvestmentsSchema

  private val latestSchema = Def2

  def schemaFor(maybeTaxYear: Option[String]): CreateAndAmendReliefInvestmentsSchema = {
    maybeTaxYear
      .map(ResolveTaxYear.apply)
      .flatMap(_.toOption.map(schemaFor))
      .getOrElse(latestSchema)
  }

  def schemaFor(taxYear: TaxYear): CreateAndAmendReliefInvestmentsSchema = {
    if (taxYear <= TaxYear.ending(2025)) Def1
    else if (taxYear >= TaxYear.ending(2026)) Def2
    else latestSchema

  }

}
