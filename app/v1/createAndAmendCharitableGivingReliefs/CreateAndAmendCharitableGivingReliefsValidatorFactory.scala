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

package v1.createAndAmendCharitableGivingReliefs

import cats.data.Validated.{Invalid, Valid}
import play.api.libs.json.JsValue
import shared.controllers.validators.Validator
import v1.createAndAmendCharitableGivingReliefs.CreateAndAmendCharitableGivingReliefsSchema.{Def1, Def2}
import v1.createAndAmendCharitableGivingReliefs.def1.Def1_CreateAndAmendCharitableGivingReliefsValidator
import v1.createAndAmendCharitableGivingReliefs.def2.Def2_CreateAndAmendCharitableGivingReliefsValidator
import v1.createAndAmendCharitableGivingReliefs.model.request.CreateAndAmendCharitableGivingTaxReliefsRequestData

import javax.inject.Singleton

@Singleton
class CreateAndAmendCharitableGivingReliefsValidatorFactory {

  def validator(nino: String, taxYear: String, body: JsValue): Validator[CreateAndAmendCharitableGivingTaxReliefsRequestData] = {

    val schema = CreateAndAmendCharitableGivingReliefsSchema.schemaFor(taxYear)

    schema match {
      case Valid(Def1)     => new Def1_CreateAndAmendCharitableGivingReliefsValidator(nino, taxYear, body)
      case Valid(Def2)     => new Def2_CreateAndAmendCharitableGivingReliefsValidator(nino, taxYear, body)
      case Invalid(errors) => Validator.returningErrors(errors)
    }
  }

}
