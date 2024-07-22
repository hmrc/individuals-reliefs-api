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

package v1.AmendOtherReliefs

import api.controllers.validators.Validator
import play.api.libs.json.JsValue
import v1.AmendOtherReliefs.AmendOtherReliefsSchema.Def1
import v1.AmendOtherReliefs.def1.Def1_AmendOtherReliefsValidator
import v1.AmendOtherReliefs.model.request.AmendOtherReliefsRequestData

import javax.inject.Inject

class AmendOtherReliefsValidatorFactory @Inject() {

  def validator(nino: String, taxYear: String, body: JsValue): Validator[AmendOtherReliefsRequestData] = {
    val schema = AmendOtherReliefsSchema.schema
    schema match {
      case Def1 => new Def1_AmendOtherReliefsValidator(nino, taxYear, body)
    }
  }

}
