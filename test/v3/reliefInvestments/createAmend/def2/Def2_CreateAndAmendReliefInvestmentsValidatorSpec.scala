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

package v3.reliefInvestments.createAmend.def2

import common.{DateOfInvestmentFormatError, NameFormatError, UniqueInvestmentRefFormatError}
import play.api.libs.json._
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import shared.models.utils.JsonErrorValidators
import shared.utils.UnitSpec
import v3.reliefInvestments.createAmend.model.request.CreateAndAmendReliefInvestmentsRequestData
import v3.reliefInvestments.createAmend.def2.Def2_CreateAndAmendReliefInvestmentsValidator
import v3.reliefInvestments.createAmend.def2.model.request._

class Def2_CreateAndAmendReliefInvestmentsValidatorSpec extends UnitSpec with JsonErrorValidators {

  implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  private val validNino = "AA123456A"
  private val taxYear   = "2025-26"

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

  private def bodyWith(vctSubscriptionItems: Seq[JsValue] = List(validVctSubscriptionsItem),
                       eisSubscriptionsItems: Seq[JsValue] = List(validEisSubscriptionsItem),
                       communityInvestmentItems: Seq[JsValue] = List(validCommunityInvestmentItem),
                       seedEnterpriseInvestmentItems: Seq[JsValue] = List(validSeedEnterpriseInvestmentItem)): JsValue =
    Json.parse(s"""
       |{
       |  "vctSubscription":${JsArray(vctSubscriptionItems)},
       |  "eisSubscription":${JsArray(eisSubscriptionsItems)},
       |  "communityInvestment": ${JsArray(communityInvestmentItems)},
       |  "seedEnterpriseInvestment": ${JsArray(seedEnterpriseInvestmentItems)}
       |}
        """.stripMargin)

  private val validBody = bodyWith()

  private val parsedNino    = Nino(validNino)
  private val parsedTaxYear = TaxYear.fromMtd(taxYear)

  private val parsedCctSubscriptionsItem =
    VctSubscriptionsItem("VCTREF", Some("VCT Fund X"), Some("2018-04-16"), Some(BigDecimal(23312.00)), BigDecimal(1334.00))

  private val parsedEisSubscriptionsItem =
    EisSubscriptionsItem("XTAL", Some("EIS Fund X"), Some("2018-04-16"), Some(BigDecimal(23312.00)), BigDecimal(43432.00), Some(true))

  private val parsedCommunityInvestmentItem =
    CommunityInvestmentItem("VCTREF", Some("VCT Fund X"), Some("2018-04-16"), Some(BigDecimal(23312.00)), BigDecimal(1334.00))

  private val parsedSeedEnterpriseInvestmentItem =
    SeedEnterpriseInvestmentItem("1234121A", Some("Company Inc"), Some("2020-12-12"), Some(BigDecimal(123123.22)), BigDecimal(3432.00))

  private val parsedBody = Def2_CreateAndAmendReliefInvestmentsRequestBody(
    Some(List(parsedCctSubscriptionsItem)),
    Some(List(parsedEisSubscriptionsItem)),
    Some(List(parsedCommunityInvestmentItem)),
    Some(List(parsedSeedEnterpriseInvestmentItem))
  )

  private def validator(nino: String, taxYear: String, body: JsValue) = new Def2_CreateAndAmendReliefInvestmentsValidator(nino, taxYear, body)

