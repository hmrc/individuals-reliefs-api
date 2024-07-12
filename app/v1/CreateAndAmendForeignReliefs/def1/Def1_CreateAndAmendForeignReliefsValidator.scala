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

package v1.CreateAndAmendForeignReliefs.def1

import api.controllers.validators.Validator
import api.controllers.validators.resolvers.{ResolveNino, ResolveNonEmptyJsonObject, ResolveTaxYear}
import api.models.domain.TaxYear
import api.models.errors.MtdError
import cats.data.Validated
import cats.implicits.catsSyntaxTuple3Semigroupal
import play.api.libs.json.JsValue
import v1.CreateAndAmendForeignReliefs.def1.Def1_CreateAndAmendForeignReliefsRulesValidator.validateBusinessRules
import v1.CreateAndAmendForeignReliefs.def1.model.request.{Def1_CreateAndAmendForeignReliefsBody, Def1_CreateAndAmendForeignReliefsRequestData}
import v1.CreateAndAmendForeignReliefs.model.request.CreateAndAmendForeignReliefsRequestData

import scala.annotation.nowarn

class Def1_CreateAndAmendForeignReliefsValidator(nino: String, taxYear: String, body: JsValue)
    extends Validator[CreateAndAmendForeignReliefsRequestData] {

  @nowarn("cat=lint-byname-implicit")
  private val resolveJson = new ResolveNonEmptyJsonObject[Def1_CreateAndAmendForeignReliefsBody]()

  def validate: Validated[Seq[MtdError], Def1_CreateAndAmendForeignReliefsRequestData] =
    (
      ResolveNino(nino),
      ResolveTaxYear(TaxYear.minimumTaxYear.year, taxYear, None, None),
      resolveJson(body)
    ).mapN(Def1_CreateAndAmendForeignReliefsRequestData) andThen validateBusinessRules

}
