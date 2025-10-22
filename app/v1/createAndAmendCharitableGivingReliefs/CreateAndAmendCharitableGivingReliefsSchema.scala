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

package v1.createAndAmendCharitableGivingReliefs

import cats.data.Validated
import cats.data.Validated.Valid
import shared.controllers.validators.resolvers.ResolveTaxYearMinimum
import shared.models.domain.TaxYear
import shared.models.errors.MtdError

import scala.math.Ordering.Implicits.infixOrderingOps

sealed trait CreateAndAmendCharitableGivingReliefsSchema

object CreateAndAmendCharitableGivingReliefsSchema {

  case object Def1 extends CreateAndAmendCharitableGivingReliefsSchema
  case object Def2 extends CreateAndAmendCharitableGivingReliefsSchema

  def schemaFor(taxYearString: String): Validated[Seq[MtdError], CreateAndAmendCharitableGivingReliefsSchema] =
    ResolveTaxYearMinimum(TaxYear.fromMtd("2017-18"))(taxYearString).andThen(schemaForValidated)

  private def schemaForValidated(taxYear: TaxYear): Validated[Seq[MtdError], CreateAndAmendCharitableGivingReliefsSchema] =
    Valid(if (taxYear >= TaxYear.fromMtd("2025-26")) Def2 else Def1)

}
