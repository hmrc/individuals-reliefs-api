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

package v1.controllers.validators

import api.controllers.validators.{Validator, ValidatorOps}
import api.controllers.validators.resolvers.{ResolveNino, ResolveNonEmptyJsonObject, ResolveParsedCountryCode, ResolveParsedNumber, ResolveTaxYear}
import api.models.domain.TaxYear
import api.models.errors.MtdError
import cats.data.Validated
import cats.data.Validated.Valid
import cats.implicits.{catsSyntaxTuple3Semigroupal, toTraverseOps}
import play.api.libs.json.JsValue
import v1.models.request.createAndAmendForeignReliefs._

import javax.inject.Singleton
import scala.annotation.nowarn

@Singleton
class CreateAndAmendForeignReliefsValidatorFactory extends ValidatorOps {

  private val resolveParsedNumber = ResolveParsedNumber()

  @nowarn("cat=lint-byname-implicit")
  private val resolveJson = new ResolveNonEmptyJsonObject[CreateAndAmendForeignReliefsBody]()

  private val valid = Valid(())

  def validator(nino: String, taxYear: String, body: JsValue): Validator[CreateAndAmendForeignReliefsRequestData] =
    new Validator[CreateAndAmendForeignReliefsRequestData] {

      def validate: Validated[Seq[MtdError], CreateAndAmendForeignReliefsRequestData] =
        (
          ResolveNino(nino),
          ResolveTaxYear(TaxYear.minimumTaxYear.year, taxYear, None, None),
          resolveJson(body)
        ).mapN(CreateAndAmendForeignReliefsRequestData) andThen validateBusinessRules

      private def validateBusinessRules(
          parsed: CreateAndAmendForeignReliefsRequestData): Validated[Seq[MtdError], CreateAndAmendForeignReliefsRequestData] = {
        import parsed.body._

        List(
          foreignTaxCreditRelief.mapOrElse(validateForeignTaxCreditRelief),
          foreignIncomeTaxCreditRelief.mapOrElse(validateForeignIncomeTaxCreditRelief),
          foreignTaxForFtcrNotClaimed.mapOrElse(validateForeignTaxForFtcrNotClaimed)
        ).sequence.map(_ => parsed)
      }

    }

  private def validateForeignTaxCreditRelief(foreignTaxCreditRelief: ForeignTaxCreditRelief): Validated[Seq[MtdError], Unit] =
    resolveParsedNumber(foreignTaxCreditRelief.amount, None, Some("/foreignTaxCreditRelief/amount")).andThen(_ => valid)

  private def validateForeignIncomeTaxCreditRelief(foreignIncomeTaxCreditRelief: Seq[ForeignIncomeTaxCreditRelief]): Validated[Seq[MtdError], Unit] =
    foreignIncomeTaxCreditRelief.zipAndValidate { case (entry, index) =>
      import entry._

      List(
        ResolveParsedCountryCode(countryCode, s"/foreignIncomeTaxCreditRelief/$index/countryCode"),
        foreignTaxPaid.map(resolveParsedNumber(_, None, Some(s"/foreignIncomeTaxCreditRelief/$index/foreignTaxPaid"))).getOrElse(valid),
        resolveParsedNumber(taxableAmount, None, Some(s"/foreignIncomeTaxCreditRelief/$index/taxableAmount"))
      ).sequence.andThen(_ => valid)
    }

  private def validateForeignTaxForFtcrNotClaimed(foreignTaxForFtcrNotClaimed: ForeignTaxForFtcrNotClaimed): Validated[Seq[MtdError], Unit] =
    resolveParsedNumber(foreignTaxForFtcrNotClaimed.amount, None, Some("/foreignTaxForFtcrNotClaimed/amount")).andThen(_ => valid)

}
