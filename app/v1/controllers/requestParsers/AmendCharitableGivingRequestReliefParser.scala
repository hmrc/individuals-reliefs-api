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

import v1.controllers.requestParsers.validators.AmendCharitableGivingReliefValidator
import v1.models.domain.Nino
import v1.models.request.createAndAmendCharitableGivingTaxRelief._

import javax.inject.Inject

class AmendCharitableGivingRequestReliefParser @Inject() (val validator: AmendCharitableGivingReliefValidator)
    extends RequestParser[CreateAndAmendCharitableGivingTaxReliefRawData, CreateAndAmendCharitableGivingTaxReliefRequest] {

  override protected def requestFor(data: CreateAndAmendCharitableGivingTaxReliefRawData): CreateAndAmendCharitableGivingTaxReliefRequest =
    CreateAndAmendCharitableGivingTaxReliefRequest(Nino(data.nino), data.taxYear, data.body.as[CreateAndAmendCharitableGivingTaxReliefBody])

}