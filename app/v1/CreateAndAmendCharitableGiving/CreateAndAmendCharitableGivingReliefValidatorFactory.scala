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

package v1.CreateAndAmendCharitableGiving

import api.controllers.validators.Validator
import play.api.libs.json.JsValue
import v1.CreateAndAmendCharitableGiving.def1.Def1_CreateAndAmendCharitableGivingReliefValidator
import v1.CreateAndAmendCharitableGiving.model.request.CreateAndAmendCharitableGivingTaxReliefRequestData

import javax.inject.Singleton

@Singleton
class CreateAndAmendCharitableGivingReliefValidatorFactory {

  def validator(nino: String, taxYear: String, body: JsValue): Validator[CreateAndAmendCharitableGivingTaxReliefRequestData] = {

    taxYear match {
      case _ =>
        new Def1_CreateAndAmendCharitableGivingReliefValidator(nino, taxYear, body)
    }
  }

}
