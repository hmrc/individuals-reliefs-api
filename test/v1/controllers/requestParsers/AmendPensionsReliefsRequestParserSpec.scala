/*
 * Copyright 2020 HM Revenue & Customs
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

import play.api.libs.json.Json
import support.UnitSpec
import uk.gov.hmrc.domain.Nino
import v1.mocks.validators.MockAmendPensionsReliefsValidator
import v1.models.errors.{BadRequestError, ErrorWrapper, NinoFormatError, TaxYearFormatError}
import v1.models.request.amendPensionsReliefs._

class AmendPensionsReliefsRequestParserSpec extends UnitSpec {
  private val nino = "AA123456A"
  private val taxYear = "2019-20"
  private val requestBodyJson = Json.parse(
    """
      |{
      |  "pensionReliefs": {
      |    "regularPensionContributions": 1999.99,
      |    "oneOffPensionContributionsPaid": 1999.99,
      |    "retirementAnnuityPayments": 1999.99,
      |    "paymentToEmployersSchemeNoTaxRelief": 1999.99,
      |    "overseasPensionSchemeContributions": 1999.99
      |  }
      |}""".stripMargin)



  val inputData =
    AmendPensionsReliefsRawData(nino, taxYear, requestBodyJson)

  val inputNone =
    AmendPensionsReliefsRawData(nino, taxYear, Json.obj())

  trait Test extends MockAmendPensionsReliefsValidator {
    lazy val parser = new AmendPensionsReliefsRequestParser(mockValidator)
  }

  "parse" should {

    "return a request object" when {
      "valid request data is supplied" in new Test {
        MockAmendPensionsReliefsValidator.validate(inputData).returns(Nil)

        parser.parseRequest(inputData) shouldBe
          Right(AmendPensionsReliefsRequest(Nino(nino), taxYear, AmendPensionsReliefsBody(
            pensionReliefs = PensionReliefs(
              regularPensionContributions = Some(1999.99),
              oneOffPensionContributionsPaid = Some(1999.99),
              retirementAnnuityPayments = Some(1999.99),
              paymentToEmployersSchemeNoTaxRelief = Some(1999.99),
              overseasPensionSchemeContributions = Some(1999.99)
            )
          )))
      }
    }
    "return an ErrorWrapper" when {

      "a single validation error occurs" in new Test {
        MockAmendPensionsReliefsValidator.validate(inputData)
          .returns(List(NinoFormatError))

        parser.parseRequest(inputData) shouldBe
          Left(ErrorWrapper(None, NinoFormatError, None))
      }

      "multiple validation errors occur" in new Test {
        MockAmendPensionsReliefsValidator.validate(inputData)
          .returns(List(NinoFormatError, TaxYearFormatError))

        parser.parseRequest(inputData) shouldBe
          Left(ErrorWrapper(None, BadRequestError, Some(Seq(NinoFormatError, TaxYearFormatError))))
      }
    }
  }
}