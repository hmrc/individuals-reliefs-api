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

package api.controllers.validators

import api.models.errors.MtdError
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import play.api.http.Status.BAD_REQUEST
import support.UnitSpec

class ValidatorOpsSpec extends UnitSpec with ValidatorOps {

  private object InvalidFieldError extends MtdError("INVALID", "Field valid1 is invalid", BAD_REQUEST)

  "validateWithPaths" should {
    "validate fields with paths correctly" in {
      val invalid = Invalid(List(InvalidFieldError.withPath("/invalidSomeValueWithPath")))

      val validSomeValueWithPath   = (Some(5), "/validSomeValueWithPath")
      val invalidSomeValueWithPath = (Some(10), "/invalidSomeValueWithPath")

      val noneValueWithPath = (Some(5), "/noneValueWithPath")

      def someValidation(number: Int, path: Option[String]): Validated[Seq[MtdError], Int] = {
        if (number > 5) Invalid(List(InvalidFieldError.withPath(path.getOrElse(""))))
        else Valid(number)
      }

      val validResult   = validateWithPaths(validSomeValueWithPath, noneValueWithPath)(someValidation)
      val invalidResult = validateWithPaths(validSomeValueWithPath, noneValueWithPath, invalidSomeValueWithPath)(someValidation)

      validResult shouldBe Valid(())
      invalidResult shouldBe invalid
    }
  }

  "ValidatorOptionOps" should {
    val invalid = Invalid(List(InvalidFieldError))

    "map over the Option value or return the provided default for Validated" in {
      val defaultValid = Valid(10)

      val someValue: Option[Int] = Some(5)
      val noneValue: Option[Int] = None

      val someResult1 = someValue.mapOrElse(v => Valid(v * 2), defaultValid)
      val someResult2 = someValue.mapOrElse(_ => invalid, defaultValid)

      val noneResult1 = noneValue.mapOrElse(v => Valid(v * 2), defaultValid)
      val noneResult2 = noneValue.mapOrElse(_ => invalid, defaultValid)

      someResult1 shouldBe defaultValid
      someResult2 shouldBe invalid

      noneResult1 shouldBe defaultValid
      noneResult2 shouldBe defaultValid
    }

    "map over the Option value or return the unit for Validated" in {
      val valid = Valid(())

      val someValue: Option[Int] = Some(5)
      val noneValue: Option[Int] = None

      val someResult = someValue.mapOrElse(_ => valid)
      val noneResult = noneValue.mapOrElse(_ => valid)

      someResult shouldBe valid
      noneResult shouldBe valid
    }
  }

  "ValidatorOptionSeqOps" should {
    val invalid = Invalid(List(InvalidFieldError))

    "zip and validate the sequence or return the provided default for Validated" in {
      val defaultValid = Valid(List(10, 20, 30))

      val someList: Option[Seq[Int]] = Some(List(1, 2, 3))
      val noneList: Option[Seq[Int]] = None

      val someResult = someList.zipAndValidate((v, idx) => Valid(v * (idx + 1)), defaultValid)
      val noneResult = noneList.zipAndValidate((v, idx) => Valid(v * (idx + 1)), defaultValid)

      someResult shouldBe Valid(List(1, 4, 9))
      noneResult shouldBe defaultValid
    }

    "zip and validate the sequence or return the unit for Validated" in {
      val validUnit = Valid(())

      val someList: Option[Seq[Int]] = Some(List(1, 2, 3))
      val noneList: Option[Seq[Int]] = None

      val someInvalidResult = someList.zipAndValidate((_, _) => invalid)
      val someValidResult   = someList.zipAndValidate((_, _) => Valid(()))
      val noneResult        = noneList.zipAndValidate((_, _) => invalid)

      someInvalidResult shouldBe Invalid(List(InvalidFieldError, InvalidFieldError, InvalidFieldError))
      someValidResult shouldBe validUnit
      noneResult shouldBe validUnit
    }
  }

  "ValidatorSeqOps" should {
    "zip and validate the sequence correctly" in {
      val invalid = Invalid(List(InvalidFieldError.withPath("/pathWithIndex/2")))

      val validFields   = List(1, 2, 3)
      val invalidFields = List(1, 2, 10)

      def someValidation(number: Int, index: Int): Validated[Seq[MtdError], Unit] = {
        if (number > 5) Invalid(List(InvalidFieldError.withPath(s"/pathWithIndex/$index")))
        else Valid(())
      }

      val validResult   = validFields.zipAndValidate(someValidation)
      val invalidResult = invalidFields.zipAndValidate(someValidation)

      validResult shouldBe Valid(())
      invalidResult shouldBe invalid
    }
  }

}
