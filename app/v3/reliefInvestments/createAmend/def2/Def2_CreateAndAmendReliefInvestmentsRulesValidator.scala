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

package v3.reliefInvestments.createAmend.def2

import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import cats.implicits.toFoldableOps
import common.{DateOfInvestmentFormatError, NameFormatError, UniqueInvestmentRefFormatError}
import shared.controllers.validators.RulesValidator
import shared.controllers.validators.resolvers.{ResolveIsoDate, ResolveParsedNumber}
import shared.models.errors.MtdError
import v3.reliefInvestments.createAmend.def2.model.request._

import java.time.LocalDate

object Def2_CreateAndAmendReliefInvestmentsRulesValidator extends RulesValidator[Def2_CreateAndAmendReliefInvestmentsRequestData] {

  private val minYear = 1900
  private val maxYear = 2100

  private val uniqueInvestmentRefRegex = "^[0-9a-zA-Z{À-˿’}\\- _&`():.'^]{1,90}$".r

  private val nameRegex = "^[0-9a-zA-Z{À-˿'}\\- _&`():.'^]{1,105}$".r

  private val resolveParsedNumber = ResolveParsedNumber()

  private def zipAndValidate[VALUE](fields: Seq[VALUE], validate: (VALUE, Int) => Validated[Seq[MtdError], Unit]): Validated[Seq[MtdError], Unit] =
    fields.zipWithIndex.traverse_(validate.tupled)

  def validateBusinessRules(
      parsed: Def2_CreateAndAmendReliefInvestmentsRequestData): Validated[Seq[MtdError], Def2_CreateAndAmendReliefInvestmentsRequestData] = {

    import parsed.body._

    combine(
      vctSubscription.traverse_(validateItems("vctSubscription")),
      eisSubscription.traverse_(validateItems("eisSubscription")),
      communityInvestment.traverse_(validateItems("communityInvestment")),
      seedEnterpriseInvestment.traverse_(validateItems("seedEnterpriseInvestment", "companyName"))
    ).onSuccess(parsed)
  }

  private def validateItems(itemType: String, nameField: String = "name")(items: Seq[ReliefsInvestmentItem]): Validated[Seq[MtdError], Unit] =
    zipAndValidate(items, validateItem(itemType, nameField))

  private def validateItem(itemType: String, nameField: String)(item: ReliefsInvestmentItem, index: Int): Validated[Seq[MtdError], Unit] = {

    import item._

    combine(
      validateUniqueInvestmentRef(uniqueInvestmentRef, itemType, index),
      validateName(name, s"/$itemType/$index/$nameField"),
      validateDate(dateOfInvestment, itemType, index),
      validateNumericFields(amountInvested, reliefClaimed, itemType, index)
    )
  }

  private def validateUniqueInvestmentRef(uniqueInvestmentRef: String, itemType: String, index: Int): Validated[Seq[MtdError], Unit] =
    if (uniqueInvestmentRefRegex.matches(uniqueInvestmentRef)) {
      valid
    } else {
      Invalid(List(UniqueInvestmentRefFormatError.withPath(s"/$itemType/$index/uniqueInvestmentRef")))
    }

  private def validateName(maybeName: Option[String], path: String): Validated[Seq[MtdError], Unit] =
    maybeName
      .traverse_(name => if (nameRegex.matches(name)) valid else Invalid(List(NameFormatError.withPath(path))))

  private def validateDate(maybeDate: Option[String], itemType: String, index: Int): Validated[Seq[MtdError], Unit] = {
    val path = s"/$itemType/$index/dateOfInvestment"
    maybeDate.traverse_(ResolveIsoDate(_, DateOfInvestmentFormatError.withPath(path)).andThen(isDateInRange(_, path)))
  }

  private def isDateInRange(date: LocalDate, path: String): Validated[Seq[MtdError], Unit] = {
    if (date.getYear >= minYear && date.getYear < maxYear) Valid(()) else Invalid(List(DateOfInvestmentFormatError.withPath(path)))
  }

  private def validateNumericFields(amountInvested: Option[BigDecimal],
                                    reliefClaimed: BigDecimal,
                                    itemType: String,
                                    index: Int): Validated[Seq[MtdError], Unit] = {
    List(
      (amountInvested, s"/$itemType/$index/amountInvested"),
      (Some(reliefClaimed), s"/$itemType/$index/reliefClaimed")
    ).traverse_ { case (value, path) => resolveParsedNumber(value, path) }
  }

}
