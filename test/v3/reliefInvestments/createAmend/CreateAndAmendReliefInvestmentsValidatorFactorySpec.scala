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

import play.api.libs.json.{JsArray, JsValue, Json}
import shared.controllers.validators.{AlwaysErrorsValidator, Validator}
import shared.utils.UnitSpec
import v3.reliefInvestments.createAmend.def1.Def1_CreateAndAmendReliefInvestmentsValidator
import v3.reliefInvestments.createAmend.def2.Def2_CreateAndAmendReliefInvestmentsValidator
import v3.reliefInvestments.createAmend.model.request.CreateAndAmendReliefInvestmentsRequestData

class CreateAndAmendReliefInvestmentsValidatorFactorySpec extends UnitSpec {

  private val validVctSubscriptionsItem = Json.parse("""
                                                       |{
                                                       |  "uniqueInvestmentRef": "VCTREF",
                                                       |  "name": "VCT Fund X",
                                                       |  "dateOfInvestment": "2018-04-16",
                                                       |  "amountInvested": 23312.00,
                                                       |  "reliefClaimed": 1334.00
                                                       |}
        """.stripMargin)

  private val validEisSubscriptionsItem = Json.parse("""
                                                       |{
                                                       |  "uniqueInvestmentRef": "XTAL",
                                                       |  "name": "EIS Fund X",
                                                       |  "knowledgeIntensive": true,
                                                       |  "dateOfInvestment": "2018-04-16",
                                                       |  "amountInvested": 23312.00,
                                                       |  "reliefClaimed": 43432.00
                                                       |}
        """.stripMargin)

  private val validCommunityInvestmentItem = Json.parse("""
                                                          |{
                                                          |  "uniqueInvestmentRef": "VCTREF",
                                                          |  "name": "VCT Fund X",
                                                          |  "dateOfInvestment": "2018-04-16",
                                                          |  "amountInvested": 23312.00,
                                                          |  "reliefClaimed": 1334.00
                                                          |}
        """.stripMargin)

  private val validSeedEnterpriseInvestmentItem = Json.parse("""
                                                               |{
                                                               |  "uniqueInvestmentRef": "1234121A",
                                                               |  "companyName": "Company Inc",
                                                               |  "dateOfInvestment": "2020-12-12",
                                                               |  "amountInvested": 123123.22,
                                                               |  "reliefClaimed": 3432.00
                                                               |}
        """.stripMargin)

  private val validSocialEnterpriseInvestmentItem = Json.parse(
    """
      |{
      |  "uniqueInvestmentRef": "VCTREF",
      |  "socialEnterpriseName": "VCT Fund X",
      |  "dateOfInvestment": "2018-04-16",
      |  "amountInvested": 23312.00,
      |  "reliefClaimed": 1334.00
      |}
        """.stripMargin
  )

  private def bodyWith(vctSubscriptionItems: Seq[JsValue] = List(validVctSubscriptionsItem),
                       eisSubscriptionsItems: Seq[JsValue] = List(validEisSubscriptionsItem),
                       communityInvestmentItems: Seq[JsValue] = List(validCommunityInvestmentItem),
                       seedEnterpriseInvestmentItems: Seq[JsValue] = List(validSeedEnterpriseInvestmentItem),
                       socialEnterpriseInvestmentItems: Seq[JsValue] = List(validSocialEnterpriseInvestmentItem)): JsValue =
    Json.parse(s"""
                  |{
                  |  "vctSubscription":${JsArray(vctSubscriptionItems)},
                  |  "eisSubscription":${JsArray(eisSubscriptionsItems)},
                  |  "communityInvestment": ${JsArray(communityInvestmentItems)},
                  |  "seedEnterpriseInvestment": ${JsArray(seedEnterpriseInvestmentItems)},
                  |  "socialEnterpriseInvestment": ${JsArray(socialEnterpriseInvestmentItems)}
                  |}
        """.stripMargin)

  private val validBody = bodyWith()

  private def validatorFor(taxYear: String): Validator[CreateAndAmendReliefInvestmentsRequestData] =
    new CreateAndAmendReliefInvestmentsValidatorFactory().validator(nino = "ignoredNino", taxYear = taxYear, validBody)

  "CreateAndAmendReliefInvestmentsValidatorFactory" when {
    "given a request corresponding to a Def1 schema" should {
      "return a Def1 validator" in {
        validatorFor("2024-25") shouldBe a[Def1_CreateAndAmendReliefInvestmentsValidator]
      }
    }
    "given a request corresponding to a Def2 schema" should {
      "return a Def2 validator" in {
        validatorFor("2025-26") shouldBe a[Def2_CreateAndAmendReliefInvestmentsValidator]
      }
    }

    "given a request where no valid schema could be determined" should {
      "return a validator returning the errors" in {
        validatorFor("BAD_TAX_YEAR") shouldBe an[AlwaysErrorsValidator]
      }
    }
  }

}
