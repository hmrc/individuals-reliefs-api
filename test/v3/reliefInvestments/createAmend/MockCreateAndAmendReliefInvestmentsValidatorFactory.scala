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

package v3.reliefInvestments.createAmend

import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import play.api.libs.json.JsValue
import shared.controllers.validators.Validator
import shared.models.errors.MtdError
import v3.reliefInvestments.createAmend.model.request.CreateAndAmendReliefInvestmentsRequestData

trait MockCreateAndAmendReliefInvestmentsValidatorFactory extends TestSuite with MockFactory {

  val mockCreateAndAmendReliefInvestmentsValidatorFactory: CreateAndAmendReliefInvestmentsValidatorFactory =
    mock[CreateAndAmendReliefInvestmentsValidatorFactory]

  object MockedCreateAndAmendReliefInvestmentsValidatorFactory {

    def validator(): CallHandler[Validator[CreateAndAmendReliefInvestmentsRequestData]] =
      (mockCreateAndAmendReliefInvestmentsValidatorFactory.validator(_: String, _: String, _: JsValue)).expects(*, *, *)

  }

  def willUseValidator(
      use: Validator[CreateAndAmendReliefInvestmentsRequestData]): CallHandler[Validator[CreateAndAmendReliefInvestmentsRequestData]] = {
    MockedCreateAndAmendReliefInvestmentsValidatorFactory
      .validator()
      .anyNumberOfTimes()
      .returns(use)
  }

  def returningSuccess(result: CreateAndAmendReliefInvestmentsRequestData): Validator[CreateAndAmendReliefInvestmentsRequestData] =
    new Validator[CreateAndAmendReliefInvestmentsRequestData] {
      def validate: Validated[Seq[MtdError], CreateAndAmendReliefInvestmentsRequestData] = Valid(result)
    }

  def returning(result: MtdError*): Validator[CreateAndAmendReliefInvestmentsRequestData] = returningErrors(result)

  def returningErrors(result: Seq[MtdError]): Validator[CreateAndAmendReliefInvestmentsRequestData] =
    new Validator[CreateAndAmendReliefInvestmentsRequestData] {
      def validate: Validated[Seq[MtdError], CreateAndAmendReliefInvestmentsRequestData] = Invalid(result)
    }

}
