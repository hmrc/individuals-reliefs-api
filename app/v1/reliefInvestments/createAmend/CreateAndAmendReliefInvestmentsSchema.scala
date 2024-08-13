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

package v1.reliefInvestments.createAmend

import api.controllers.validators.resolvers.ResolveTaxYear
import api.models.domain.TaxYear

sealed trait CreateAndAmendReliefInvestmentsSchema

object CreateAndAmendReliefInvestmentsSchema {
  case object Def1 extends CreateAndAmendReliefInvestmentsSchema

  private val defaultSchema = Def1

  def schemaFor(maybeTaxYear: Option[String]): CreateAndAmendReliefInvestmentsSchema = {
    maybeTaxYear
      .map(ResolveTaxYear.apply)
      .flatMap(_.toOption.map(schemaFor))
      .getOrElse(defaultSchema)
  }

  def schemaFor(taxYear: TaxYear): CreateAndAmendReliefInvestmentsSchema = {
    taxYear match {
      case _ => Def1
    }

  }
}
