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

package v2.foreignReliefs.createAmend.def1

import cats.data.Validated
import cats.implicits.catsSyntaxTuple3Semigroupal
import play.api.libs.json.JsValue
import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers.{ResolveNino, ResolveNonEmptyJsonObject, ResolveTaxYearMinimum}
import shared.models.domain.TaxYear
import shared.models.errors.MtdError
import Def1_CreateAndAmendForeignReliefsRulesValidator.validateBusinessRules
import v2.foreignReliefs.createAmend.def1.model.request.{Def1_CreateAndAmendForeignReliefsBody, Def1_CreateAndAmendForeignReliefsRequestData}
import v2.foreignReliefs.createAmend.model.request.CreateAndAmendForeignReliefsRequestData

class Def1_CreateAndAmendForeignReliefsValidator(nino: String, taxYear: String, body: JsValue)
    extends Validator[CreateAndAmendForeignReliefsRequestData] {

  private val resolveJson    = new ResolveNonEmptyJsonObject[Def1_CreateAndAmendForeignReliefsBody]()
  private val resolveTaxYear = ResolveTaxYearMinimum(TaxYear.fromMtd("2020-21"))

  def validate: Validated[Seq[MtdError], Def1_CreateAndAmendForeignReliefsRequestData] =
    (
      ResolveNino(nino),
      resolveTaxYear(taxYear),
      resolveJson(body)
    ).mapN(Def1_CreateAndAmendForeignReliefsRequestData.apply) andThen validateBusinessRules

}