  "validator" should {
    "return the parsed domain object" when {
      "passed a valid request" in {
        val result: Either[ErrorWrapper, CreateAndAmendReliefInvestmentsRequestData] =
          validator(validNino, taxYear, validBody).validateAndWrapResult()

        result shouldBe Right(Def2_CreateAndAmendReliefInvestmentsRequestData(parsedNino, parsedTaxYear, parsedBody))
      }
    }

    "return a single error" when {
      "passed an invalid nino" in {
        val result: Either[ErrorWrapper, CreateAndAmendReliefInvestmentsRequestData] =
          validator("invalid", taxYear, validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }

      "passed an empty body" in {
        val invalidBody = JsObject.empty
        val result: Either[ErrorWrapper, CreateAndAmendReliefInvestmentsRequestData] =
          validator(validNino, taxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError))
      }

      "passed a body with at least one empty array" in {
        val invalidBody = validBody.update("/vctSubscription", JsArray(List()))
        val result: Either[ErrorWrapper, CreateAndAmendReliefInvestmentsRequestData] =
          validator(validNino, taxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/vctSubscription")))
      }

      "passed a body with at least array containing an empty object" in {
        val invalidBody = validBody.update("/vctSubscription", JsArray(List(JsObject.empty)))
        val result: Either[ErrorWrapper, CreateAndAmendReliefInvestmentsRequestData] =
          validator(validNino, taxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            RuleIncorrectOrEmptyBodyError
              .withPaths(List("/vctSubscription/0/reliefClaimed", "/vctSubscription/0/uniqueInvestmentRef"))))
      }

      "passed a body with a negative numeric field" when {
        def testValueFormatError(path: String, body: JsValue): Unit = s"for $path" in {
          val result: Either[ErrorWrapper, CreateAndAmendReliefInvestmentsRequestData] =
            validator(validNino, taxYear, body).validateAndWrapResult()

          result shouldBe Left(ErrorWrapper(correlationId, ValueFormatError.withPath(path)))
        }

        val numericFields = List("/amountInvested", "/reliefClaimed")

        numericFields.foreach(path =>
          testValueFormatError(
            s"/vctSubscription/0$path",
            bodyWith(vctSubscriptionItems = List(validVctSubscriptionsItem.update(path, JsNumber(-1.00))))))

        numericFields.foreach(path =>
          testValueFormatError(
            s"/eisSubscription/0$path",
            bodyWith(eisSubscriptionsItems = List(validEisSubscriptionsItem.update(path, JsNumber(-1.00))))))

        numericFields.foreach(path =>
          testValueFormatError(
            s"/communityInvestment/0$path",
            bodyWith(communityInvestmentItems = List(validCommunityInvestmentItem.update(path, JsNumber(-1.00))))))

        numericFields.foreach(path =>
          testValueFormatError(
            s"/seedEnterpriseInvestment/0$path",
            bodyWith(seedEnterpriseInvestmentItems = List(validSeedEnterpriseInvestmentItem.update(path, JsNumber(-1.00))))))
      }

      "passed a body with invalidly formatted date of investments" in {
        val invalidVctSubscriptionsItem         = validVctSubscriptionsItem.update("/dateOfInvestment", JsString(""))
        val invalidEisSubscriptionsItem         = validEisSubscriptionsItem.update("/dateOfInvestment", JsString(""))
        val invalidCommunityInvestmentItem      = validCommunityInvestmentItem.update("/dateOfInvestment", JsString(""))
        val invalidSeedEnterpriseInvestmentItem = validSeedEnterpriseInvestmentItem.update("/dateOfInvestment", JsString(""))

        val invalidBody = bodyWith(
          List(invalidVctSubscriptionsItem),
          List(invalidEisSubscriptionsItem),
          List(invalidCommunityInvestmentItem),
          List(invalidSeedEnterpriseInvestmentItem)
        )
        val result: Either[ErrorWrapper, CreateAndAmendReliefInvestmentsRequestData] =
          validator(validNino, taxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            DateOfInvestmentFormatError.withPaths(List(
              "/vctSubscription/0/dateOfInvestment",
              "/eisSubscription/0/dateOfInvestment",
              "/communityInvestment/0/dateOfInvestment",
              "/seedEnterpriseInvestment/0/dateOfInvestment"
            ))
          ))
      }

      "passed a body with out of range formatted date of investments" in {
        val invalidVctSubscriptionsItem         = validVctSubscriptionsItem.update("/dateOfInvestment", JsString("1879-09-23"))
        val invalidEisSubscriptionsItem         = validEisSubscriptionsItem.update("/dateOfInvestment", JsString("2109-01-30"))
        val invalidCommunityInvestmentItem      = validCommunityInvestmentItem.update("/dateOfInvestment", JsString("1150-09-23"))
        val invalidSeedEnterpriseInvestmentItem = validSeedEnterpriseInvestmentItem.update("/dateOfInvestment", JsString("2100-01-01"))

        val invalidBody = bodyWith(
          List(invalidVctSubscriptionsItem),
          List(invalidEisSubscriptionsItem),
          List(invalidCommunityInvestmentItem),
          List(invalidSeedEnterpriseInvestmentItem)
        )
        val result: Either[ErrorWrapper, CreateAndAmendReliefInvestmentsRequestData] =
          validator(validNino, taxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            DateOfInvestmentFormatError.withPaths(List(
              "/vctSubscription/0/dateOfInvestment",
              "/eisSubscription/0/dateOfInvestment",
              "/communityInvestment/0/dateOfInvestment",
              "/seedEnterpriseInvestment/0/dateOfInvestment"
            ))
          ))
      }

