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

package v1.controllers.requestParsers.validators.validations

import support.UnitSpec
import v1.models.errors.{DateOfInvestmentFormatError, DateFormatError}

class DateValidationSpec extends UnitSpec {

  val validDate: Option[String]   = Some("2018-04-06")
  val invalidDate: Option[String] = Some("04-06-2018")

  "validate" should {
    "return no errors" when {
      "a valid date is supplied" in {
        val validationResult = DateValidation.validateOptional(validDate, "/vctSubscription/0/dateOfInvestment", DateOfInvestmentFormatError)

        validationResult.isEmpty shouldBe true
      }
      "no valid date is supplied" in {
        val validationResult = DateValidation.validateOptional(None, "/maintenancePayments/0/exSpouseDateOfBirth", DateFormatError)

        validationResult.isEmpty shouldBe true
      }
    }

    "return an error" when {
      "a invalid date is supplied" in {
        val validationResult = DateValidation.validateOptional(invalidDate, "/vctSubscription/0/dateOfInvestment", DateOfInvestmentFormatError)

        validationResult.isEmpty shouldBe false
        validationResult.length shouldBe 1
        validationResult.head shouldBe DateOfInvestmentFormatError.copy(paths = Some(Seq("/vctSubscription/0/dateOfInvestment")))
      }
    }
  }

}
