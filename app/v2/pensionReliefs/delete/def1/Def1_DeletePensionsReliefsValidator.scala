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

package v2.pensionReliefs.delete.def1

import cats.data.Validated
import cats.implicits.catsSyntaxTuple2Semigroupal
import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers.{ResolveNino, ResolveTaxYearMinimum}
import shared.models.domain.TaxYear
import shared.models.errors.MtdError
import v2.pensionReliefs.delete.def1.model.request.Def1_DeletePensionsReliefsRequestData
import v2.pensionReliefs.delete.model.request.DeletePensionsReliefsRequestData

class Def1_DeletePensionsReliefsValidator(nino: String, taxYear: String) extends Validator[DeletePensionsReliefsRequestData] {

  private val resolveTaxYear = ResolveTaxYearMinimum(TaxYear.fromMtd("2020-21"))

  def validate: Validated[Seq[MtdError], DeletePensionsReliefsRequestData] = {
    (
      ResolveNino(nino),
      resolveTaxYear(taxYear)
    ).mapN(Def1_DeletePensionsReliefsRequestData)
  }

}
