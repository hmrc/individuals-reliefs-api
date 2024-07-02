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

package v1.controllers.validators

import api.controllers.validators.Validator
import api.models.errors.MtdError
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import play.api.libs.json.JsValue
import v1.CreateAndAmendForeignReliefs.CreateAndAmendForeignReliefsValidatorFactory
import v1.CreateAndAmendForeignReliefs.model.request.CreateAndAmendForeignReliefsRequestData

trait MockCreateAndAmendForeignReliefsValidatorFactory extends MockFactory {

  val mockCreateAndAmendForeignReliefsValidatorFactory: CreateAndAmendForeignReliefsValidatorFactory =
    mock[CreateAndAmendForeignReliefsValidatorFactory]

  object MockedCreateAndAmendForeignReliefsValidatorFactory {

    def validator(): CallHandler[Validator[CreateAndAmendForeignReliefsRequestData]] =
      (mockCreateAndAmendForeignReliefsValidatorFactory.validator(_: String, _: String, _: JsValue)).expects(*, *, *)

  }

  def willUseValidator(use: Validator[CreateAndAmendForeignReliefsRequestData]): CallHandler[Validator[CreateAndAmendForeignReliefsRequestData]] = {
    MockedCreateAndAmendForeignReliefsValidatorFactory
      .validator()
      .anyNumberOfTimes()
      .returns(use)
  }

  def returningSuccess(result: CreateAndAmendForeignReliefsRequestData): Validator[CreateAndAmendForeignReliefsRequestData] =
    new Validator[CreateAndAmendForeignReliefsRequestData] {
      def validate: Validated[Seq[MtdError], CreateAndAmendForeignReliefsRequestData] = Valid(result)
    }

  def returning(result: MtdError*): Validator[CreateAndAmendForeignReliefsRequestData] = returningErrors(result)

  def returningErrors(result: Seq[MtdError]): Validator[CreateAndAmendForeignReliefsRequestData] =
    new Validator[CreateAndAmendForeignReliefsRequestData] {
      def validate: Validated[Seq[MtdError], CreateAndAmendForeignReliefsRequestData] = Invalid(result)
    }

}
