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

package v1.models.request

import support.UnitSpec

class TaxYearSpec extends UnitSpec {

  "TaxYear" when {
    "constructed from an MTD tax year" should {
      "allow the downstream tax year to be extracted" should {
        "return the downstream tax value" in {
          TaxYear.fromMtd("2018-19").toDownstream shouldBe "2019"
        }
      }

      "toMtd" should {
        "allow the MTD tax year to be extracted" in {
          TaxYear.fromMtd("2018-19").toMtd shouldBe "2018-19"
        }
      }
    }

    "constructed from a downstream tax year" should {
      "allow the downstream tax year to be extracted" should {
        "return the downstream tax value" in {
          TaxYear.fromDownstream("2019").toDownstream shouldBe "2019"
        }
      }

      "toMtd" should {
        "allow the MTD tax year to be extracted" in {
          TaxYear.fromDownstream("2019").toMtd shouldBe "2018-19"
        }
      }
    }

    "constructed directly" must {
      "not compile" in {
        """new TaxYear("2021-22")""" shouldNot compile
      }
    }

    "compared with equals" must {
      "have equality based on content" in {
        TaxYear.fromMtd("2021-22") shouldBe TaxYear.fromDownstream("2022")
        TaxYear.fromMtd("2021-22") should not be TaxYear.fromDownstream("2021")
      }
    }
  }

}
