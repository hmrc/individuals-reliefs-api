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

package v3.pensionReliefs.createAmend.def1

import cats.data.Validated
import cats.implicits.{catsSyntaxTuple3Semigroupal, toFoldableOps}
import play.api.libs.json.JsValue
import shared.controllers.validators.resolvers.{ResolveNino, ResolveNonEmptyJsonObject, ResolveParsedNumber, ResolveTaxYearMinimum}
import shared.controllers.validators.{RulesValidator, Validator}
import shared.models.domain.TaxYear
import shared.models.errors.MtdError
import v3.pensionReliefs.createAmend.def1.Def1_CreateAmendPensionReliefsValidator.validateBusinessRules
import v3.pensionReliefs.createAmend.def1.model.request.{CreateAmendPensionsReliefsBody, Def1_CreateAmendPensionsReliefsRequestData, PensionReliefs}
import v3.pensionReliefs.createAmend.model.request.CreateAmendPensionsReliefsRequestData

import javax.inject.Singleton

@Singleton
class Def1_CreateAmendPensionsReliefsValidator(nino: String, taxYear: String, body: JsValue)
    extends Validator[CreateAmendPensionsReliefsRequestData] {

  private val resolveJson = new ResolveNonEmptyJsonObject[CreateAmendPensionsReliefsBody]()
  val resolveTaxYear      = ResolveTaxYearMinimum(TaxYear.fromMtd("2020-21"))

  def validate: Validated[Seq[MtdError], CreateAmendPensionsReliefsRequestData] =
    (
      ResolveNino(nino),
      resolveTaxYear(taxYear),
      resolveJson(body)
    ).mapN(Def1_CreateAmendPensionsReliefsRequestData) andThen validateBusinessRules

}

object Def1_CreateAmendPensionReliefsValidator extends RulesValidator[CreateAmendPensionsReliefsRequestData] {
  private val resolveParsedNumber = ResolveParsedNumber()

  def validateBusinessRules(parsed: CreateAmendPensionsReliefsRequestData): Validated[Seq[MtdError], CreateAmendPensionsReliefsRequestData] =
    validate(parsed.body.pensionReliefs).onSuccess(parsed)

  private def validate(pensionReliefs: PensionReliefs): Validated[Seq[MtdError], Unit] = {
    import pensionReliefs._

    List(
      (regularPensionContributions, "/pensionReliefs/regularPensionContributions"),
      (oneOffPensionContributionsPaid, "/pensionReliefs/oneOffPensionContributionsPaid"),
      (retirementAnnuityPayments, "/pensionReliefs/retirementAnnuityPayments"),
      (paymentToEmployersSchemeNoTaxRelief, "/pensionReliefs/paymentToEmployersSchemeNoTaxRelief"),
      (overseasPensionSchemeContributions, "/pensionReliefs/overseasPensionSchemeContributions")
    ).traverse_ { case (value, path) =>
      resolveParsedNumber(value, path)
    }

  }

}
