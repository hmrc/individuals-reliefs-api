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

package v1.otherReliefs.amend.def1

import api.controllers.validators.Validator
import api.controllers.validators.resolvers._
import api.models.domain.TaxYear
import api.models.errors.MtdError
import cats.data.Validated
import cats.implicits._
import play.api.libs.json.JsValue
import v1.otherReliefs.amend.def1.model.request.{Def1_AmendOtherReliefsRequestBody, Def1_AmendOtherReliefsRequestData}
import v1.otherReliefs.amend.model.request.AmendOtherReliefsRequestData

import javax.inject.Singleton

@Singleton
class Def1_AmendOtherReliefsValidator(nino: String, taxYear: String, body: JsValue) extends Validator[AmendOtherReliefsRequestData] {

  private val resolveJson = new ResolveNonEmptyJsonObject[Def1_AmendOtherReliefsRequestBody]()

  private val rulesValidator = Def1_AmendOtherReliefsRulesValidator

  override def validate: Validated[Seq[MtdError], AmendOtherReliefsRequestData] =
    (
      ResolveNino(nino),
      ResolveTaxYear(TaxYear.minimumTaxYear.year, taxYear, None, None),
      resolveJson(body)
    ).mapN(Def1_AmendOtherReliefsRequestData) andThen rulesValidator.validateBusinessRules

}
