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
import v1.models.request.createAndAmendCharitableGivingTaxRelief.NonUkCharities

class NonUkCharitiesValidationSpec extends UnitSpec {

  "NonUkCharitiesValidation" when {
    "checking for missing names" must {
      "return false" when {
        "amount > 0 and names are specified" in {
          NonUkCharitiesValidation.hasMissingNames(NonUkCharities(Some(Seq("Some charity")), 1)) shouldBe false
        }

        "amount = 0 and names are specified" in {
          NonUkCharitiesValidation.hasMissingNames(NonUkCharities(Some(Seq("Some charity")), 0)) shouldBe false
        }

        "amount = 0 and names are not specified" in {
          NonUkCharitiesValidation.hasMissingNames(NonUkCharities(None, 0)) shouldBe false
        }
      }

      "return true" when {
        "amount > 0 and names sequence is not present" in {
          NonUkCharitiesValidation.hasMissingNames(NonUkCharities(None, 1)) shouldBe true
        }

        "amount > 0 and names sequence is empty" in {
          NonUkCharitiesValidation.hasMissingNames(NonUkCharities(Some(Nil), 1)) shouldBe true
        }
      }
    }

    "validating a charity name" must {
      "return false" when {
        "the name does not satisfy the regex" in {
          NonUkCharitiesValidation.isNameValid("") shouldBe false
          NonUkCharitiesValidation.isNameValid("A" * 76) shouldBe false
        }
      }

      "return true" when {
        "the name does satisfy the regex" in {
          NonUkCharitiesValidation.isNameValid("A") shouldBe true
          NonUkCharitiesValidation.isNameValid("A" * 75) shouldBe true
          NonUkCharitiesValidation.isNameValid("ABCabc0123 &'()*,-./@£") shouldBe true
        }
      }
    }
  }

}
