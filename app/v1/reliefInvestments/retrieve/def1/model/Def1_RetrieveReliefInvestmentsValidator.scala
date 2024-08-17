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

package v1.reliefInvestments.retrieve.def1.model

import api.controllers.validators.Validator
import api.controllers.validators.resolvers.{ResolveNino, ResolveTaxYear}
import api.models.domain.TaxYear
import api.models.errors.MtdError
import cats.data.Validated
import cats.implicits.catsSyntaxTuple2Semigroupal
import v1.reliefInvestments.retrieve.def1.model.request.Def1_RetrieveReliefInvestmentsRequestData
import v1.reliefInvestments.retrieve.model.request.RetrieveReliefInvestmentsRequestData

import javax.inject.Singleton

@Singleton
class Def1_RetrieveReliefInvestmentsValidator(nino: String, taxYear: String) extends Validator[RetrieveReliefInvestmentsRequestData] {

  override def validate: Validated[Seq[MtdError], Def1_RetrieveReliefInvestmentsRequestData] = {
    (
      ResolveNino(nino),
      ResolveTaxYear(TaxYear.minimumTaxYear.year, taxYear, None, None)
    ).mapN(Def1_RetrieveReliefInvestmentsRequestData)
  }

}