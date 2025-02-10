/*
 * Copyright 2024 HM Revenue & Customs
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

package v2.pensionReliefs.retrieve

import shared.controllers.validators.Validator
import v2.pensionReliefs.retrieve.RetrievePensionsReliefsSchema.Def1
import v2.pensionReliefs.retrieve.def1.Def1_RetrievePensionsReliefsValidator
import v2.pensionReliefs.retrieve.model.request.RetrievePensionsReliefsRequestData

import javax.inject.{Inject, Singleton}

@Singleton
class RetrievePensionsReliefsValidatorFactory @Inject() {

  def validator(nino: String, taxYear: String): Validator[RetrievePensionsReliefsRequestData] = {

    val schema = RetrievePensionsReliefsSchema.schema

    schema match {
      case Def1 => new Def1_RetrievePensionsReliefsValidator(nino, taxYear)
    }

  }

}
