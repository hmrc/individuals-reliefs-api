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

package v3.foreignReliefs.retrieve

import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import shared.controllers.validators.Validator
import shared.models.errors.MtdError
import v3.foreignReliefs.retrieve.model.request.RetrieveForeignReliefsRequestData

trait MockRetrieveForeignReliefsValidatorFactory extends TestSuite with MockFactory {

  val mockRetrieveForeignReliefsValidatorFactory: RetrieveForeignReliefsValidatorFactory = mock[RetrieveForeignReliefsValidatorFactory]

  object MockedRetrieveForeignReliefsValidatorFactory {

    def validator(): CallHandler[Validator[RetrieveForeignReliefsRequestData]] =
      (mockRetrieveForeignReliefsValidatorFactory.validator(_: String, _: String)).expects(*, *)

  }

  def willUseValidator(use: Validator[RetrieveForeignReliefsRequestData]): CallHandler[Validator[RetrieveForeignReliefsRequestData]] = {
    MockedRetrieveForeignReliefsValidatorFactory
      .validator()
      .anyNumberOfTimes()
      .returns(use)
  }

  def returningSuccess(result: RetrieveForeignReliefsRequestData): Validator[RetrieveForeignReliefsRequestData] =
    new Validator[RetrieveForeignReliefsRequestData] {
      def validate: Validated[Seq[MtdError], RetrieveForeignReliefsRequestData] = Valid(result)
    }

  def returning(result: MtdError*): Validator[RetrieveForeignReliefsRequestData] = returningErrors(result)

  def returningErrors(result: Seq[MtdError]): Validator[RetrieveForeignReliefsRequestData] =
    new Validator[RetrieveForeignReliefsRequestData] {
      def validate: Validated[Seq[MtdError], RetrieveForeignReliefsRequestData] = Invalid(result)
    }

}
