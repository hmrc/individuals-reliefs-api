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

import config.AppConfig
import javax.inject.Inject
import v1.controllers.requestParsers.validators.validations._
import v1.models.errors.{MtdError, RuleIncorrectOrEmptyBodyError}
import v1.models.request.amendForeignReliefs._

class AmendForeignReliefsValidator @Inject()(appConfig: AppConfig) extends Validator[AmendForeignReliefsRawData] {

  private val validationSet = List(parameterFormatValidation, parameterRuleValidation, bodyFormatValidation, bodyFieldValidation)

  private def parameterFormatValidation: AmendForeignReliefsRawData => List[List[MtdError]] = (data: AmendForeignReliefsRawData) => {
    List(
      NinoValidation.validate(data.nino),
      TaxYearValidation.validate(data.taxYear)
    )
  }

  private def parameterRuleValidation: AmendForeignReliefsRawData => List[List[MtdError]] = (data: AmendForeignReliefsRawData) => {
    List(
      MtdTaxYearValidation.validate(data.taxYear, minimumTaxYear)
    )
  }

  private def bodyFormatValidation: AmendForeignReliefsRawData => List[List[MtdError]] = { data =>
    List(
      JsonFormatValidation.validate[AmendForeignReliefsBody](data.body, RuleIncorrectOrEmptyBodyError)
    )
  }

  private def bodyFieldValidation: AmendForeignReliefsRawData => List[List[MtdError]] = { data =>
    val body = data.body.as[AmendForeignReliefsBody]

    val errorsO: Option[List[List[MtdError]]] = for {
      foreignTaxCreditReliefErrors <- body.foreignTaxCreditRelief.map(validateForeignTaxCreditRelief)
    } yield List(foreignTaxCreditReliefErrors)

    List(errorsO.map(flattenErrors)).flatten
  }

  private def validateForeignTaxCreditRelief(foreignTaxCreditRelief: ForeignTaxCreditRelief): List[MtdError] = {
    List(
      NumberValidation.validateOptional(
        field = Some(foreignTaxCreditRelief.amount),
        path = s"/foreignTaxCreditRelief/amount"
      )
    ).flatten
  }


  override def validate(data: AmendForeignReliefsRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }
}
