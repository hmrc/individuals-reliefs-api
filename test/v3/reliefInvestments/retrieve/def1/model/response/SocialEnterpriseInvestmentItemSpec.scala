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

package v3.reliefInvestments.retrieve.def1.model.response

import play.api.libs.json.*
import shared.utils.UnitSpec
import v3.fixtures.Def1_RetrieveReliefInvestmentsFixtures.{socialEnterpriseInvestmentItemJson, socialEnterpriseInvestmentItemModel}

class SocialEnterpriseInvestmentItemSpec extends UnitSpec {

  "reads" when {
    "passed valid JSON" should {
      "return a valid model" in {
        socialEnterpriseInvestmentItemJson.as[SocialEnterpriseInvestmentItem] shouldBe socialEnterpriseInvestmentItemModel
      }
    }
  }

  "writes" when {
    "passed valid model" should {
      "return valid json" in {
        Json.toJson(socialEnterpriseInvestmentItemModel) shouldBe socialEnterpriseInvestmentItemJson
      }
    }
  }

  "error when JSON is invalid" in {
    JsObject.empty.validate[SocialEnterpriseInvestmentItem] shouldBe a[JsError]
  }

}
