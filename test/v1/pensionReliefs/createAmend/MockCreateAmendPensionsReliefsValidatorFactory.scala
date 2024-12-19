/*
 * Copyright 2024 HM Revenue & Customs
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

package v1.pensionReliefs.createAmend

import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import play.api.libs.json.JsValue
import shared.controllers.validators.Validator
import shared.models.errors.MtdError
import v1.pensionReliefs.createAmend.model.request.CreateAmendPensionsReliefsRequestData

trait MockCreateAmendPensionsReliefsValidatorFactory extends MockFactory {

  val mockAmendPensionsReliefsValidatorFactory: CreateAmendPensionsReliefsValidatorFactory = mock[CreateAmendPensionsReliefsValidatorFactory]

  object MockedAmendPensionsReliefsValidatorFactory {

    def validator(): CallHandler[Validator[CreateAmendPensionsReliefsRequestData]] =
      (mockAmendPensionsReliefsValidatorFactory.validator(_: String, _: String, _: JsValue)).expects(*, *, *)

  }

  def willUseValidator(use: Validator[CreateAmendPensionsReliefsRequestData]): CallHandler[Validator[CreateAmendPensionsReliefsRequestData]] = {
    MockedAmendPensionsReliefsValidatorFactory
      .validator()
      .anyNumberOfTimes()
      .returns(use)
  }

  def returningSuccess(result: CreateAmendPensionsReliefsRequestData): Validator[CreateAmendPensionsReliefsRequestData] =
    new Validator[CreateAmendPensionsReliefsRequestData] {
      def validate: Validated[Seq[MtdError], CreateAmendPensionsReliefsRequestData] = Valid(result)
    }

  def returning(result: MtdError*): Validator[CreateAmendPensionsReliefsRequestData] = returningErrors(result)

  def returningErrors(result: Seq[MtdError]): Validator[CreateAmendPensionsReliefsRequestData] =
    new Validator[CreateAmendPensionsReliefsRequestData] {
      def validate: Validated[Seq[MtdError], CreateAmendPensionsReliefsRequestData] = Invalid(result)
    }

}
