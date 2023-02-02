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

package v1.controllers.requestParsers

import api.models.domain.{Nino, TaxYear}
import api.models.errors
import api.models.errors.{BadRequestError, NinoFormatError, TaxYearFormatError}
import play.api.libs.json.Json
import support.UnitSpec
import v1.fixtures.CreateAndAmendForeignReliefsFixtures.{requestBodyJson, requestBodyModel}
import v1.mocks.validators.MockCreateAndAmendForeignReliefsValidator
import v1.models.request.createAndAmendForeignReliefs._

class CreateAndAmendForeignReliefsRequestParserSpec extends UnitSpec {
  private val nino                   = "AA123456A"
  private val taxYear                = "2021-22"
  implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  val inputData: CreateAndAmendForeignReliefsRawData =
    CreateAndAmendForeignReliefsRawData(nino, taxYear, requestBodyJson)

  val inputNone: CreateAndAmendForeignReliefsRawData =
    CreateAndAmendForeignReliefsRawData(nino, taxYear, Json.obj())

  trait Test extends MockCreateAndAmendForeignReliefsValidator {
    lazy val parser = new CreateAndAmendForeignReliefsRequestParser(mockValidator)
  }

  "parse" should {

    "return a request object" when {
      "valid request data is supplied" in new Test {
        MockCreateAndAmendForeignReliefsValidator.validate(inputData).returns(Nil)

        parser.parseRequest(inputData) shouldBe Right(CreateAndAmendForeignReliefsRequest(Nino(nino), TaxYear.fromMtd(taxYear), requestBodyModel))
      }
    }

    "return an ErrorWrapper" when {

      "a single validation error occurs" in new Test {
        MockCreateAndAmendForeignReliefsValidator
          .validate(inputData)
          .returns(List(NinoFormatError))

        parser.parseRequest(inputData) shouldBe
          Left(errors.ErrorWrapper(correlationId, NinoFormatError, None))
      }

      "multiple validation errors occur" in new Test {
        MockCreateAndAmendForeignReliefsValidator
          .validate(inputData)
          .returns(List(NinoFormatError, TaxYearFormatError))

        parser.parseRequest(inputData) shouldBe
          Left(errors.ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, TaxYearFormatError))))
      }
    }
  }

}