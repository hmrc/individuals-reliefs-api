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

import api.controllers.validators.RulesValidator
import api.controllers.validators.resolvers.{ResolveParsedCountryCode, ResolveParsedNumber}
import api.models.errors.MtdError
import cats.data.Validated
import cats.implicits.toFoldableOps
import v1.models.request.createAndAmendForeignReliefs.{
  CreateAndAmendForeignReliefsRequestData,
  ForeignIncomeTaxCreditRelief,
  ForeignTaxCreditRelief,
  ForeignTaxForFtcrNotClaimed
}

object CreateAndAmendForeignReliefsRulesValidator extends RulesValidator[CreateAndAmendForeignReliefsRequestData] {

  private val resolveParsedNumber = ResolveParsedNumber()

  private def zipAndValidate[FIELD](fields: Seq[FIELD], validate: (FIELD, Int) => Validated[Seq[MtdError], Unit]): Validated[Seq[MtdError], Unit] =
    fields.zipWithIndex.traverse_(validate.tupled)

  def validateBusinessRules(parsed: CreateAndAmendForeignReliefsRequestData): Validated[Seq[MtdError], CreateAndAmendForeignReliefsRequestData] = {
    import parsed.body._

    combine(
      foreignTaxCreditRelief.traverse_(validate),
      foreignIncomeTaxCreditRelief.traverse_(validate),
      foreignTaxForFtcrNotClaimed.traverse_(validate)
    ).onSuccess(parsed)
  }

  private def validate(foreignTaxCreditRelief: ForeignTaxCreditRelief): Validated[Seq[MtdError], Unit] =
    resolveParsedNumber(foreignTaxCreditRelief.amount, None, Some("/foreignTaxCreditRelief/amount")).andThen(_ => valid)

  private def validate(foreignIncomeTaxCreditReliefs: Seq[ForeignIncomeTaxCreditRelief]): Validated[Seq[MtdError], Unit] =
    zipAndValidate(foreignIncomeTaxCreditReliefs, validate)

  private def validate(entry: ForeignIncomeTaxCreditRelief, index: Int): Validated[Seq[MtdError], Unit] = {
    import entry._

    combine(
      ResolveParsedCountryCode(countryCode, s"/foreignIncomeTaxCreditRelief/$index/countryCode"),
      foreignTaxPaid.traverse_(resolveParsedNumber(_, None, Some(s"/foreignIncomeTaxCreditRelief/$index/foreignTaxPaid"))),
      resolveParsedNumber(taxableAmount, None, Some(s"/foreignIncomeTaxCreditRelief/$index/taxableAmount"))
    )
  }

  private def validate(foreignTaxForFtcrNotClaimed: ForeignTaxForFtcrNotClaimed): Validated[Seq[MtdError], Unit] =
    resolveParsedNumber(foreignTaxForFtcrNotClaimed.amount, None, Some("/foreignTaxForFtcrNotClaimed/amount")).andThen(_ => valid)

}
