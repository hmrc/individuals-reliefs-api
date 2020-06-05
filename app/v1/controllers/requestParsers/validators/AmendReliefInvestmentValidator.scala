/*
 * Copyright 2020 HM Revenue & Customs
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

import v1.controllers.requestParsers.validators.validations._
import v1.models.errors.{MtdError, RuleIncorrectOrEmptyBodyError}
import v1.models.request.amendReliefInvestments.{AmendReliefInvestmentsBody, AmendReliefInvestmentsRawData, CommunityInvestmentItem, EisSubscriptionsItem, SeedEnterpriseInvestmentItem, SocialEnterpriseInvestmentItem, VctSubscriptionsItem}


class AmendReliefInvestmentValidator extends Validator[AmendReliefInvestmentsRawData] {

  private val validationSet = List(parameterFormatValidation, bodyFormatValidation, incorrectOfEmptyBodySubmittedValidation, bodyFieldValidation)

  private def parameterFormatValidation: AmendReliefInvestmentsRawData => List[List[MtdError]] = (data: AmendReliefInvestmentsRawData) => {
    List(
      NinoValidation.validate(data.nino),
      TaxYearValidation.validate(data.taxYear)
    )
  }

  private def bodyFormatValidation: AmendReliefInvestmentsRawData => List[List[MtdError]] = { data =>
    List(
      JsonFormatValidation.validate[AmendReliefInvestmentsBody](data.body, RuleIncorrectOrEmptyBodyError)
    )
  }

  private def incorrectOfEmptyBodySubmittedValidation: AmendReliefInvestmentsRawData => List[List[MtdError]] = { data =>
    val body = data.body.as[AmendReliefInvestmentsBody]
    if(body.isIncorrectOrEmptyBody) List(List(RuleIncorrectOrEmptyBodyError)) else NoValidationErrors
  }

  private def bodyFieldValidation: AmendReliefInvestmentsRawData => List[List[MtdError]] = { data =>
    val body = data.body.as[AmendReliefInvestmentsBody]

    List(flattenErrors(
      List(
        body.vctSubscription.map(_.zipWithIndex.flatMap {
          case (item, i) => validateVctSubscription(item, i)
        }),
        body.eisSubscription.map(_.zipWithIndex.flatMap {
          case (item, i) => validateEisSubscription(item, i)
        }),
        body.communityInvestment.map(_.zipWithIndex.flatMap {
          case (item, i) => validateCommunityInvestment(item, i)
        }),
        body.seedEnterpriseInvestment.map(_.zipWithIndex.flatMap {
          case (item, i) => validateSeedEnterpriseInvestment(item, i)
        }),
        body.socialEnterpriseInvestment.map(_.zipWithIndex.flatMap {
          case (item, i) => validatesocialEnterpriseInvestment(item, i)
        })
      ).map(_.getOrElse(NoValidationErrors).toList)
    ))
  }

  private def validateVctSubscription(vctSubscriptionsItem: VctSubscriptionsItem, arrayIndex: Int): List[MtdError] = {
    List(
      InvestmentRefValidation.validateOptional(
        investmentRef = vctSubscriptionsItem.uniqueInvestmentRef,
        path = s"/vctSubscription/$arrayIndex/uniqueInvestmentRef"
      ),
      NameValidation.validateOptional(
        name = vctSubscriptionsItem.name,
        path = s"/vctSubscription/$arrayIndex/name"
      ),
      DateValidation.validateOptional(
        date = vctSubscriptionsItem.dateOfInvestment,
        path = s"/vctSubscription/$arrayIndex/dateOfInvestment"
      ),
      NumberValidation.validateOptional(
        field = vctSubscriptionsItem.amountInvested,
        path = s"/vctSubscription/$arrayIndex/amountInvested"
      ),
      NumberValidation.validateOptional(
        field = vctSubscriptionsItem.reliefClaimed,
        path = s"/vctSubscription/$arrayIndex/reliefClaimed"
      ),
    ).flatten
  }

  private def validateEisSubscription(eisSubscriptionsItem: EisSubscriptionsItem, arrayIndex: Int): List[MtdError] = {
    List(
      InvestmentRefValidation.validateOptional(
        investmentRef = eisSubscriptionsItem.uniqueInvestmentRef,
        path = s"/eisSubscription/$arrayIndex/uniqueInvestmentRef"
      ),
      NameValidation.validateOptional(
        name = eisSubscriptionsItem.name,
        path = s"/eisSubscription/$arrayIndex/name"
      ),
      DateValidation.validateOptional(
        date = eisSubscriptionsItem.dateOfInvestment,
        path = s"/eisSubscription/$arrayIndex/dateOfInvestment"
      ),
      NumberValidation.validateOptional(
        field = eisSubscriptionsItem.amountInvested,
        path = s"/eisSubscription/$arrayIndex/amountInvested"
      ),
      NumberValidation.validateOptional(
        field = eisSubscriptionsItem.reliefClaimed,
        path = s"/eisSubscription/$arrayIndex/reliefClaimed"
      ),
    ).flatten
  }


  private def validateCommunityInvestment(communityInvestmentItem: CommunityInvestmentItem, arrayIndex: Int): List[MtdError] = {
    List(
      InvestmentRefValidation.validateOptional(
        investmentRef = communityInvestmentItem.uniqueInvestmentRef,
        path = s"/communityInvestment/$arrayIndex/uniqueInvestmentRef"
      ),
      NameValidation.validateOptional(
        name = communityInvestmentItem.name,
        path = s"/communityInvestment/$arrayIndex/name"
      ),
      DateValidation.validateOptional(
        date = communityInvestmentItem.dateOfInvestment,
        path = s"/communityInvestment/$arrayIndex/dateOfInvestment"
      ),
      NumberValidation.validateOptional(
        field = communityInvestmentItem.amountInvested,
        path = s"/communityInvestment/$arrayIndex/amountInvested"
      ),
      NumberValidation.validateOptional(
        field = communityInvestmentItem.reliefClaimed,
        path = s"/communityInvestment/$arrayIndex/reliefClaimed"
      ),
    ).flatten
  }

  private def validateSeedEnterpriseInvestment(seedEnterpriseInvestmentItem: SeedEnterpriseInvestmentItem, arrayIndex: Int): List[MtdError] = {
    List(
      InvestmentRefValidation.validateOptional(
        investmentRef = seedEnterpriseInvestmentItem.uniqueInvestmentRef,
        path = s"/seedEnterpriseInvestment/$arrayIndex/uniqueInvestmentRef"
      ),
      NameValidation.validateOptional(
        name = seedEnterpriseInvestmentItem.companyName,
        path = s"/seedEnterpriseInvestment/$arrayIndex/companyName"
      ),
      DateValidation.validateOptional(
        date = seedEnterpriseInvestmentItem.dateOfInvestment,
        path = s"/seedEnterpriseInvestment/$arrayIndex/dateOfInvestment"
      ),
      NumberValidation.validateOptional(
        field = seedEnterpriseInvestmentItem.amountInvested,
        path = s"/seedEnterpriseInvestment/$arrayIndex/amountInvested"
      ),
      NumberValidation.validateOptional(
        field = seedEnterpriseInvestmentItem.reliefClaimed,
        path = s"/seedEnterpriseInvestment/$arrayIndex/reliefClaimed"
      ),
    ).flatten
  }


  private def validatesocialEnterpriseInvestment(socialEnterpriseInvestmentItem: SocialEnterpriseInvestmentItem, arrayIndex: Int): List[MtdError] = {
    List(
      InvestmentRefValidation.validateOptional(
        investmentRef = socialEnterpriseInvestmentItem.uniqueInvestmentRef,
        path = s"/socialEnterpriseInvestment/$arrayIndex/uniqueInvestmentRef"
      ),
      NameValidation.validateOptional(
        name = socialEnterpriseInvestmentItem.socialEnterpriseName,
        path = s"/socialEnterpriseInvestment/$arrayIndex/socialEnterpriseName"
      ),
      DateValidation.validateOptional(
        date = socialEnterpriseInvestmentItem.dateOfInvestment,
        path = s"/socialEnterpriseInvestment/$arrayIndex/dateOfInvestment"
      ),
      NumberValidation.validateOptional(
        field = socialEnterpriseInvestmentItem.amountInvested,
        path = s"/socialEnterpriseInvestment/$arrayIndex/amountInvested"
      ),
      NumberValidation.validateOptional(
        field = socialEnterpriseInvestmentItem.reliefClaimed,
        path = s"/socialEnterpriseInvestment/$arrayIndex/reliefClaimed"
      ),
    ).flatten
  }


  override def validate(data: AmendReliefInvestmentsRawData): List[MtdError] = {
   run(validationSet, data).distinct
  }
}