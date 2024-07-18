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

package v1.deleteForeignReliefs

import api.controllers.validators.Validator
import v1.deleteForeignReliefs.def1.Def1_DeleteForeignReliefsValidator
import v1.deleteForeignReliefs.model.DeleteForeignReliefsRequestData

import javax.inject.Singleton

@Singleton
class DeleteForeignReliefsValidatorFactory {

  def validator(nino: String, taxYear: String): Validator[DeleteForeignReliefsRequestData] = {

    taxYear match {
      case _ => new Def1_DeleteForeignReliefsValidator(nino, taxYear)
    }
  }

}
