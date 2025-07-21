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

import play.api.libs.json.{Json, OWrites, Reads}
import shared.models.domain.Timestamp
import v3.reliefInvestments.retrieve.model.response.RetrieveReliefInvestmentsResponse

case class Def2_RetrieveReliefInvestmentsResponse(
    submittedOn: Timestamp,
    vctSubscription: Option[Seq[VctSubscriptionsItem]],
    eisSubscription: Option[Seq[EisSubscriptionsItem]],
    communityInvestment: Option[Seq[CommunityInvestmentItem]],
    seedEnterpriseInvestment: Option[Seq[SeedEnterpriseInvestmentItem]]
) extends RetrieveReliefInvestmentsResponse

object Def2_RetrieveReliefInvestmentsResponse {

  implicit val reads: Reads[Def2_RetrieveReliefInvestmentsResponse]    = Json.reads[Def2_RetrieveReliefInvestmentsResponse]
  implicit val writes: OWrites[Def2_RetrieveReliefInvestmentsResponse] = Json.writes[Def2_RetrieveReliefInvestmentsResponse]

}
