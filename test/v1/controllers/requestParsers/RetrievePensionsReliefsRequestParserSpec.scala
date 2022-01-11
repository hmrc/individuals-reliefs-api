/*
 * Copyright 2022 HM Revenue & Customs
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

import support.UnitSpec
import v1.models.domain.Nino
import v1.mocks.validators.MockRetrievePensionsReliefsValidator
import v1.models.errors.{BadRequestError, ErrorWrapper, NinoFormatError, TaxYearFormatError}
import v1.models.request.retrievePensionsReliefs.{RetrievePensionsReliefsRawData, RetrievePensionsReliefsRequest}

class RetrievePensionsReliefsRequestParserSpec extends UnitSpec {
  val nino: String = "AA123456B"
  val taxYear: String = "2018-19"
  implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  val inputData: RetrievePensionsReliefsRawData = RetrievePensionsReliefsRawData(nino, taxYear)

  trait Test extends MockRetrievePensionsReliefsValidator {
    lazy val parser = new RetrievePensionsReliefsRequestParser(mockValidator)
  }

  "parse" should {

    "return a request object" when {
      "valid request data is supplied" in new Test {
        MockRetrievePensionsReliefsValidator.validate(inputData).returns(Nil)

        parser.parseRequest(inputData) shouldBe
        Right(RetrievePensionsReliefsRequest(Nino(nino), taxYear))
      }
    }

    "return an errorWrapper" when {

      "a single validation error occurs" in new Test {
        MockRetrievePensionsReliefsValidator.validate(inputData)
          .returns(List(NinoFormatError))

        parser.parseRequest(inputData) shouldBe
        Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }

      "multiple validation errors occur" in new Test {
        MockRetrievePensionsReliefsValidator.validate(inputData)
          .returns(List(NinoFormatError, TaxYearFormatError))

        parser.parseRequest(inputData) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, TaxYearFormatError))))
      }
    }
  }
}
