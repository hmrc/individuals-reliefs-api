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

package v1.reliefInvestments.retrieve.def1.model.response

import api.models.utils.JsonErrorValidators
import play.api.libs.json.Json
import support.UnitSpec
import v1.fixtures.RetrieveReliefInvestmentsFixtures.{eisSubscriptionsItemJson, eisSubscriptionsItemModel}

class EisSubscriptionsItemSpec extends UnitSpec with JsonErrorValidators {

  "reads" when {
    "passed valid JSON" should {
      "return a valid model" in {
        eisSubscriptionsItemJson().as[EisSubscriptionsItem] shouldBe eisSubscriptionsItemModel()
      }

      "return valid model when the optional knowledgeIntensive is omitted" in {
        eisSubscriptionsItemJson(knowledgeIntensive = None) shouldBe Json.toJson(eisSubscriptionsItemModel(knowledgeIntensive = None))
      }
    }
  }

  "writes" when {
    "passed valid model" should {
      "return valid json" in {
        Json.toJson(eisSubscriptionsItemModel()) shouldBe eisSubscriptionsItemJson()
      }

      "return valid json when the optional knowledgeIntensive is omitted" in {
        Json.toJson(eisSubscriptionsItemModel(knowledgeIntensive = None)) shouldBe eisSubscriptionsItemJson(knowledgeIntensive = None)
      }
    }
  }

}
