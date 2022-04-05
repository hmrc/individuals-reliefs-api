/*
 * Copyright 2022 HM Revenue & Customs
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

package v1.controllers.requestParsers

import javax.inject.Inject
import v1.models.domain.Nino
import v1.controllers.requestParsers.validators.DeleteForeignReliefsValidator
import v1.models.request.deleteForeignReliefs.{DeleteForeignReliefsRawData, DeleteForeignReliefsRequest}

class DeleteForeignReliefsRequestParser @Inject() (val validator: DeleteForeignReliefsValidator)
    extends RequestParser[DeleteForeignReliefsRawData, DeleteForeignReliefsRequest] {

  override protected def requestFor(data: DeleteForeignReliefsRawData): DeleteForeignReliefsRequest = {
    DeleteForeignReliefsRequest(Nino(data.nino), data.taxYear)
  }

}
