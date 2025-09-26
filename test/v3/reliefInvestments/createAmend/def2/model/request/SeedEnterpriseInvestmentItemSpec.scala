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

import play.api.libs.json.*
import shared.utils.UnitSpec
import v3.reliefInvestments.createAmend.def2.model.Def2_CreateAndAmendReliefInvestmentsFixtures.*

class SeedEnterpriseInvestmentItemSpec extends UnitSpec {

  "reads" when {
    "passed valid JSON" should {
      "return a valid model" in {
        Def2_seedEnterpriseInvestmentItemJson.as[SeedEnterpriseInvestmentItem] shouldBe Def2_seedEnterpriseInvestmentItemModel
      }
    }
  }

  "writes" when {
    "passed valid model" should {
      "return valid json" in {
        Json.toJson(Def2_seedEnterpriseInvestmentItemModel) shouldBe Def2_seedEnterpriseInvestmentItemJson
      }
    }
  }

  "error when JSON is invalid" in {
    JsObject.empty.validate[SeedEnterpriseInvestmentItem] shouldBe a[JsError]
  }

}
