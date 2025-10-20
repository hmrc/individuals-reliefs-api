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

package v3.charitableGiving.retrieve.def1

import cats.data.Validated
//import cats.implicits.catsSyntaxTuple2Semigroupal
import shared.controllers.validators.Validator
//import shared.controllers.validators.resolvers.{ResolveNino, ResolveTaxYearMinimum}
import shared.controllers.validators.resolvers.ResolveNino
import shared.models.domain.TaxYear
import shared.models.errors.MtdError
import v3.charitableGiving.retrieve.model.request.RetrieveCharitableGivingReliefsRequestData
import v3.charitableGiving.retrieve.def1.model.request.Def1_RetrieveCharitableGivingReliefsRequestData
//import v3.charitableGiving.retrieve.def1.Def1_RetrieveCharitableGivingReliefsValidator

class Def1_RetrieveCharitableGivingReliefsValidator(nino: String, taxYear: String) extends Validator[RetrieveCharitableGivingReliefsRequestData] {

  // private val resolveTaxYear = ResolveTaxYearMinimum(TaxYear.fromMtd("2017-18"))

//  def validate: Validated[Seq[MtdError], RetrieveCharitableGivingReliefsRequestData] =
//    (
//      ResolveNino(nino),
//      resolveTaxYear(taxYear)
//    ).mapN(Def1_RetrieveCharitableGivingReliefsRequestData.apply)

  override def validate: Validated[Seq[MtdError], RetrieveCharitableGivingReliefsRequestData] =
    ResolveNino(nino).map(validNino => Def1_RetrieveCharitableGivingReliefsRequestData(validNino, TaxYear.fromMtd(taxYear)))

}
