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

package v1.models.response.retrieveOtherReliefs

import play.api.libs.json.Json
import support.UnitSpec
import v1.models.utils.JsonErrorValidators

class PostCessationTradeReliefAndCertainOtherLossesSpec extends UnitSpec with JsonErrorValidators {
  val postCessationTradeReliefAndCertainOtherLosses: PostCessationTradeReliefAndCertainOtherLosses = PostCessationTradeReliefAndCertainOtherLosses(
    Some("myRef"),
    Some("ACME Inc"),
    Some("2019-08-10"),
    Some("Widgets Manufacturer"),
    Some("AB12412/A12"),
    222.22
  )

  val noOptionsPostCessationTradeReliefAndCertainOtherLosses: PostCessationTradeReliefAndCertainOtherLosses = PostCessationTradeReliefAndCertainOtherLosses(
    None,
    None,
    None,
    None,
    None,
    222.22
  )

  val json = Json.parse(
    """{
      |  "customerReference": "myRef",
      |  "businessName": "ACME Inc",
      |  "dateBusinessCeased": "2019-08-10",
      |  "natureOfTrade": "Widgets Manufacturer",
      |  "incomeSource": "AB12412/A12",
      |  "amount": 222.22
      |}""".stripMargin
  )

  val noOptionsJson = Json.parse(
    """{
      |  "amount": 222.22
      |}""".stripMargin
  )

  "reads" when {
    "passed valid JSON" should {
      "return a valid model" in {
        postCessationTradeReliefAndCertainOtherLosses shouldBe json.as[PostCessationTradeReliefAndCertainOtherLosses]
      }
    }
  }
  "reads from a JSON with no optional fields supplied" when {
    "passed a JSON with no optional fields" should {
      "return a model with no optional fields" in {
        noOptionsPostCessationTradeReliefAndCertainOtherLosses shouldBe noOptionsJson.as[PostCessationTradeReliefAndCertainOtherLosses]
      }
    }
  }
  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(postCessationTradeReliefAndCertainOtherLosses) shouldBe json
      }
    }
  }
  "writes from a model with no optional fields" when {
    "passed a model with no optional fields" should {
      "return a JSON with no optional fields" in {
        Json.toJson(noOptionsPostCessationTradeReliefAndCertainOtherLosses) shouldBe noOptionsJson
      }
    }
  }
}