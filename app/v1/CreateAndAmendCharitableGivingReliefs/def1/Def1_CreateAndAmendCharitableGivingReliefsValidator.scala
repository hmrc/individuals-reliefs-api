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

package v1.CreateAndAmendCharitableGivingReliefs.def1

import api.controllers.validators.Validator
import api.controllers.validators.resolvers.{ResolveNino, ResolveNonEmptyJsonObject, ResolveTaxYear}
import api.models.domain.TaxYear
import api.models.errors.MtdError
import cats.data.Validated
import cats.implicits.catsSyntaxTuple3Semigroupal
import play.api.libs.json.JsValue
import v1.CreateAndAmendCharitableGivingReliefs.def1.model.request.{
  Def1_CreateAndAmendCharitableGivingTaxReliefsBody,
  Def1_CreateAndAmendCharitableGivingTaxReliefsRequestData
}
import v1.CreateAndAmendCharitableGivingReliefs.model.request.CreateAndAmendCharitableGivingTaxReliefsRequestData

import scala.annotation.nowarn

class Def1_CreateAndAmendCharitableGivingReliefsValidator(nino: String, taxYear: String, body: JsValue)
    extends Validator[CreateAndAmendCharitableGivingTaxReliefsRequestData] {

  @nowarn("cat=lint-byname-implicit")
  private val resolveJson: ResolveNonEmptyJsonObject[Def1_CreateAndAmendCharitableGivingTaxReliefsBody] =
    new ResolveNonEmptyJsonObject[Def1_CreateAndAmendCharitableGivingTaxReliefsBody]()

  private val rulesValidator: Def1_CreateAndAmendCharitableGivingReliefsRulesValidator = new Def1_CreateAndAmendCharitableGivingReliefsRulesValidator()

  def validate: Validated[Seq[MtdError], CreateAndAmendCharitableGivingTaxReliefsRequestData] =
    (
      ResolveNino(nino),
      ResolveTaxYear(TaxYear.charitableGivingMinimumTaxYear.year, taxYear, None, None),
      resolveJson(body)
    ).mapN(Def1_CreateAndAmendCharitableGivingTaxReliefsRequestData) andThen rulesValidator.validateBusinessRules

}
