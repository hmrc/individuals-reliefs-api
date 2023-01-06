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

import play.api.libs.json.Json
import support.UnitSpec
import v1.mocks.validators.MockAmendCharitableGivingReliefValidator
import v1.models.domain.Nino
import v1.models.errors.{BadRequestError, ErrorWrapper, NinoFormatError, TaxYearFormatError}
import v1.models.request.TaxYear
import v1.models.request.createAndAmendCharitableGivingTaxRelief.{
  CreateAndAmendCharitableGivingTaxReliefBody,
  CreateAndAmendCharitableGivingTaxReliefRawData,
  CreateAndAmendCharitableGivingTaxReliefRequest,
  GiftAidPayments
}

class AmendCharitableGivingRequestReliefParserSpec extends UnitSpec {
  private val nino                   = "AA123456A"
  private val taxYear                = "2018-19"
  implicit val correlationId: String = "corr-id"

  private val requestBodyJson = Json.parse(s"""
        |{
        |   "giftAidPayments": {
        |      "totalAmount": 1.23
        |   }
        |}
        |""".stripMargin)

  val rawData: CreateAndAmendCharitableGivingTaxReliefRawData =
    CreateAndAmendCharitableGivingTaxReliefRawData(nino, taxYear, requestBodyJson)

  val request: CreateAndAmendCharitableGivingTaxReliefRequest = CreateAndAmendCharitableGivingTaxReliefRequest(
    Nino(nino),
    TaxYear.fromMtd(taxYear),
    CreateAndAmendCharitableGivingTaxReliefBody(
      Some(GiftAidPayments(nonUkCharities = None, totalAmount = Some(1.23), None, None, None)),
      None
    )
  )

  trait Test extends MockAmendCharitableGivingReliefValidator {
    lazy val parser = new AmendCharitableGivingRequestReliefParser(mockValidator)
  }

  "parse" should {
    "return a request object" when {
      "valid request data is supplied" in new Test {
        MockAmendCharitableGivingReliefValidator.validate(rawData) returns Nil

        parser.parseRequest(rawData) shouldBe Right(request)
      }
    }

    "return an ErrorWrapper" when {
      "a single validation error occurs" in new Test {
        MockAmendCharitableGivingReliefValidator.validate(rawData) returns List(NinoFormatError)

        parser.parseRequest(rawData) shouldBe Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }

      "multiple validation errors occur" in new Test {
        MockAmendCharitableGivingReliefValidator.validate(rawData) returns List(NinoFormatError, TaxYearFormatError)

        parser.parseRequest(rawData) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, TaxYearFormatError))))
      }
    }
  }

}
