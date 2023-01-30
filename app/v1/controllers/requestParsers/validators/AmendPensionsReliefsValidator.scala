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

package v1.controllers.requestParsers.validators

import api.controllers.requestParsers.validators.Validator
import api.controllers.requestParsers.validators.validations._
import api.models.errors.{MtdError, RuleIncorrectOrEmptyBodyError}
import config.AppConfig
import v1.models.request.amendPensionsReliefs._

import javax.inject.Inject

class AmendPensionsReliefsValidator @Inject() (appConfig: AppConfig) extends Validator[AmendPensionsReliefsRawData] {

  private val validationSet = List(
    parameterFormatValidation,
    parameterRuleValidation,
    bodyFormatValidation,
    incorrectOrEmptyBodySubmittedValidation,
    bodyFieldValidation
  )

  private def parameterFormatValidation: AmendPensionsReliefsRawData => List[List[MtdError]] = (data: AmendPensionsReliefsRawData) => {
    List(
      NinoValidation.validate(data.nino),
      TaxYearValidation.validate(data.taxYear)
    )
  }

  private def parameterRuleValidation: AmendPensionsReliefsRawData => List[List[MtdError]] = (data: AmendPensionsReliefsRawData) => {
    List(
      MtdTaxYearValidation.validate(data.taxYear, minimumTaxYear)
    )
  }

  private def bodyFormatValidation: AmendPensionsReliefsRawData => List[List[MtdError]] = { data =>
    List(
      JsonFormatValidation.validate[AmendPensionsReliefsBody](data.body, RuleIncorrectOrEmptyBodyError)
    )
  }

  private def incorrectOrEmptyBodySubmittedValidation: AmendPensionsReliefsRawData => List[List[MtdError]] = { data =>
    val body = data.body.as[AmendPensionsReliefsBody]
    if (body.isIncorrectOrEmptyBody) List(List(RuleIncorrectOrEmptyBodyError)) else NoValidationErrors
  }

  private def bodyFieldValidation: AmendPensionsReliefsRawData => List[List[MtdError]] = { data =>
    val body = data.body.as[AmendPensionsReliefsBody]

    List(
      flattenErrors(
        List(
          validatePensionsReliefs(body.pensionReliefs)
        )
      ))
  }

  private def validatePensionsReliefs(pensionReliefs: PensionReliefs): List[MtdError] = {
    List(
      NumberValidation.validateOptional(
        field = pensionReliefs.regularPensionContributions,
        path = s"/pensionReliefs/regularPensionContributions"
      ),
      NumberValidation.validateOptional(
        field = pensionReliefs.oneOffPensionContributionsPaid,
        path = s"/pensionReliefs/oneOffPensionContributionsPaid"
      ),
      NumberValidation.validateOptional(
        field = pensionReliefs.retirementAnnuityPayments,
        path = s"/pensionReliefs/retirementAnnuityPayments"
      ),
      NumberValidation.validateOptional(
        field = pensionReliefs.paymentToEmployersSchemeNoTaxRelief,
        path = s"/pensionReliefs/paymentToEmployersSchemeNoTaxRelief"
      ),
      NumberValidation.validateOptional(
        field = pensionReliefs.overseasPensionSchemeContributions,
        path = s"/pensionReliefs/overseasPensionSchemeContributions"
      )
    ).flatten
  }

  override def validate(data: AmendPensionsReliefsRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }

}
