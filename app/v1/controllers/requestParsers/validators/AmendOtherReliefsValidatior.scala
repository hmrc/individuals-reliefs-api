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

import v1.controllers.requestParsers.validators.validations.{JsonFormatValidation, NinoValidation, TaxYearValidation}
import v1.models.errors.{MtdError, RuleIncorrectOrEmptyBodyError}

class AmendOtherReliefsValidatior extends Validator[???] {

  private val validationSet = List(parameterFormatValidation, bodyFormatValidation, bodyFieldValidation)

  private def parameterFormatValidation: RawData??? => List[List[MtdError]] = (data: RawData???) => {
    List(
      NinoValidation.validate(data.nino),
      TaxYearValidation.validate(data.taxYear)
    )
  }

  private def bodyFormatValidation: RawData??? => List[List[MtdError]] = { data =>
    List(
      JsonFormatValidation.validate[Body???](data.body, RuleIncorrectOrEmptyBodyError)
    )
  }

  private def bodyFieldValidation: RawData??? => List[List[MtdError]] = { data =>
    val body = data.body.as[Body???]

    List(flattenErrors(
      List(
        body.vctSubscriptionsItems.map(_.zipWithIndex.flatMap {
          case (item, i) => validateVctSubscription(item, i)
        }),
        body.eisSubscriptionsItems.map(_.zipWithIndex.flatMap {
          case (item, i) => validateEisSubscription(item, i)
        }),
        body.communityInvestmentItems.map(_.zipWithIndex.flatMap {
          case (item, i) => validateCommunityInvestment(item, i)
        }),
        body.seedEnterpriseInvestmentItems.map(_.zipWithIndex.flatMap {
          case (item, i) => validateSeedEnterpriseInvestment(item, i)
        }),
        body.socialEnterpriseInvestmentItems.map(_.zipWithIndex.flatMap {
          case (item, i) => validatesocialEnterpriseInvestment(item, i)
        })
      ).map(_.getOrElse(NoValidationErrors).toList)
    ))
  }

  private def validateVctSubscription(vctSubscriptionsItem: VctSubscriptionsItem, arrayIndex: Int): List[MtdError] = {
    List(
      InvestmentRefValidation.validateOptional(
        investmentRef = vctSubscriptionsItem.uniqueInvestmentRef,
        path = s"/vctSubscriptionsItems/$arrayIndex/uniqueInvestmentRef"
      ),
      NameValidation.validateOptional(
        name = vctSubscriptionsItem.name,
        path = s"/vctSubscriptionsItems/$arrayIndex/name"
      ),
      DateValidation.validateOptional(
        date = vctSubscriptionsItem.dateOfInvestment,
        path = s"/vctSubscriptionsItems/$arrayIndex/dateOfInvestment"
      ),
      NumberValidation.validateOptional(
        field = vctSubscriptionsItem.amountInvested,
        path = s"/vctSubscriptionsItems/$arrayIndex/amountInvested"
      ),
      NumberValidation.validateOptional(
        field = vctSubscriptionsItem.reliefClaimed,
        path = s"/vctSubscriptionsItems/$arrayIndex/reliefClaimed"
      ),
    ).flatten
  }

  private def validateEisSubscription(eisSubscriptionsItem: EisSubscriptionsItem, arrayIndex: Int): List[MtdError] = {
    List(
      InvestmentRefValidation.validateOptional(
        investmentRef = eisSubscriptionsItem.uniqueInvestmentRef,
        path = s"/eisSubscriptionsItems/$arrayIndex/uniqueInvestmentRef"
      ),
      NameValidation.validateOptional(
        name = eisSubscriptionsItem.name,
        path = s"/eisSubscriptionsItems/$arrayIndex/name"
      ),
      DateValidation.validateOptional(
        date = eisSubscriptionsItem.dateOfInvestment,
        path = s"/eisSubscriptionsItems/$arrayIndex/dateOfInvestment"
      ),
      NumberValidation.validateOptional(
        field = eisSubscriptionsItem.amountInvested,
        path = s"/eisSubscriptionsItems/$arrayIndex/amountInvested"
      ),
      NumberValidation.validateOptional(
        field = eisSubscriptionsItem.reliefClaimed,
        path = s"/eisSubscriptionsItems/$arrayIndex/reliefClaimed"
      ),
    ).flatten
  }


