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

package v1.reliefInvestments.createAmend.def1.model.request

import play.api.libs.json.{Json, OFormat}
import v1.reliefInvestments.common.model.{
  CommunityInvestmentItem,
  EisSubscriptionsItem,
  SeedEnterpriseInvestmentItem,
  SocialEnterpriseInvestmentItem,
  VctSubscriptionsItem
}
import v1.reliefInvestments.createAmend.model.request.CreateAndAmendReliefInvestmentsBody

case class Def1_CreateAndAmendReliefInvestmentsRequestBody(vctSubscription: Option[Seq[VctSubscriptionsItem]],
                                                           eisSubscription: Option[Seq[EisSubscriptionsItem]],
                                                           communityInvestment: Option[Seq[CommunityInvestmentItem]],
                                                           seedEnterpriseInvestment: Option[Seq[SeedEnterpriseInvestmentItem]],
                                                           socialEnterpriseInvestment: Option[Seq[SocialEnterpriseInvestmentItem]])
    extends CreateAndAmendReliefInvestmentsBody {

  private def isEmpty: Boolean = vctSubscription.isEmpty &&
    eisSubscription.isEmpty &&
    communityInvestment.isEmpty &&
    seedEnterpriseInvestment.isEmpty &&
    socialEnterpriseInvestment.isEmpty

  private def vctSubscriptionIsEmpty: Boolean =
    vctSubscription.isDefined && vctSubscription.get.isEmpty

  private def eisSubscriptionIsEmpty: Boolean =
    eisSubscription.isDefined && eisSubscription.get.isEmpty

  private def communityInvestmentIsEmpty: Boolean =
    communityInvestment.isDefined && communityInvestment.get.isEmpty

  private def seedEnterpriseInvestmentIsEmpty: Boolean =
    seedEnterpriseInvestment.isDefined && seedEnterpriseInvestment.get.isEmpty

  private def socialEnterpriseInvestmentIsEmpty: Boolean =
    socialEnterpriseInvestment.isDefined && socialEnterpriseInvestment.get.isEmpty

  def isIncorrectOrEmptyBody: Boolean = isEmpty || {
    vctSubscriptionIsEmpty ||
    eisSubscriptionIsEmpty ||
    communityInvestmentIsEmpty ||
    seedEnterpriseInvestmentIsEmpty ||
    socialEnterpriseInvestmentIsEmpty
  }

}

object Def1_CreateAndAmendReliefInvestmentsRequestBody {
  implicit val format: OFormat[Def1_CreateAndAmendReliefInvestmentsRequestBody] = Json.format[Def1_CreateAndAmendReliefInvestmentsRequestBody]
}
