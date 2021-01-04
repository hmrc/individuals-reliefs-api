/*
 * Copyright 2021 HM Revenue & Customs
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
import v1.mocks.validators.MockAmendForeignReliefsValidator
import v1.models.errors.{BadRequestError, ErrorWrapper, NinoFormatError, TaxYearFormatError}
import v1.models.request.amendForeignReliefs._

class AmendForeignReliefsRequestParserSpec extends UnitSpec {
  private val nino = "AA123456A"
  private val taxYear = "2018-19"
  implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"
  val amount: BigDecimal = 1234.56
  private val requestBodyJson = Json.parse(
    s"""|
        |{
        |  "foreignTaxCreditRelief": {
        |    "amount": $amount
        |  },
        |  "foreignIncomeTaxCreditRelief": {
        |    "countryCode": "FRA",
        |    "foreignTaxPaid": $amount,
        |    "taxableAmount": $amount,
        |    "employmentLumpSum": true
        |  },
        |  "foreignTaxForFtcrNotClaimed": {
        |    "amount": $amount
        |  }
        |}
        |""".stripMargin)

  val inputData: AmendForeignReliefsRawData =
    AmendForeignReliefsRawData(nino, taxYear, requestBodyJson)

  val inputNone: AmendForeignReliefsRawData =
    AmendForeignReliefsRawData(nino, taxYear, Json.obj())

  trait Test extends MockAmendForeignReliefsValidator {
    lazy val parser = new AmendForeignReliefsRequestParser(mockValidator)
  }

  "parse" should {

    "return a request object" when {
      "valid request data is supplied" in new Test {
        MockAmendForeignReliefsValidator.validate(inputData).returns(Nil)

        parser.parseRequest(inputData) shouldBe
          Right(AmendForeignReliefsRequest(Nino(nino), taxYear, AmendForeignReliefsBody(
            foreignTaxCreditRelief = Some(ForeignTaxCreditRelief(
              amount = amount
            )),
            foreignIncomeTaxCreditRelief = Some(ForeignIncomeTaxCreditRelief(
              countryCode = Some("FRA"),
              foreignTaxPaid = Some(amount),
              taxableAmount = Some(amount),
              employmentLumpSum = true
            )), foreignTaxForFtcrNotClaimed = Some(ForeignTaxForFtcrNotClaimed(
              amount = amount
            ))
          )))
      }
    }
    "return an ErrorWrapper" when {

      "a single validation error occurs" in new Test {
        MockAmendForeignReliefsValidator.validate(inputData)
          .returns(List(NinoFormatError))

        parser.parseRequest(inputData) shouldBe
          Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }

      "multiple validation errors occur" in new Test {
        MockAmendForeignReliefsValidator.validate(inputData)
          .returns(List(NinoFormatError, TaxYearFormatError))

        parser.parseRequest(inputData) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, TaxYearFormatError))))
      }
    }
  }
}