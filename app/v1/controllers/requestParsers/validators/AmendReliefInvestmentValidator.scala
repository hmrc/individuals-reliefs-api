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

import v1.controllers.requestParsers.validators.validations.{DateValidation, InvestmentRefValidation, JsonFormatValidation, NameValidation, NinoValidation, NoValidationErrors, NumberValidation, TaxYearValidation}
import v1.models.errors.{FormatDateOfInvestmentErrorGenerator, FormatInvestmentRefErrorGenerator, FormatNameErrorGenerator, MtdError, RuleIncorrectOrEmptyBodyError, ValueFormatErrorGenerator}
import v1.models.requestData.amendReliefInvestments.{AmendReliefInvestmentsBody, AmendReliefInvestmentsRawData, AmendReliefInvestmentsRequest, CommunityInvestmentItem, EisSubscriptionsItem, SeedEnterpriseInvestmentItem, SocialEnterpriseInvestmentItem, VctSubscriptionsItem}


class AmendReliefInvestmentValidator extends Validator[AmendReliefInvestmentsRawData] {

  private val validationSet = List(parameterFormatValidation, bodyFormatValidator, bodyValueValidator)

  private def parameterFormatValidation: AmendReliefInvestmentsRawData => List[List[MtdError]] = (data: AmendReliefInvestmentsRawData) => {
    List(
      NinoValidation.validate(data.nino),
      TaxYearValidation.validate(data.taxYear),
    )
  }

  //noinspection ScalaStyle
  private def bodyFormatValidator: AmendReliefInvestmentsRawData => List[List[MtdError]] = { data =>
    List(
      JsonFormatValidation.validate[AmendReliefInvestmentsBody](data.body, RuleIncorrectOrEmptyBodyError)
    )
  }

  private def bodyValueValidator: AmendReliefInvestmentsRawData => List[List[MtdError]] = { data =>
    val requestBodyData = data.body.as[AmendReliefInvestmentsBody]

    List(flattenErrors(
      List(
        requestBodyData.vctSubscription.map(validateVct).getOrElse(NoValidationErrors)
      )
    ))
  }

  private def validateVct(vct: VctSubscriptionsItem): List[MtdError] = {
    List(
      InvestmentRefValidation.validateOptional(
        vct.uniqueInvestmentRef),
      NumberValidation.validateOptional(
        vct.amountInvested),
      NumberValidation.validateOptional(
        vct.reliefClaimed),
      DateValidation.validateOptional(
        vct.dateOfInvestment),
      NameValidation.validateOptional(
        vct.name)
    ).flatten
  }

  private def flattenErrors(errors: List[List[MtdError]]): List[MtdError] = {
    errors.flatten.groupBy(_.message).map {case (_, errors) =>

      val baseError = errors.head.copy(paths = Some(Seq.empty[String]))

      errors.fold(baseError)(
        (error1, error2) =>
          error1.copy(paths = Some(error1.paths.getOrElse(Seq.empty[String]) ++ error2.paths.getOrElse(Seq.empty[String])))
      )
    }.toList
  }

}
