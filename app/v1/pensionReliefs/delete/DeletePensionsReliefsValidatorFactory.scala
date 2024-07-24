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

package v1.pensionReliefs.delete

import api.controllers.validators.Validator
import v1.pensionReliefs.delete.DeletePensionsReliefsSchema.Def1
import v1.pensionReliefs.delete.def1.Def1_DeletePensionsReliefsValidator
import v1.pensionReliefs.delete.model.request.DeletePensionsReliefsRequestData

class DeletePensionsReliefsValidatorFactory {

  def validator(nino: String, taxYear: String): Validator[DeletePensionsReliefsRequestData] = {

    val schema = DeletePensionsReliefsSchema.schema

    schema match {
      case Def1 => new Def1_DeletePensionsReliefsValidator(nino, taxYear)
    }
  }

}
