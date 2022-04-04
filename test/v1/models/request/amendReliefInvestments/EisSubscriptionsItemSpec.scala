/*
 * Copyright 2022 HM Revenue & Customs
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

import play.api.libs.json.Json
import support.UnitSpec
import v1.models.utils.JsonErrorValidators

class EisSubscriptionsItemSpec extends UnitSpec with JsonErrorValidators {

  val eisSubscriptionsItem = EisSubscriptionsItem(
    "XTAL",
    Some("EIS Fund X"),
    true,
    Some("EIS Fund X"),
    Some(BigDecimal(23312.00)),
    BigDecimal(43432.00)
  )

  val json = Json.parse(
    """
      |{
      |  "uniqueInvestmentRef": "XTAL",
      |  "name": "EIS Fund X",
      |  "knowledgeIntensive": true,
      |  "dateOfInvestment": "EIS Fund X",
      |  "amountInvested": 23312.00,
      |  "reliefClaimed": 43432.00
      |}
        """.stripMargin
  )

  "reads" when {
    "passed valid JSON" should {
      "return a valid model" in {
        json.as[EisSubscriptionsItem] shouldBe eisSubscriptionsItem
      }
    }
  }

  "writes" when {
    "passed valid model" should {
      "return valid json" in {
        Json.toJson(eisSubscriptionsItem) shouldBe json
      }
    }
  }

}
