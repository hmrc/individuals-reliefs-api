/*
 * Copyright 2025 HM Revenue & Customs
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

package v3.reliefInvestments.retrieve.def2.model.response

import play.api.libs.json.{Json, OFormat}

case class CommunityInvestmentItem(uniqueInvestmentRef: String,
                                   name: Option[String],
                                   dateOfInvestment: Option[String],
                                   amountInvested: Option[BigDecimal],
                                   reliefClaimed: BigDecimal)

object CommunityInvestmentItem {
  implicit val format: OFormat[CommunityInvestmentItem] = Json.format[CommunityInvestmentItem]
}