  private def validateCommunityInvestment(communityInvestmentItem: CommunityInvestmentItem, arrayIndex: Int): List[MtdError] = {
    List(
      InvestmentRefValidation.validateOptional(
        investmentRef = communityInvestmentItem.uniqueInvestmentRef,
        path = s"/communityInvestmentItems/$arrayIndex/uniqueInvestmentRef"
      ),
      NameValidation.validateOptional(
        name = communityInvestmentItem.name,
        path = s"/communityInvestmentItems/$arrayIndex/name"
      ),
      DateValidation.validateOptional(
        date = communityInvestmentItem.dateOfInvestment,
        path = s"/communityInvestmentItems/$arrayIndex/dateOfInvestment"
      ),
      NumberValidation.validateOptional(
        field = communityInvestmentItem.amountInvested,
        path = s"/communityInvestmentItems/$arrayIndex/amountInvested"
      ),
      NumberValidation.validateOptional(
        field = communityInvestmentItem.reliefClaimed,
        path = s"/communityInvestmentItems/$arrayIndex/reliefClaimed"
      ),
    ).flatten
  }

  private def validateSeedEnterpriseInvestment(seedEnterpriseInvestmentItem: SeedEnterpriseInvestmentItem, arrayIndex: Int): List[MtdError] = {
    List(
      InvestmentRefValidation.validateOptional(
        investmentRef = seedEnterpriseInvestmentItem.uniqueInvestmentRef,
        path = s"/seedEnterpriseInvestmentItems/$arrayIndex/uniqueInvestmentRef"
      ),
      NameValidation.validateOptional(
        name = seedEnterpriseInvestmentItem.companyName,
        path = s"/seedEnterpriseInvestmentItems/$arrayIndex/companyName"
      ),
      DateValidation.validateOptional(
        date = seedEnterpriseInvestmentItem.dateOfInvestment,
        path = s"/seedEnterpriseInvestmentItems/$arrayIndex/dateOfInvestment"
      ),
      NumberValidation.validateOptional(
        field = seedEnterpriseInvestmentItem.amountInvested,
        path = s"/seedEnterpriseInvestmentItems/$arrayIndex/amountInvested"
      ),
      NumberValidation.validateOptional(
        field = seedEnterpriseInvestmentItem.reliefClaimed,
        path = s"/seedEnterpriseInvestmentItems/$arrayIndex/reliefClaimed"
      ),
    ).flatten
  }


  private def validatesocialEnterpriseInvestment(socialEnterpriseInvestmentItem: SocialEnterpriseInvestmentItem, arrayIndex: Int): List[MtdError] = {
    List(
      InvestmentRefValidation.validateOptional(
        investmentRef = socialEnterpriseInvestmentItem.uniqueInvestmentRef,
        path = s"/socialEnterpriseInvestmentItems/$arrayIndex/uniqueInvestmentRef"
      ),
      NameValidation.validateOptional(
        name = socialEnterpriseInvestmentItem.socialEnterpriseName,
        path = s"/socialEnterpriseInvestmentItems/$arrayIndex/socialEnterpriseName"
      ),
      DateValidation.validateOptional(
        date = socialEnterpriseInvestmentItem.dateOfInvestment,
        path = s"/socialEnterpriseInvestmentItems/$arrayIndex/dateOfInvestment"
      ),
      NumberValidation.validateOptional(
        field = socialEnterpriseInvestmentItem.amountInvested,
        path = s"/socialEnterpriseInvestmentItems/$arrayIndex/amountInvested"
      ),
      NumberValidation.validateOptional(
        field = socialEnterpriseInvestmentItem.reliefClaimed,
        path = s"/socialEnterpriseInvestmentItems/$arrayIndex/reliefClaimed"
      ),
    ).flatten
  }


  override def validate(data: AmendReliefInvestmentsRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }
}
