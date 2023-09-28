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
import api.controllers.validators.resolvers._
import api.models.domain.TaxYear
import api.models.errors.MtdError
import cats.data.Validated
import cats.implicits.catsSyntaxTuple3Semigroupal
import play.api.libs.json.JsValue
import v1.controllers.validators.CreateAndAmendForeignReliefsRulesValidator.validateBusinessRules
import v1.models.request.createAndAmendForeignReliefs._

import javax.inject.Singleton
import scala.annotation.nowarn

@Singleton
class CreateAndAmendForeignReliefsValidatorFactory {

  @nowarn("cat=lint-byname-implicit")
  private val resolveJson = new ResolveNonEmptyJsonObject[CreateAndAmendForeignReliefsBody]()

  def validator(nino: String, taxYear: String, body: JsValue): Validator[CreateAndAmendForeignReliefsRequestData] =
    new Validator[CreateAndAmendForeignReliefsRequestData] {

      def validate: Validated[Seq[MtdError], CreateAndAmendForeignReliefsRequestData] =
        (
          ResolveNino(nino),
          ResolveTaxYear(TaxYear.minimumTaxYear.year, taxYear, None, None),
          resolveJson(body)
        ).mapN(CreateAndAmendForeignReliefsRequestData) andThen validateBusinessRules

    }

}
