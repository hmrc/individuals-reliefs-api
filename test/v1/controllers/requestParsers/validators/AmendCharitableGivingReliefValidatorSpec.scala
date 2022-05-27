/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package v1.controllers.requestParsers.validators

import play.api.libs.json._
import support.UnitSpec
import v1.models.errors._
import v1.models.request.createAndAmendCharitableGivingTaxRelief.CreateAndAmendCharitableGivingTaxReliefRawData
import v1.models.utils.JsonErrorValidators

class AmendCharitableGivingReliefValidatorSpec extends UnitSpec with JsonErrorValidators {

  private val validNino    = "AA123456A"
  private val validTaxYear = "2021-22"

  private def nonUkCharitiesJson(charityNames: Seq[String] = Seq("MSF")) =
    Json.parse(s"""
      |{
      |  "charityNames": ${JsArray(charityNames.map(JsString))},
      |  "totalAmount": 10.99
      |}
      |""".stripMargin)

  private def giftAidPaymentsJson(nonUkCharities: JsValue = nonUkCharitiesJson()) =
    Json.parse(s"""
      |{
      |   "nonUkCharities": $nonUkCharities,
      |   "totalAmount": 10.99,
      |   "oneOffAmount": 10.99,
      |   "amountTreatedAsPreviousTaxYear": 10.99,
      |   "amountTreatedAsSpecifiedTaxYear": 10.99
      |}
      |""".stripMargin)

  private def giftsJson(nonUkCharities: JsValue = nonUkCharitiesJson()) =
    Json.parse(s"""
      |{
      |   "nonUkCharities": $nonUkCharities,
      |   "benefitFromLandAndBuildings": 10.99,
      |   "benefitFromSharesOrSecurities": 10.99
      |}""".stripMargin)

  private def requestBodyJson(giftAidPayments: JsValue = giftAidPaymentsJson(), gifts: JsValue = giftsJson()): JsValue =
    Json.parse(s"""
      |{
      |   "giftAidPayments": $giftAidPayments,
      |   "gifts": $gifts
      |}""".stripMargin)

  private val body = requestBodyJson()

  val validator = new AmendCharitableGivingReliefValidator

  "running a validation" should {
    "return no errors" when {
      "only gift payments included" in {
        validator.validate(
          CreateAndAmendCharitableGivingTaxReliefRawData(
            validNino,
            validTaxYear,
            Json.parse(s"""
            |{
            |   "giftAidPayments": ${giftAidPaymentsJson()}
            |}""".stripMargin)
          )) shouldBe Nil
      }

      "only gifts included" in {
        validator.validate(
          CreateAndAmendCharitableGivingTaxReliefRawData(
            validNino,
            validTaxYear,
            Json.parse(s"""
            |{
            |   "gifts": ${giftsJson()}
            |}""".stripMargin)
          )) shouldBe Nil
      }

      "all sections included" in {
        validator.validate(CreateAndAmendCharitableGivingTaxReliefRawData(validNino, validTaxYear, body)) shouldBe Nil
      }
    }
  }

  "return NinoFormatError error" when {
    "an invalid nino is supplied" in {
      validator.validate(CreateAndAmendCharitableGivingTaxReliefRawData("BADNINO", validTaxYear, body)) shouldBe List(NinoFormatError)
    }
  }

  "return TaxYearFormatError" when {
    "an invalid tax year is supplied" in {
      validator.validate(CreateAndAmendCharitableGivingTaxReliefRawData(validNino, "BADTAXYEAR", body)) shouldBe List(TaxYearFormatError)
    }
  }

  "return RuleTaxYearNotSupportedError" when {
    "a tax year that is too early is supplied" in {
      validator.validate(CreateAndAmendCharitableGivingTaxReliefRawData(validNino, "2016-17", body)) shouldBe List(RuleTaxYearNotSupportedError)
    }
  }

  "return RuleTaxYearRangeInvalidError" when {
    "a tax year range is more than 1 year" in {
      validator.validate(CreateAndAmendCharitableGivingTaxReliefRawData(validNino, validTaxYear, body)) shouldBe List(RuleTaxYearRangeInvalidError)
    }
  }

  "return a FormatValue error" when {
    val badValue = JsNumber(123.456)

    Seq(
      "/giftAidPayments/nonUkCharities/amount",
      "/giftAidPayments/totalAmount",
      "/giftAidPayments/oneOffAmount",
      "/giftAidPayments/amountTreatedAsPreviousTaxYear",
      "/giftAidPayments/amountTreatedAsSpecifiedTaxYear",
      "/gifts/nonUkCharities/amount",
      "/gifts/benefitFromLandAndBuildings",
      "/gifts/benefitFromSharesOrSecurities"
    ).foreach(path => testValueFormatError(body.update(path, badValue), path))

    def testValueFormatError(body: JsValue, expectedPath: String): Unit = s"for $expectedPath" in {
      validator.validate(CreateAndAmendCharitableGivingTaxReliefRawData(validNino, validTaxYear, body)) shouldBe
        List(ValueFormatError.copy(paths = Some(Seq(expectedPath))))
    }
  }

