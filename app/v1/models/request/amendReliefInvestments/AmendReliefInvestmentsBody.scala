/*
 * Copyright 2020 HM Revenue & Customs
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

package v1.models.request.amendReliefInvestments

import play.api.libs.json.{Json, OFormat}

case class AmendReliefInvestmentsBody(vctSubscriptionsItems: Option[Seq[VctSubscriptionsItem]],
                                      eisSubscriptionsItems: Option[Seq[EisSubscriptionsItem]],
                                      communityInvestmentItems: Option[Seq[CommunityInvestmentItem]],
                                      seedEnterpriseInvestmentItems: Option[Seq[SeedEnterpriseInvestmentItem]],
                                      socialEnterpriseInvestmentItems: Option[Seq[SocialEnterpriseInvestmentItem]]) {
  private def isEmpty: Boolean = vctSubscriptionsItems.isEmpty &&
    eisSubscriptionsItems.isEmpty &&
    communityInvestmentItems.isEmpty &&
    seedEnterpriseInvestmentItems.isEmpty &&
    socialEnterpriseInvestmentItems.isEmpty

  private def vctSubscriptionContainsEmptyObjectsOrIsEmpty: Boolean =
    (vctSubscriptionsItems.isDefined && vctSubscriptionsItems.get.isEmpty) ||
      vctSubscriptionsItems.isDefined && vctSubscriptionsItems.get.exists(_.isEmpty)

  private def eisSubscriptionContainsEmptyObjectsOrIsEmpty: Boolean =
    (eisSubscriptionsItems.isDefined && eisSubscriptionsItems.get.isEmpty) ||
      eisSubscriptionsItems.isDefined && eisSubscriptionsItems.get.exists(_.isEmpty)

  private def communityInvestmentContainsEmptyObjectsOrIsEmpty: Boolean =
    (communityInvestmentItems.isDefined && communityInvestmentItems.get.isEmpty) ||
      communityInvestmentItems.isDefined && communityInvestmentItems.get.exists(_.isEmpty)

  private def seedEnterpriseInvestmentContainsEmptyObjectsOrIsEmpty: Boolean =
    (seedEnterpriseInvestmentItems.isDefined && seedEnterpriseInvestmentItems.get.isEmpty) ||
      seedEnterpriseInvestmentItems.isDefined && seedEnterpriseInvestmentItems.get.exists(_.isEmpty)

  private def socialEnterpriseInvestmentContainsEmptyObjectsOrIsEmpty: Boolean =
    (socialEnterpriseInvestmentItems.isDefined && socialEnterpriseInvestmentItems.get.isEmpty) ||
      socialEnterpriseInvestmentItems.isDefined && socialEnterpriseInvestmentItems.get.exists(_.isEmpty)

  def isIncorrectOrEmptyBody: Boolean = isEmpty || {
    vctSubscriptionContainsEmptyObjectsOrIsEmpty ||
      eisSubscriptionContainsEmptyObjectsOrIsEmpty ||
      communityInvestmentContainsEmptyObjectsOrIsEmpty ||
      seedEnterpriseInvestmentContainsEmptyObjectsOrIsEmpty ||
      socialEnterpriseInvestmentContainsEmptyObjectsOrIsEmpty
  }
}

object AmendReliefInvestmentsBody {
  implicit val format: OFormat[AmendReliefInvestmentsBody] = Json.format[AmendReliefInvestmentsBody]
}