      "passed a body with invalidly formatted unique investment references" in {
        val invalidVctSubscriptionsItem         = validVctSubscriptionsItem.update("/uniqueInvestmentRef", JsString("ABC/123"))
        val invalidEisSubscriptionsItem         = validEisSubscriptionsItem.update("/uniqueInvestmentRef", JsString("ABC/123"))
        val invalidCommunityInvestmentItem      = validCommunityInvestmentItem.update("/uniqueInvestmentRef", JsString("ABC/123"))
        val invalidSeedEnterpriseInvestmentItem = validSeedEnterpriseInvestmentItem.update("/uniqueInvestmentRef", JsString("ABC/123"))

        val invalidBody = bodyWith(
          List(invalidVctSubscriptionsItem),
          List(invalidEisSubscriptionsItem),
          List(invalidCommunityInvestmentItem),
          List(invalidSeedEnterpriseInvestmentItem)
        )
        val result: Either[ErrorWrapper, CreateAndAmendReliefInvestmentsRequestData] =
          validator(validNino, taxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            UniqueInvestmentRefFormatError.withPaths(List(
              "/vctSubscription/0/uniqueInvestmentRef",
              "/eisSubscription/0/uniqueInvestmentRef",
              "/communityInvestment/0/uniqueInvestmentRef",
              "/seedEnterpriseInvestment/0/uniqueInvestmentRef"
            ))
          ))
      }

      "passed a body with invalidly formatted names" in {
        val invalidVctSubscriptionsItem         = validVctSubscriptionsItem.update("/name", JsString(""))
        val invalidEisSubscriptionsItem         = validEisSubscriptionsItem.update("/name", JsString(""))
        val invalidCommunityInvestmentItem      = validCommunityInvestmentItem.update("/name", JsString(""))
        val invalidSeedEnterpriseInvestmentItem = validSeedEnterpriseInvestmentItem.update("/companyName", JsString(""))

        val invalidBody = bodyWith(
          List(invalidVctSubscriptionsItem),
          List(invalidEisSubscriptionsItem),
          List(invalidCommunityInvestmentItem),
          List(invalidSeedEnterpriseInvestmentItem)
        )
        val result: Either[ErrorWrapper, CreateAndAmendReliefInvestmentsRequestData] =
          validator(validNino, taxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            NameFormatError.withPaths(
              List(
                "/vctSubscription/0/name",
                "/eisSubscription/0/name",
                "/communityInvestment/0/name",
                "/seedEnterpriseInvestment/0/companyName"
              ))
          ))
      }
    }

    "return multiple errors" when {
      "the request has multiple issues (path parameters)" in {
        val invalidBody = JsObject.empty
        val result: Either[ErrorWrapper, CreateAndAmendReliefInvestmentsRequestData] =
          validator("invalid", taxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            BadRequestError,
            Some(List(NinoFormatError, RuleIncorrectOrEmptyBodyError))
          )
        )
      }
    }
  }

}
