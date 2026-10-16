/*
 * Copyright 2026 HM Revenue & Customs
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

package api.controllers.validators.resolvers

import api.models.errors.*
import api.utils.UnitSpec
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}

import java.time.LocalDate

class ResolveIsoDateSpec extends UnitSpec {

  private val validDate = "2024-06-21"

  "ResolveIsoDate" should {

    "return the parsed date" when {
      "given a valid ISO date string" in {
        val expected = Valid(LocalDate.parse(validDate))

        val result: Validated[Seq[MtdError], LocalDate] = ResolveIsoDate(DateFormatError)(validDate)
        result shouldBe expected
      }

      "given a valid ISO date string in an Option" in {
        val expected = Valid(Some(LocalDate.parse(validDate)))

        val result = ResolveIsoDate(Option(validDate), DateFormatError)
        result shouldBe expected
      }

      "given a valid ISO date string in an Option and a pre-supplied MtdError" in {
        val expected = Valid(Some(LocalDate.parse(validDate)))

        val result = ResolveIsoDate(DateFormatError)(Option(validDate))
        result shouldBe expected
      }
    }

    "return an error" when {
      "given an invalid/non-ISO date string" in {
        val invalidDate = "not-a-date"
        val result      = ResolveIsoDate(invalidDate, DateFormatError)
        result shouldBe Invalid(List(DateFormatError))
      }
    }

    "return None" when {
      "given None as input" in {
        val expected = Valid(None)

        val result: Validated[Seq[MtdError], Option[LocalDate]] = ResolveIsoDate(None, DateFormatError)
        result shouldBe expected
      }
    }
  }

}
