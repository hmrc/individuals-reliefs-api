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
import v1.models.requestData.amendReliefInvestments.{AmendReliefInvestmentsBody, AmendReliefInvestmentsRawData, AmendReliefInvestmentsRequest}

class AmendReliefInvestmentDataParserSpec extends UnitSpec {
  private val nino = "AA123456A"
  private val taxYear = "2018-19"
  private val requestData =
    AmendReliefInvestmentsBody(Seq("VCTREF", "VCT Fund X", "2018-04-16", 23312.00, 1334.00), Seq("XTAL", "EIS Fund X", true, "2020-12-12", 23312.00, 43432.00),
                               Seq("CIREF", "CI X", "2020-12-12", 6442.00, 2344.00),
                               Seq("123412/1A", "Company Inc", "2020-12-12", 123123.22, 3432.00),
                               Seq("123412/1A", "SE Inc", "2020-12-12", 123123.22, 3432.00))
  private val requestBodyJson = Json.parse(
    """
      |{
      |  "vctSubscription":[
      |    {
      |      "uniqueInvestmentRef": "VCTREF",
      |      "name": "VCT Fund X",
      |      "dateOfInvestment": "2018-04-16",
      |      "amountInvested": 23312.00,
      |      "reliefClaimed": 1334.00
      |      }
      |  ],
      |  "eisSubscription":[
      |    {
      |      "uniqueInvestmentRef": "XTAL",
      |      "name": "EIS Fund X",
      |      "knowledgeIntensive": true,
      |      "dateOfInvestment": "2020-12-12",
      |      "amountInvested": 23312.00,
      |      "reliefClaimed": 43432.00
      |    }
      |  ],
      |  "communityInvestment": [
      |    {
      |      "uniqueInvestmentRef": "CIREF",
      |      "name": "CI X",
      |      "dateOfInvestment": "2020-12-12",
      |      "amountInvested": 6442.00,
      |      "reliefClaimed": 2344.00
      |    }
      |  ],
      |  "seedEnterpriseInvestment": [
      |    {
      |      "uniqueInvestmentRef": "123412/1A",
      |      "companyName": "Company Inc",
      |      "dateOfInvestment": "2020-12-12",
      |      "amountInvested": 123123.22,
      |      "reliefClaimed": 3432.00
      |    }
      |  ],
      |  "socialEnterpriseInvestment": [
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
    lazy val parser = new AmendReliefInvestmentDataParser(mockValidator)
  }

  "parse" should {

    "return a request object" when {
      "valid request data is supplied" in new Test {
        MockAmendReliefInvestmentValidator.validate(inputData).returns(Nil)

        parser.parseRequest(inputData) shouldBe
          Right(AmendReliefInvestmentsRequest(Nino(nino),taxYear,AmendReliefInvestmentsBody(Seq("VCTREF", "VCT Fund X", "2018-04-16", 23312.00, 1334.00), Seq("XTAL", "EIS Fund X", true, "2020-12-12", 23312.00, 43432.00),
            Seq("CIREF", "CI X", "2020-12-12", 6442.00, 2344.00),
            Seq("123412/1A", "Company Inc", "2020-12-12", 123123.22, 3432.00),
            Seq("123412/1A", "SE Inc", "2020-12-12", 123123.22, 3432.00))))
      }
    }
  }

}
