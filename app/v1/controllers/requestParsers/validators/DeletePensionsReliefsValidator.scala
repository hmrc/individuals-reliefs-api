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
import api.controllers.requestParsers.validators.validations.{MtdTaxYearValidation, NinoValidation, TaxYearValidation, minimumTaxYear}
import api.models.errors.MtdError
import config.AppConfig
import v1.models.request.deletePensionsReliefs.DeletePensionsReliefsRawData

import javax.inject.Inject

class DeletePensionsReliefsValidator @Inject() (appConfig: AppConfig) extends Validator[DeletePensionsReliefsRawData] {

  private val validationSet = List(parameterFormatValidation, parameterRuleValidation)

  private def parameterFormatValidation: DeletePensionsReliefsRawData => List[List[MtdError]] = data => {
    List(
      NinoValidation.validate(data.nino),
      TaxYearValidation.validate(data.taxYear)
    )
  }

  private def parameterRuleValidation: DeletePensionsReliefsRawData => List[List[MtdError]] = (data: DeletePensionsReliefsRawData) => {
    List(
      MtdTaxYearValidation.validate(data.taxYear, minimumTaxYear)
    )
  }

  override def validate(data: DeletePensionsReliefsRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }

}
