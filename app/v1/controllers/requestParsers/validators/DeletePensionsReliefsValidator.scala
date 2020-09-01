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

import config.FixedConfig
import v1.controllers.requestParsers.validators.validations.{MtdTaxYearValidation, NinoValidation, TaxYearValidation}
import v1.models.errors.MtdError
import v1.models.request.deletePensionsReliefs.DeletePensionsReliefsRawData

class DeletePensionsReliefsValidator extends Validator[DeletePensionsReliefsRawData] with FixedConfig {

  private val validationSet = List(parameterFormatValidation, parameterRuleValidation)

  private def parameterFormatValidation: DeletePensionsReliefsRawData => List[List[MtdError]] = data => {
    List(
      NinoValidation.validate(data.nino),
      TaxYearValidation.validate(data.taxYear)
    )
  }

  private def parameterRuleValidation: DeletePensionsReliefsRawData => List[List[MtdError]] = (data: DeletePensionsReliefsRawData) => {
    List(
      MtdTaxYearValidation.validate(data.taxYear, mtdMinimumTaxYear)
    )
  }

  override def validate(data: DeletePensionsReliefsRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }

}