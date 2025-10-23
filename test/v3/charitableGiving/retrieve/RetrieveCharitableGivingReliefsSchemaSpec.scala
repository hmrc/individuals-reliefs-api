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

package v3.charitableGiving.retrieve

import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import shared.models.domain.{TaxYear, TaxYearPropertyCheckSupport}
import shared.utils.UnitSpec
import v3.charitableGiving.retrieve.RetrieveCharitableGivingReliefsSchema.*
import cats.data.Validated.{Invalid, Valid}
import shared.models.errors.*

class RetrieveCharitableGivingReliefsSchemaSpec extends UnitSpec with ScalaCheckDrivenPropertyChecks with TaxYearPropertyCheckSupport {

  "schema lookup" when {
    "a valid tax year is supplied" must {
      "use Def1 schema for tax years between 2017-18 and 2024-25" in {
        forTaxYearsInRange(TaxYear.fromMtd("2017-18"), TaxYear.fromMtd("2024-25")) { taxYear =>
          schemaFor(taxYear.asMtd) shouldBe Valid(Def1)
        }
      }

      "use Def2 schema for tax years 2025-26 onwards" in {
        forTaxYearsFrom(TaxYear.fromMtd("2025-26")) { taxYear =>
          schemaFor(taxYear.asMtd) shouldBe Valid(Def2)
        }
      }

    }

    "handle errors" when {
      "an invalid tax year is supplied" must {
        "disallow tax years prior to 2017-18 and return RuleTaxYearNotSupportedError" in {
          forTaxYearsBefore(TaxYear.fromMtd("2017-18")) { taxYear =>
            schemaFor(taxYear.asMtd) shouldBe Invalid(Seq(RuleTaxYearNotSupportedError))
          }
        }
      }

      "the tax year format is invalid" must {
        "return a TaxYearFormatError" in {
          schemaFor("NotATaxYear") shouldBe Invalid(Seq(TaxYearFormatError))
        }

        "the tax year range is invalid" must {
          "return a RuleTaxYearRangeInvalidError" in {
            schemaFor("2020-99") shouldBe Invalid(Seq(RuleTaxYearRangeInvalidError))
          }
        }
      }
    }
  }

}
