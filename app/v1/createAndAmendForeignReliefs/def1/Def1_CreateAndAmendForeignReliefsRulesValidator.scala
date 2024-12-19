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

package v1.createAndAmendForeignReliefs.def1

import cats.data.Validated
import cats.implicits.toFoldableOps
import shared.controllers.validators.RulesValidator
import shared.controllers.validators.resolvers.{ResolveParsedCountryCode, ResolveParsedNumber}
import shared.models.errors.MtdError
import v1.createAndAmendForeignReliefs.def1.model.request.{
  Def1_CreateAndAmendForeignReliefsRequestData,
  Def1_ForeignIncomeTaxCreditRelief,
  Def1_ForeignTaxCreditRelief,
  Def1_ForeignTaxForFtcrNotClaimed
}

object Def1_CreateAndAmendForeignReliefsRulesValidator extends RulesValidator[Def1_CreateAndAmendForeignReliefsRequestData] {

  private val resolveParsedNumber = ResolveParsedNumber()

  def validateBusinessRules(parsed: Def1_CreateAndAmendForeignReliefsRequestData): Validated[Seq[MtdError], Def1_CreateAndAmendForeignReliefsRequestData] = {
    import parsed.body._

    combine(
      foreignTaxCreditRelief.traverse_(validate),
      foreignIncomeTaxCreditRelief.traverse_(validate),
      foreignTaxForFtcrNotClaimed.traverse_(validate)
    ).onSuccess(parsed)
  }

  private def zipAndValidate[VALUE](fields: Seq[VALUE], validate: (VALUE, Int) => Validated[Seq[MtdError], Unit]): Validated[Seq[MtdError], Unit] =
    fields.zipWithIndex.traverse_(validate.tupled)

  private def validate(foreignTaxCreditRelief: Def1_ForeignTaxCreditRelief): Validated[Seq[MtdError], Unit] =
    resolveParsedNumber(foreignTaxCreditRelief.amount, "/foreignTaxCreditRelief/amount").andThen(_ => valid)

  private def validate(foreignIncomeTaxCreditReliefs: Seq[Def1_ForeignIncomeTaxCreditRelief]): Validated[Seq[MtdError], Unit] =
    zipAndValidate(foreignIncomeTaxCreditReliefs, validate)

  private def validate(entry: Def1_ForeignIncomeTaxCreditRelief, index: Int): Validated[Seq[MtdError], Unit] = {
    import entry._
    combine(
      ResolveParsedCountryCode(countryCode, s"/foreignIncomeTaxCreditRelief/$index/countryCode"),
      foreignTaxPaid.traverse_(resolveParsedNumber(_, s"/foreignIncomeTaxCreditRelief/$index/foreignTaxPaid")),
      resolveParsedNumber(taxableAmount, s"/foreignIncomeTaxCreditRelief/$index/taxableAmount")
    )
  }

  private def validate(foreignTaxForFtcrNotClaimed: Def1_ForeignTaxForFtcrNotClaimed): Validated[Seq[MtdError], Unit] =
    resolveParsedNumber(foreignTaxForFtcrNotClaimed.amount, "/foreignTaxForFtcrNotClaimed/amount").andThen(_ => valid)

}
