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

package v1.CreateAndAmendCharitableGiving.model.request

import play.api.libs.json.{Json, OFormat}
import v1.CreateAndAmendCharitableGiving.def1.model.request.{Def1_GiftAidPayments, Def1_Gifts}

sealed trait CreateAndAmendCharitableGivingTaxReliefBody

case class Def1_CreateAndAmendCharitableGivingTaxReliefBody(giftAidPayments: Option[Def1_GiftAidPayments], gifts: Option[Def1_Gifts])
    extends CreateAndAmendCharitableGivingTaxReliefBody

object Def1_CreateAndAmendCharitableGivingTaxReliefBody {
  implicit val format: OFormat[Def1_CreateAndAmendCharitableGivingTaxReliefBody] = Json.format[Def1_CreateAndAmendCharitableGivingTaxReliefBody]

}
