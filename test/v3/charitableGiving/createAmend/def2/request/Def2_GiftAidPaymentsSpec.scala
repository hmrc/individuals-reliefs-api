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

package v3.charitableGiving.createAmend.def2.request

import play.api.libs.json.*
import shared.utils.UnitSpec
import v3.charitableGiving.createAmend.def2.model.request.Def2_GiftAidPayments
import v3.fixtures.createAndAmendCharitableGivingTaxReliefs.Def2_CreateAndAmendCharitableGivingTaxReliefsFixtures.{
  giftAidModel,
  giftAidMtdJson,
  giftAidDesJson
}

class Def2_GiftAidPaymentsSpec extends UnitSpec {

  "reads" when {
    "passed valid JSON" should {
      "return a valid model" in {
        giftAidMtdJson.as[Def2_GiftAidPayments] shouldBe giftAidModel
      }
    }
  }

  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(giftAidModel) shouldBe giftAidDesJson
      }
    }
  }

  "error when JSON is invalid" in {
    val invalidJson = Json.obj(
      "totalAmount" -> Json.parse("false")
    )
    invalidJson.validate[Def2_GiftAidPayments] shouldBe a[JsError]
  }

}
