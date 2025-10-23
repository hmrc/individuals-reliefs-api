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

package v1.retrieveCharitableGivingReliefs

import cats.data.Validated.{Invalid, Valid}
import shared.controllers.validators.Validator
import shared.models.errors.MtdError
import v1.retrieveCharitableGivingReliefs.RetrieveCharitableGivingReliefsSchema.{Def1, Def2}
import v1.retrieveCharitableGivingReliefs.def1.Def1_RetrieveCharitableGivingReliefsValidator
import v1.retrieveCharitableGivingReliefs.def2.Def2_RetrieveCharitableGivingReliefsValidator
import v1.retrieveCharitableGivingReliefs.model.request.RetrieveCharitableGivingReliefsRequestData

import javax.inject.Singleton

@Singleton
class RetrieveCharitableGivingReliefsValidatorFactory {

  def validator(nino: String, taxYear: String): Validator[RetrieveCharitableGivingReliefsRequestData] = {
    val schema = RetrieveCharitableGivingReliefsSchema.schemaFor(taxYear)

    schema match {
      case Valid(Def1)     => new Def1_RetrieveCharitableGivingReliefsValidator(nino, taxYear)
      case Valid(Def2)     => new Def2_RetrieveCharitableGivingReliefsValidator(nino, taxYear)
      case Invalid(errors) => Validator.returningErrors(errors)
    }

  }

}
