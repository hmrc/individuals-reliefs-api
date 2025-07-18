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

package v3.reliefInvestments.createAmend.def2.model.request

import play.api.libs.json.{Json, OFormat}
import v3.reliefInvestments.createAmend.model.request.CreateAndAmendReliefInvestmentsBody

case class Def2_CreateAndAmendReliefInvestmentsRequestBody(vctSubscription: Option[Seq[VctSubscriptionsItem]],
                                                           eisSubscription: Option[Seq[EisSubscriptionsItem]],
                                                           communityInvestment: Option[Seq[CommunityInvestmentItem]],
                                                           seedEnterpriseInvestment: Option[Seq[SeedEnterpriseInvestmentItem]])
    extends CreateAndAmendReliefInvestmentsBody {

  private def isEmpty: Boolean = vctSubscription.isEmpty &&
    eisSubscription.isEmpty &&
    communityInvestment.isEmpty &&
    seedEnterpriseInvestment.isEmpty

  private def vctSubscriptionIsEmpty: Boolean =
    vctSubscription.isDefined && vctSubscription.get.isEmpty

  private def eisSubscriptionIsEmpty: Boolean =
    eisSubscription.isDefined && eisSubscription.get.isEmpty

  private def communityInvestmentIsEmpty: Boolean =
    communityInvestment.isDefined && communityInvestment.get.isEmpty

  private def seedEnterpriseInvestmentIsEmpty: Boolean =
    seedEnterpriseInvestment.isDefined && seedEnterpriseInvestment.get.isEmpty

  def isIncorrectOrEmptyBody: Boolean = isEmpty || {
    vctSubscriptionIsEmpty ||
    eisSubscriptionIsEmpty ||
    communityInvestmentIsEmpty ||
    seedEnterpriseInvestmentIsEmpty
  }

}

object Def2_CreateAndAmendReliefInvestmentsRequestBody {
  implicit val format: OFormat[Def2_CreateAndAmendReliefInvestmentsRequestBody] = Json.format[Def2_CreateAndAmendReliefInvestmentsRequestBody]
}
