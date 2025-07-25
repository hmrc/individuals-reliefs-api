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

package v3.pensionReliefs.delete

import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import shared.controllers.validators.Validator
import shared.models.errors.MtdError
import v3.pensionReliefs.delete.model.request.DeletePensionsReliefsRequestData

trait MockDeletePensionsReliefsValidatorFactory extends TestSuite with MockFactory {

  val mockDeletePensionsReliefsValidatorFactory: DeletePensionsReliefsValidatorFactory =
    mock[DeletePensionsReliefsValidatorFactory]

  object MockedDeletePensionsReliefsValidatorFactory {

    def validator(): CallHandler[Validator[DeletePensionsReliefsRequestData]] =
      (mockDeletePensionsReliefsValidatorFactory.validator(_: String, _: String)).expects(*, *)

  }

  def willUseValidator(use: Validator[DeletePensionsReliefsRequestData]): CallHandler[Validator[DeletePensionsReliefsRequestData]] = {
    MockedDeletePensionsReliefsValidatorFactory
      .validator()
      .anyNumberOfTimes()
      .returns(use)
  }

  def returningSuccess(result: DeletePensionsReliefsRequestData): Validator[DeletePensionsReliefsRequestData] =
    new Validator[DeletePensionsReliefsRequestData] {
      def validate: Validated[Seq[MtdError], DeletePensionsReliefsRequestData] = Valid(result)
    }

  def returning(result: MtdError*): Validator[DeletePensionsReliefsRequestData] = returningErrors(result)

  def returningErrors(result: Seq[MtdError]): Validator[DeletePensionsReliefsRequestData] =
    new Validator[DeletePensionsReliefsRequestData] {
      def validate: Validated[Seq[MtdError], DeletePensionsReliefsRequestData] = Invalid(result)
    }

}
