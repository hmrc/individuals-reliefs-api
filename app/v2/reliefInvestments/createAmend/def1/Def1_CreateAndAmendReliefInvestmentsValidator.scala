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

package v2.reliefInvestments.createAmend.def1

import cats.data.Validated
import cats.implicits._
import play.api.libs.json.JsValue
import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers._
import shared.models.domain.TaxYear
import shared.models.errors.{MtdError, RuleTaxYearForVersionNotSupportedError, RuleTaxYearNotSupportedError}
import v2.reliefInvestments.createAmend.def1.model.request.{
  Def1_CreateAndAmendReliefInvestmentsRequestBody,
  Def1_CreateAndAmendReliefInvestmentsRequestData
}
import v2.reliefInvestments.createAmend.model.request.CreateAndAmendReliefInvestmentsRequestData

import javax.inject.Singleton

@Singleton
class Def1_CreateAndAmendReliefInvestmentsValidator(nino: String, taxYear: String, body: JsValue)
    extends Validator[CreateAndAmendReliefInvestmentsRequestData] {

  private val resolveJson = new ResolveNonEmptyJsonObject[Def1_CreateAndAmendReliefInvestmentsRequestBody]()

  private val resolveTaxYear = ResolveTaxYearMinMax(
    (TaxYear.fromMtd("2020-21"), TaxYear.fromMtd("2024-25")),
    RuleTaxYearNotSupportedError,
    RuleTaxYearForVersionNotSupportedError)

  private val rulesValidator = Def1_CreateAndAmendReliefInvestmentsRulesValidator

  override def validate: Validated[Seq[MtdError], CreateAndAmendReliefInvestmentsRequestData] =
    (
      ResolveNino(nino),
      resolveTaxYear(taxYear),
      resolveJson(body)
    ).mapN(Def1_CreateAndAmendReliefInvestmentsRequestData) andThen rulesValidator.validateBusinessRules

}
