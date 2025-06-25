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

package v1.createAndAmendCharitableGivingReliefs.def1

import cats.data.Validated
import cats.implicits.catsSyntaxTuple3Semigroupal
import play.api.libs.json.JsValue
import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers.{ResolveNino, ResolveNonEmptyJsonObject, ResolveTaxYearMinimum}
import shared.models.domain.TaxYear
import shared.models.errors.MtdError
import v1.createAndAmendCharitableGivingReliefs.def1.model.request.Def1_CreateAndAmendCharitableGivingTaxReliefsBody
import v1.createAndAmendCharitableGivingReliefs.model.request.{
  CreateAndAmendCharitableGivingTaxReliefsRequestData,
  Def1_CreateAndAmendCharitableGivingTaxReliefsRequestData
}

class Def1_CreateAndAmendCharitableGivingReliefsValidator(nino: String, taxYear: String, body: JsValue)
    extends Validator[CreateAndAmendCharitableGivingTaxReliefsRequestData] {

  private val resolveJson: ResolveNonEmptyJsonObject[Def1_CreateAndAmendCharitableGivingTaxReliefsBody] =
    new ResolveNonEmptyJsonObject[Def1_CreateAndAmendCharitableGivingTaxReliefsBody]()

  private val resolveTaxYear = ResolveTaxYearMinimum(TaxYear.fromMtd("2017-18"))

  private val rulesValidator =
    new Def1_CreateAndAmendCharitableGivingReliefsRulesValidator()

  def validate: Validated[Seq[MtdError], CreateAndAmendCharitableGivingTaxReliefsRequestData] =
    (
      ResolveNino(nino),
      resolveTaxYear(taxYear),
      resolveJson(body)
    ).mapN(Def1_CreateAndAmendCharitableGivingTaxReliefsRequestData) andThen rulesValidator.validateBusinessRules

}
