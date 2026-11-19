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

package v3.foreignReliefs.createAmend

import api.controllers.validators.Validator
import api.controllers.validators.resolvers.*
import api.models.domain.TaxYear
import api.models.errors.MtdError
import cats.data.Validated
import cats.implicits.catsSyntaxTuple3Semigroupal
import play.api.libs.json.JsValue
import v3.foreignReliefs.createAmend.CreateAndAmendForeignReliefsRulesValidator.validateBusinessRules
import v3.foreignReliefs.createAmend.model.request.{CreateAndAmendForeignReliefsBody, CreateAndAmendForeignReliefsRequestData}

class CreateAndAmendForeignReliefsValidator(nino: String, taxYear: String, body: JsValue) extends Validator[CreateAndAmendForeignReliefsRequestData] {

  private val resolveJson    = new ResolveNonEmptyJsonObject[CreateAndAmendForeignReliefsBody]()
  private val resolveTaxYear = ResolveDetailedTaxYear(minimumTaxYear = TaxYear.fromMtd("2020-21"))

  def validate: Validated[Seq[MtdError], CreateAndAmendForeignReliefsRequestData] =
    (
      ResolveNino(nino),
      resolveTaxYear(taxYear),
      resolveJson(body)
    ).mapN(CreateAndAmendForeignReliefsRequestData.apply) andThen validateBusinessRules

}
