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

package v3.charitableGiving.createAmend.def1

import cats.data.Validated
import cats.implicits.*
import play.api.libs.json.JsValue
import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers.{ResolveNino, ResolveNonEmptyJsonObject}
import shared.models.domain.TaxYear
import shared.models.errors.MtdError
import v3.charitableGiving.createAmend.def1.model.request.Def1_CreateAndAmendCharitableGivingTaxReliefsBody
import v3.charitableGiving.createAmend.model.request.{
  CreateAndAmendCharitableGivingTaxReliefsRequestData,
  Def1_CreateAndAmendCharitableGivingTaxReliefsRequestData
}

class Def1_CreateAndAmendCharitableGivingReliefsValidator(nino: String, taxYear: String, body: JsValue)
    extends Validator[CreateAndAmendCharitableGivingTaxReliefsRequestData] {

  private val resolveJson: ResolveNonEmptyJsonObject[Def1_CreateAndAmendCharitableGivingTaxReliefsBody] =
    new ResolveNonEmptyJsonObject[Def1_CreateAndAmendCharitableGivingTaxReliefsBody]()

  private val rulesValidator =
    new Def1_CreateAndAmendCharitableGivingReliefsRulesValidator()

  override def validate: Validated[Seq[MtdError], CreateAndAmendCharitableGivingTaxReliefsRequestData] =
    (
      ResolveNino(nino),
      resolveJson(body)
    ).mapN { (validNino, requestBody) =>
      Def1_CreateAndAmendCharitableGivingTaxReliefsRequestData(
        nino = validNino,
        taxYear = TaxYear.fromMtd(taxYear),
        body = requestBody
      )
    }.andThen(rulesValidator.validateBusinessRules)

}
