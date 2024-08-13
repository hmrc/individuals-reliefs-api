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

package v1.deleteForeignReliefs

import api.controllers.validators.Validator
import api.models.errors.MtdError
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import v1.deleteForeignReliefs.model.DeleteForeignReliefsRequestData

trait MockDeleteForeignReliefsValidatorFactory extends MockFactory {

  val mockDeleteForeignReliefsValidatorFactory: DeleteForeignReliefsValidatorFactory =
    mock[DeleteForeignReliefsValidatorFactory]

  object MockedDeleteForeignReliefsValidatorFactory {

    def validator(): CallHandler[Validator[DeleteForeignReliefsRequestData]] =
      (mockDeleteForeignReliefsValidatorFactory.validator(_: String, _: String)).expects(*, *)

  }

  def willUseValidator(use: Validator[DeleteForeignReliefsRequestData]): CallHandler[Validator[DeleteForeignReliefsRequestData]] = {
    MockedDeleteForeignReliefsValidatorFactory
      .validator()
      .anyNumberOfTimes()
      .returns(use)
  }

  def returningSuccess(result: DeleteForeignReliefsRequestData): Validator[DeleteForeignReliefsRequestData] =
    new Validator[DeleteForeignReliefsRequestData] {
      def validate: Validated[Seq[MtdError], DeleteForeignReliefsRequestData] = Valid(result)
    }

  def returning(result: MtdError*): Validator[DeleteForeignReliefsRequestData] = returningErrors(result)

  def returningErrors(result: Seq[MtdError]): Validator[DeleteForeignReliefsRequestData] =
    new Validator[DeleteForeignReliefsRequestData] {
      def validate: Validated[Seq[MtdError], DeleteForeignReliefsRequestData] = Invalid(result)
    }

}
