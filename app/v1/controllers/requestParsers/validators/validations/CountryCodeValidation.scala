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

package v1.controllers.requestParsers.validators.validations

import com.neovisionaries.i18n.CountryCode
import v1.models.errors.{CountryCodeFormatError, RuleCountryCodeError, MtdError}

object CountryCodeValidation {

  def validate(field: String, path: String): List[MtdError] = (CountryCode.getByAlpha3Code(field), field) match {
    case (_: CountryCode, _)           => NoValidationErrors
    case (_, code) if code.length == 3 => List(RuleCountryCodeError.copy(paths = Some(Seq(path))))
    case _                             => List(CountryCodeFormatError.copy(paths = Some(Seq(path))))
  }

}
