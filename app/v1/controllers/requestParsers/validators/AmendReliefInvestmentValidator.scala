/*
 * Copyright 2022 HM Revenue & Customs
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

package v1.controllers.requestParsers.validators

import config.AppConfig
import javax.inject.Inject
import v1.controllers.requestParsers.validators.validations._
import v1.models.errors._
import v1.models.request.amendReliefInvestments._

class AmendReliefInvestmentValidator @Inject()(appConfig: AppConfig) extends Validator[AmendReliefInvestmentsRawData] {

  private val validationSet =
    List(parameterFormatValidation, parameterRuleValidation, bodyFormatValidation, incorrectOfEmptyBodySubmittedValidation, bodyFieldValidation)

  private def parameterFormatValidation: AmendReliefInvestmentsRawData => List[List[MtdError]] = (data: AmendReliefInvestmentsRawData) => {
    List(
      NinoValidation.validate(data.nino),
      TaxYearValidation.validate(data.taxYear)
    )
  }

  private def parameterRuleValidation: AmendReliefInvestmentsRawData => List[List[MtdError]] = (data: AmendReliefInvestmentsRawData) => {
    List(
      MtdTaxYearValidation.validate(data.taxYear, minimumTaxYear)
    )
  }

  private def bodyFormatValidation: AmendReliefInvestmentsRawData => List[List[MtdError]] = { data =>
    List(
      JsonFormatValidation.validate[AmendReliefInvestmentsBody](data.body, RuleIncorrectOrEmptyBodyError)
    )
  }

  private def incorrectOfEmptyBodySubmittedValidation: AmendReliefInvestmentsRawData => List[List[MtdError]] = { data =>
    val body = data.body.as[AmendReliefInvestmentsBody]
    if (body.isIncorrectOrEmptyBody) List(List(RuleIncorrectOrEmptyBodyError)) else NoValidationErrors
  }

  private def bodyFieldValidation: AmendReliefInvestmentsRawData => List[List[MtdError]] = { data =>
    val body = data.body.as[AmendReliefInvestmentsBody]

    val vctSubscriptionErrors = body.vctSubscription.map(_.zipWithIndex.flatMap {
      case (item, i) => validateVctSubscription(item, i)
    })

    val eisSubscriptionErrors = body.eisSubscription.map(_.zipWithIndex.flatMap {
      case (item, i) => validateEisSubscription(item, i)
    })

    val communityInvestmentErrors = body.communityInvestment.map(_.zipWithIndex.flatMap {
      case (item, i) => validateCommunityInvestment(item, i)
    })

    val seedEnterpriseInvestmentErrors =
      body.seedEnterpriseInvestment.map(_.zipWithIndex.flatMap {
        case (item, i) => validateSeedEnterpriseInvestment(item, i)
      })

    val socialEnterpriseInvestmentErrors = body.socialEnterpriseInvestment.map(_.zipWithIndex.flatMap {
      case (item, i) => validateSocialEnterpriseInvestment(item, i)
    })

    val errorsO: List[Option[Seq[MtdError]]] =
      List(vctSubscriptionErrors, eisSubscriptionErrors, communityInvestmentErrors, seedEnterpriseInvestmentErrors, socialEnterpriseInvestmentErrors)

    val errors: List[List[MtdError]] = errorsO.flatten.map(_.toList)

    List(flattenErrors(errors))
  }

  private def validateVctSubscription(vctSubscriptionsItem: VctSubscriptionsItem, arrayIndex: Int): List[MtdError] = {
    List(
      ReferenceRegexValidation.validateOptional(
        field = Some(vctSubscriptionsItem.uniqueInvestmentRef),
        path = s"/vctSubscription/$arrayIndex/uniqueInvestmentRef",
        error = UniqueInvestmentRefFormatError
      ),
      NameValidation.validateOptional(
        field = vctSubscriptionsItem.name,
        path = s"/vctSubscription/$arrayIndex/name",
        error = NameFormatError
      ),
      DateValidation.validateOptional(
        date = vctSubscriptionsItem.dateOfInvestment,
        path = s"/vctSubscription/$arrayIndex/dateOfInvestment",
        error = DateOfInvestmentFormatError
      ),
      NumberValidation.validateOptional(
        field = vctSubscriptionsItem.amountInvested,
        path = s"/vctSubscription/$arrayIndex/amountInvested"
      ),
      NumberValidation.validateOptional(
        field = Some(vctSubscriptionsItem.reliefClaimed),
        path = s"/vctSubscription/$arrayIndex/reliefClaimed"
      ),
    ).flatten
  }

  private def validateEisSubscription(eisSubscriptionsItem: EisSubscriptionsItem, arrayIndex: Int): List[MtdError] = {
    List(
      ReferenceRegexValidation.validateOptional(
        field = Some(eisSubscriptionsItem.uniqueInvestmentRef),
        path = s"/eisSubscription/$arrayIndex/uniqueInvestmentRef",
        error = UniqueInvestmentRefFormatError
      ),
      NameValidation.validateOptional(
        field = eisSubscriptionsItem.name,
        path = s"/eisSubscription/$arrayIndex/name",
        error = NameFormatError
      ),
      DateValidation.validateOptional(
        date = eisSubscriptionsItem.dateOfInvestment,
        path = s"/eisSubscription/$arrayIndex/dateOfInvestment",
        error = DateOfInvestmentFormatError
      ),
      NumberValidation.validateOptional(
        field = eisSubscriptionsItem.amountInvested,
        path = s"/eisSubscription/$arrayIndex/amountInvested"
      ),
      NumberValidation.validateOptional(
        field = Some(eisSubscriptionsItem.reliefClaimed),
        path = s"/eisSubscription/$arrayIndex/reliefClaimed"
      ),
    ).flatten
  }

  private def validateCommunityInvestment(communityInvestmentItem: CommunityInvestmentItem, arrayIndex: Int): List[MtdError] = {
    List(
      ReferenceRegexValidation.validateOptional(
        field = Some(communityInvestmentItem.uniqueInvestmentRef),
        path = s"/communityInvestment/$arrayIndex/uniqueInvestmentRef",
        error = UniqueInvestmentRefFormatError
      ),
      NameValidation.validateOptional(
        field = communityInvestmentItem.name,
        path = s"/communityInvestment/$arrayIndex/name",
        error = NameFormatError
      ),
      DateValidation.validateOptional(
        date = communityInvestmentItem.dateOfInvestment,
        path = s"/communityInvestment/$arrayIndex/dateOfInvestment",
        error = DateOfInvestmentFormatError
      ),
      NumberValidation.validateOptional(
        field = communityInvestmentItem.amountInvested,
        path = s"/communityInvestment/$arrayIndex/amountInvested"
      ),
      NumberValidation.validateOptional(
        field = Some(communityInvestmentItem.reliefClaimed),
        path = s"/communityInvestment/$arrayIndex/reliefClaimed"
      ),
    ).flatten
  }

  private def validateSeedEnterpriseInvestment(seedEnterpriseInvestmentItem: SeedEnterpriseInvestmentItem, arrayIndex: Int): List[MtdError] = {
    List(
      ReferenceRegexValidation.validateOptional(
        field = Some(seedEnterpriseInvestmentItem.uniqueInvestmentRef),
        path = s"/seedEnterpriseInvestment/$arrayIndex/uniqueInvestmentRef",
        error = UniqueInvestmentRefFormatError
      ),
      NameValidation.validateOptional(
        field = seedEnterpriseInvestmentItem.companyName,
        path = s"/seedEnterpriseInvestment/$arrayIndex/companyName",
        error = NameFormatError
      ),
      DateValidation.validateOptional(
        date = seedEnterpriseInvestmentItem.dateOfInvestment,
        path = s"/seedEnterpriseInvestment/$arrayIndex/dateOfInvestment",
        error = DateOfInvestmentFormatError
      ),
      NumberValidation.validateOptional(
        field = seedEnterpriseInvestmentItem.amountInvested,
        path = s"/seedEnterpriseInvestment/$arrayIndex/amountInvested"
      ),
      NumberValidation.validateOptional(
        field = Some(seedEnterpriseInvestmentItem.reliefClaimed),
        path = s"/seedEnterpriseInvestment/$arrayIndex/reliefClaimed"
      ),
    ).flatten
  }

  private def validateSocialEnterpriseInvestment(socialEnterpriseInvestmentItem: SocialEnterpriseInvestmentItem, arrayIndex: Int): List[MtdError] = {
    List(
      ReferenceRegexValidation.validateOptional(
        field = Some(socialEnterpriseInvestmentItem.uniqueInvestmentRef),
        path = s"/socialEnterpriseInvestment/$arrayIndex/uniqueInvestmentRef",
        error = UniqueInvestmentRefFormatError
      ),
      NameValidation.validateOptional(
        field = socialEnterpriseInvestmentItem.socialEnterpriseName,
        path = s"/socialEnterpriseInvestment/$arrayIndex/socialEnterpriseName",
        error = NameFormatError
      ),
      DateValidation.validateOptional(
        date = socialEnterpriseInvestmentItem.dateOfInvestment,
        path = s"/socialEnterpriseInvestment/$arrayIndex/dateOfInvestment",
        error = DateOfInvestmentFormatError
      ),
      NumberValidation.validateOptional(
        field = socialEnterpriseInvestmentItem.amountInvested,
        path = s"/socialEnterpriseInvestment/$arrayIndex/amountInvested"
      ),
      NumberValidation.validateOptional(
        field = Some(socialEnterpriseInvestmentItem.reliefClaimed),
        path = s"/socialEnterpriseInvestment/$arrayIndex/reliefClaimed"
      ),
    ).flatten
  }

  override def validate(data: AmendReliefInvestmentsRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }
}
