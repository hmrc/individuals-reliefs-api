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

package v3.reliefInvestments.createAmend.def1.model.request

import play.api.libs.json.Json
import shared.models.utils.JsonErrorValidators
import shared.utils.UnitSpec
import v3.reliefInvestments.createAmend.def1.model.Def1_CreateAndAmendReliefInvestmentsFixtures._

class VctSubscriptionsItemSpec extends UnitSpec with JsonErrorValidators {

  "reads" when {
    "passed valid JSON" should {
      "return a valid model" in {
        Def1_vctSubscriptionsItemJson.as[VctSubscriptionsItem] shouldBe Def1_vctSubscriptionsItemModel
      }
    }
  }

  "writes" when {
    "passed valid model" should {
      "return valid json" in {
        Json.toJson(Def1_vctSubscriptionsItemModel) shouldBe Def1_vctSubscriptionsItemJson
      }
    }
  }

}
