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

import cats.data.Validated
import cats.data.Validated.Valid
import shared.controllers.validators.resolvers.ResolveTaxYearMinimum
import shared.models.domain.TaxYear
import shared.models.errors.MtdError

import scala.math.Ordered.orderingToOrdered

sealed trait CreateAndAmendReliefInvestmentsSchema

object CreateAndAmendReliefInvestmentsSchema {
  case object Def1 extends CreateAndAmendReliefInvestmentsSchema
  case object Def2 extends CreateAndAmendReliefInvestmentsSchema

  def schemaFor(taxYearString: String): Validated[Seq[MtdError], CreateAndAmendReliefInvestmentsSchema] = {
    ResolveTaxYearMinimum(TaxYear.fromMtd("2020-21"))(taxYearString) andThen schemaFor
  }

  def schemaFor(taxYear: TaxYear): Validated[Seq[MtdError], CreateAndAmendReliefInvestmentsSchema] = {
    if (taxYear >= TaxYear.fromMtd("2025-26")) Valid(Def2) else Valid(Def1)

  }

}
