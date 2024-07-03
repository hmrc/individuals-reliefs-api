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

package v1.DeleteCharitableGivingReliefs.def1

import api.controllers.validators.Validator
import api.controllers.validators.resolvers.{ResolveNino, ResolveTaxYear}
import api.models.domain.TaxYear
import api.models.errors.MtdError
import cats.data.Validated
import cats.implicits.catsSyntaxTuple2Semigroupal
import v1.DeleteCharitableGivingReliefs.def1.model.request.Def1_DeleteCharitableGivingTaxReliefsRequestData
import v1.DeleteCharitableGivingReliefs.model.request.DeleteCharitableGivingTaxReliefsRequestData

class Def1_DeleteCharitableGivingReliefsValidator(nino: String, taxYear: String) extends Validator[DeleteCharitableGivingTaxReliefsRequestData] {

  def validate: Validated[Seq[MtdError], Def1_DeleteCharitableGivingTaxReliefsRequestData] =
    (
      ResolveNino(nino),
      ResolveTaxYear(TaxYear.charitableGivingMinimumTaxYear.year, taxYear, None, None)
    ).mapN(Def1_DeleteCharitableGivingTaxReliefsRequestData)

}
