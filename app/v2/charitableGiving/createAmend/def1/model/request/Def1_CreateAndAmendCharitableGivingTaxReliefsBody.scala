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

package v2.charitableGiving.createAmend.def1.model.request

import play.api.libs.json.{Json, OFormat}
import v2.charitableGiving.createAmend.model.request.CreateAndAmendCharitableGivingTaxReliefsBody

case class Def1_CreateAndAmendCharitableGivingTaxReliefsBody(giftAidPayments: Option[Def1_GiftAidPayments], gifts: Option[Def1_Gifts])
    extends CreateAndAmendCharitableGivingTaxReliefsBody

object Def1_CreateAndAmendCharitableGivingTaxReliefsBody {
  implicit val format: OFormat[Def1_CreateAndAmendCharitableGivingTaxReliefsBody] = Json.format[Def1_CreateAndAmendCharitableGivingTaxReliefsBody]

}
