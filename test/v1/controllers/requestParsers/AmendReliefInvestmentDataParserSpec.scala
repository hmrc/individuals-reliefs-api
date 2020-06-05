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
import v1.mocks.validators.MockAmendReliefInvestmentValidator
import v1.models.errors.{BadRequestError, ErrorWrapper, NinoFormatError, TaxYearFormatError}
import v1.models.request.amendReliefInvestments.{AmendReliefInvestmentsBody, AmendReliefInvestmentsRawData, AmendReliefInvestmentsRequest, CommunityInvestmentItem, EisSubscriptionsItem, SeedEnterpriseInvestmentItem, SocialEnterpriseInvestmentItem, VctSubscriptionsItem}

class AmendReliefInvestmentDataParserSpec extends UnitSpec {
  private val nino = "AA123456A"
  private val taxYear = "2018-19"
  private val requestBodyJson = Json.parse(
    """
      |{
      |  "vctSubscriptionsItems":[
      |    {
      |      "uniqueInvestmentRef": "VCTREF",
      |      "name": "VCT Fund X",
      |      "dateOfInvestment": "2018-04-16",
      |      "amountInvested": 23312.00,
      |      "reliefClaimed": 1334.00
      |      }
      |  ],
      |  "eisSubscriptionsItems":[
      |    {
      |      "uniqueInvestmentRef": "XTAL",
      |      "name": "EIS Fund X",
      |      "knowledgeIntensive": true,
      |      "dateOfInvestment": "2020-12-12",
      |      "amountInvested": 23312.00,
      |      "reliefClaimed": 43432.00
      |    }
      |  ],
      |  "communityInvestmentItems": [
      |    {
      |      "uniqueInvestmentRef": "CIREF",
      |      "name": "CI X",
      |      "dateOfInvestment": "2020-12-12",
      |      "amountInvested": 6442.00,
      |      "reliefClaimed": 2344.00
      |    }
      |  ],
      |  "seedEnterpriseInvestmentItems": [
      |    {
      |      "uniqueInvestmentRef": "123412/1A",
      |      "companyName": "Company Inc",
      |      "dateOfInvestment": "2020-12-12",
      |      "amountInvested": 123123.22,
      |      "reliefClaimed": 3432.00
      |    }
      |  ],
      |  "socialEnterpriseInvestmentItems": [
      |    {
      |      "uniqueInvestmentRef": "123412/1A",
      |      "socialEnterpriseName": "SE Inc",
      |      "dateOfInvestment": "2020-12-12",
      |      "amountInvested": 123123.22,
      |      "reliefClaimed": 3432.00
      |    }
      |  ]
      |}
        """.stripMargin)



  val inputData =
    AmendReliefInvestmentsRawData(nino, taxYear, requestBodyJson)

  trait Test extends MockAmendReliefInvestmentValidator {
    lazy val parser = new AmendReliefInvestmentsRequestParser(mockValidator)
  }

  "parse" should {

    "return a request object" when {
      "valid request data is supplied" in new Test {
        MockAmendReliefInvestmentValidator.validate(inputData).returns(Nil)

        parser.parseRequest(inputData) shouldBe
          Right(AmendReliefInvestmentsRequest(Nino(nino),taxYear,AmendReliefInvestmentsBody(
            Some(Seq(VctSubscriptionsItem(Some("VCTREF"), Some("VCT Fund X"), Some("2018-04-16"), Some(23312.00), Some(1334.00)))),
            Some(Seq(EisSubscriptionsItem(Some("XTAL"), Some("EIS Fund X"), Some(true), Some("2020-12-12"), Some(23312.00), Some(43432.00)))),
            Some(Seq(CommunityInvestmentItem(Some("CIREF"), Some("CI X"), Some("2020-12-12"), Some(6442.00), Some(2344.00)))),
            Some(Seq(SeedEnterpriseInvestmentItem(Some("123412/1A"), Some("Company Inc"), Some("2020-12-12"), Some(123123.22), Some(3432.00)))),
            Some(Seq(SocialEnterpriseInvestmentItem(Some("123412/1A"), Some("SE Inc"), Some("2020-12-12"), Some(123123.22), Some(3432.00))))
          )))
      }
    }
    "return an ErrorWrapper" when {

      "a single validation error occurs" in new Test {
        MockAmendReliefInvestmentValidator.validate(inputData)
          .returns(List(NinoFormatError))

        parser.parseRequest(inputData) shouldBe
          Left(ErrorWrapper(None, NinoFormatError, None))
      }

      "multiple validation errors occur" in new Test {
        MockAmendReliefInvestmentValidator.validate(inputData)
          .returns(List(NinoFormatError, TaxYearFormatError))

        parser.parseRequest(inputData) shouldBe
          Left(ErrorWrapper(None, BadRequestError, Some(Seq(NinoFormatError, TaxYearFormatError))))
      }
    }
  }
}