  "return a RULE_INCORRECT_OR_EMPTY_BODY_SUBMITTED error" when {
    "body is an empty object" in {
      validator.validate(CreateAndAmendCharitableGivingTaxReliefRawData(validNino, validTaxYear, JsObject.empty)) shouldBe List(
        RuleIncorrectOrEmptyBodyError)
    }

    "giftAidPayments is empty" in {
      validator.validate(
        CreateAndAmendCharitableGivingTaxReliefRawData(validNino, validTaxYear, body.replaceWithEmptyObject("/giftAidPayments"))) shouldBe List(
        RuleIncorrectOrEmptyBodyError.copy(paths = Some(Seq("/giftAidPayments"))))
    }

    "giftAidPayments nonUkCharities is empty" in {
      validator.validate(
        CreateAndAmendCharitableGivingTaxReliefRawData(
          validNino,
          validTaxYear,
          body.replaceWithEmptyObject("/giftAidPayments/nonUkCharities"))) shouldBe List(
        RuleIncorrectOrEmptyBodyError.copy(paths = Some(Seq("/giftAidPayments/nonUkCharities"))))
    }

    "giftAidPayments charity names array is empty" in {
      validator.validate(
        CreateAndAmendCharitableGivingTaxReliefRawData(
          validNino,
          validTaxYear,
          requestBodyJson(giftAidPayments = giftAidPaymentsJson(nonUkCharitiesJson(Nil))))) shouldBe List(
        RuleIncorrectOrEmptyBodyError.copy(paths = Some(Seq("/giftAidPayments/nonUkCharityNames"))))
    }

    "gifts is empty" in {
      validator.validate(
        CreateAndAmendCharitableGivingTaxReliefRawData(validNino, validTaxYear, body.replaceWithEmptyObject("/gifts"))) shouldBe List(
        RuleIncorrectOrEmptyBodyError.copy(paths = Some(Seq("/gifts"))))
    }

    "gifts nonUkCharities is empty" in {
      validator.validate(
        CreateAndAmendCharitableGivingTaxReliefRawData(validNino, validTaxYear, body.replaceWithEmptyObject("/gifts/nonUkCharities"))) shouldBe List(
        RuleIncorrectOrEmptyBodyError.copy(paths = Some(Seq("/gifts/nonUkCharities"))))
    }

    "gifts charity names array is empty" in {
      validator.validate(
        CreateAndAmendCharitableGivingTaxReliefRawData(validNino, validTaxYear, requestBodyJson(gifts = nonUkCharitiesJson(Nil)))) shouldBe List(
        RuleIncorrectOrEmptyBodyError.copy(paths = Some(Seq("/gifts/nonUkCharities/charityNames"))))
    }
  }

  "validating non-uk gift aid amount and charity names" when {
    "return a RuleGiftAidNonUkAmountWithoutNamesError" when {
      "names are required" in {
        validator.validate(
          CreateAndAmendCharitableGivingTaxReliefRawData(
            validNino,
            validTaxYear,
            body.removeProperty("/giftAidPayments/nonUkCharities/charityNames"))) shouldBe List(RuleGiftAidNonUkAmountWithoutNamesError)
      }
    }

    "return a FormatStringError" when {
      "charity names are not valid according to the regex" in {
        validator.validate(
          CreateAndAmendCharitableGivingTaxReliefRawData(
            validNino,
            validTaxYear,
            requestBodyJson(giftAidPayments = giftAidPaymentsJson(nonUkCharitiesJson(Seq("Good", "X" * 76, "Y" * 76)))))
        ) shouldBe List(
          StringFormatError.copy(paths =
            Some(Seq("/giftAidPayments/nonUkCharities/charityNames/1", "/giftAidPayments/nonUkCharities/charityNames/2"))))
      }
    }
  }

  "validating non-uk investment amount and charity names" when {
    "return a RuleGiftsNonUkInvestmentsAmountWithoutNamesError" when {
      "names are required" in {
        validator.validate(
          CreateAndAmendCharitableGivingTaxReliefRawData(
            validNino,
            validTaxYear,
            body.removeProperty("/gifts/nonUkCharities/charityNames"))) shouldBe List(RuleGiftsNonUkInvestmentsAmountWithoutNamesError)
      }
    }

    "return a FormatStringError" when {
      "charity names are not valid according to the regex" in {
        validator.validate(
          CreateAndAmendCharitableGivingTaxReliefRawData(
            validNino,
            validTaxYear,
            requestBodyJson(gifts = giftsJson(nonUkCharitiesJson(Seq("Good", "X" * 76, "Y" * 76)))))
        ) shouldBe List(StringFormatError.copy(paths = Some(Seq("/gifts/nonUkCharities/charityNames/1", "/gifts/nonUkCharities/charityNames/2"))))
      }
    }
  }

}
