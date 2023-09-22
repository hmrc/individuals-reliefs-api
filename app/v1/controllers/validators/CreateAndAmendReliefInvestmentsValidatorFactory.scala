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
import api.controllers.validators.resolvers._
import api.models.domain.TaxYear
import api.models.errors.{DateOfInvestmentFormatError, MtdError, NameFormatError, UniqueInvestmentRefFormatError}
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import cats.implicits._
import play.api.libs.json.JsValue
import v1.models.request.createAndAmendReliefInvestments.{
  CommunityInvestmentItem,
  CreateAndAmendReliefInvestmentsBody,
  CreateAndAmendReliefInvestmentsRequestData,
  EisSubscriptionsItem,
  SeedEnterpriseInvestmentItem,
  SocialEnterpriseInvestmentItem,
  VctSubscriptionsItem
}

import javax.inject.Singleton
import scala.annotation.nowarn

@Singleton
class CreateAndAmendReliefInvestmentsValidatorFactory extends ValidatorOps {

  private val uniqueInvestmentRefRegex = "^[0-9a-zA-Z{À-˿’}\\- _&`():.'^]{1,90}$".r

  private val nameRegex = "^[0-9a-zA-Z{À-˿'}\\- _&`():.'^]{1,105}$".r

  private val resolveParsedNumber = ResolveParsedNumber()

  @nowarn("cat=lint-byname-implicit")
  private val resolveJson = new ResolveNonEmptyJsonObject[CreateAndAmendReliefInvestmentsBody]()

  private val valid = Valid(())

  def validator(nino: String, taxYear: String, body: JsValue): Validator[CreateAndAmendReliefInvestmentsRequestData] =
    new Validator[CreateAndAmendReliefInvestmentsRequestData] {

      def validate: Validated[Seq[MtdError], CreateAndAmendReliefInvestmentsRequestData] =
        (
          ResolveNino(nino),
          ResolveTaxYear(TaxYear.minimumTaxYear.year, taxYear, None, None),
          resolveJson(body)
        ).mapN(CreateAndAmendReliefInvestmentsRequestData) andThen validateBusinessRules

      private def validateBusinessRules(
          parsed: CreateAndAmendReliefInvestmentsRequestData): Validated[Seq[MtdError], CreateAndAmendReliefInvestmentsRequestData] = {
        import parsed.body._

        List(
          vctSubscription.zipAndValidate(validateVctSubscription),
          eisSubscription.zipAndValidate(validateEisSubscription),
          communityInvestment.zipAndValidate(validateCommunityInvestment),
          seedEnterpriseInvestment.zipAndValidate(validateSeedEnterpriseInvestment),
          socialEnterpriseInvestment.zipAndValidate(validateSocialEnterpriseInvestment)
        ).traverse(identity).map(_ => parsed)
      }

    }

  private def validateVctSubscription(item: VctSubscriptionsItem, index: Int): Validated[Seq[MtdError], Unit] =
    validateItem(
      item.uniqueInvestmentRef,
      item.name,
      item.dateOfInvestment,
      item.amountInvested,
      item.reliefClaimed
    )("vctSubscription", index)

  private def validateEisSubscription(item: EisSubscriptionsItem, index: Int): Validated[Seq[MtdError], Unit] =
    validateItem(
      item.uniqueInvestmentRef,
      item.name,
      item.dateOfInvestment,
      item.amountInvested,
      item.reliefClaimed
    )("eisSubscription", index)

  private def validateCommunityInvestment(item: CommunityInvestmentItem, index: Int): Validated[Seq[MtdError], Unit] =
    validateItem(
      item.uniqueInvestmentRef,
      item.name,
      item.dateOfInvestment,
      item.amountInvested,
      item.reliefClaimed
    )("communityInvestment", index)

  private def validateSeedEnterpriseInvestment(item: SeedEnterpriseInvestmentItem, index: Int): Validated[Seq[MtdError], Unit] =
    validateItem(
      item.uniqueInvestmentRef,
      item.companyName,
      item.dateOfInvestment,
      item.amountInvested,
      item.reliefClaimed
    )("seedEnterpriseInvestment", index, "companyName")

  private def validateSocialEnterpriseInvestment(item: SocialEnterpriseInvestmentItem, index: Int): Validated[Seq[MtdError], Unit] =
    validateItem(
      item.uniqueInvestmentRef,
      item.socialEnterpriseName,
      item.dateOfInvestment,
      item.amountInvested,
      item.reliefClaimed
    )("socialEnterpriseInvestment", index, "socialEnterpriseName")

  private def validateItem(uniqueInvestmentRef: String,
                           maybeName: Option[String],
                           maybeDateOfInvestment: Option[String],
                           maybeAmountInvested: Option[BigDecimal],
                           reliefClaimed: BigDecimal)(itemType: String, index: Int, nameField: String = "name"): Validated[Seq[MtdError], Unit] =
    List(
      validateUniqueInvestmentRef(uniqueInvestmentRef, itemType, index),
      validateName(maybeName, s"/$itemType/$index/$nameField"),
      validateDate(maybeDateOfInvestment, itemType, index),
      validateNumericFields(maybeAmountInvested, reliefClaimed, itemType, index)
    ).sequence.andThen(_ => valid)

  private def validateUniqueInvestmentRef(uniqueInvestmentRef: String, itemType: String, index: Int): Validated[Seq[MtdError], Unit] =
    if (uniqueInvestmentRefRegex.matches(uniqueInvestmentRef))
      valid
    else
      Invalid(List(UniqueInvestmentRefFormatError.withPath(s"/$itemType/$index/uniqueInvestmentRef")))

  private def validateName(maybeName: Option[String], path: String): Validated[Seq[MtdError], Unit] =
    maybeName.mapOrElse(name =>
      if (nameRegex.matches(name)) valid
      else Invalid(List(NameFormatError.withPath(path))))

  private def validateDate(maybeDate: Option[String], itemType: String, index: Int): Validated[Seq[MtdError], Unit] =
    maybeDate.mapOrElse(ResolveIsoDate(_, Some(DateOfInvestmentFormatError), Some(s"/$itemType/$index/dateOfInvestment")).andThen(_ => valid))

  private def validateNumericFields(amountInvested: Option[BigDecimal],
                                    reliefClaimed: BigDecimal,
                                    itemType: String,
                                    index: Int): Validated[Seq[MtdError], Unit] = {
    validateWithPaths(
      (amountInvested, s"/$itemType/$index/amountInvested"),
      (Some(reliefClaimed), s"/$itemType/$index/reliefClaimed")
    )(resolveParsedNumber(_: BigDecimal, None, _: Option[String]))
  }

}
