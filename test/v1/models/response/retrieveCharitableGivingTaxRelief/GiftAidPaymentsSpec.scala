/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package v1.models.response.retrieveCharitableGivingTaxRelief

import play.api.libs.json.{JsObject, Json}
import support.UnitSpec

class GiftAidPaymentsSpec extends UnitSpec {

  "GiftAidPayments reads" must {
    "include the nonUkCharities object in the model" when {
      "a nonUkCharities value is present in the downstream JSON" in {
        Json
          .parse("""{
                    |   "nonUkCharitiesCharityNames": ["charity 1", "charity 2"],
                    |   "nonUkCharities": 1.12,
                    |   "currentYear": 2.12,
                    |   "oneOffCurrentYear": 3.12,
                    |   "currentYearTreatedAsPreviousYear": 4.12,
                    |   "nextYearTreatedAsCurrentYear": 5.12
                    |}""".stripMargin)
          .as[GiftAidPayments] shouldBe
          GiftAidPayments(
            nonUkCharities = Some(NonUkCharities(charityNames = Some(Seq("charity 1", "charity 2")), totalAmount = 1.12)),
            totalAmount = Some(2.12),
            oneOffAmount = Some(3.12),
            amountTreatedAsPreviousTaxYear = Some(4.12),
            amountTreatedAsSpecifiedTaxYear = Some(5.12)
          )
      }
    }

    "omit the nonUkCharities object from the model" when {
      "no nonUkCharities value is present in the downstream JSON" in {
        Json
          .parse("""{
                   |   "currentYear": 2.12,
                   |   "oneOffCurrentYear": 3.12,
                   |   "currentYearTreatedAsPreviousYear": 4.12,
                   |   "nextYearTreatedAsCurrentYear": 5.12
                   |}""".stripMargin)
          .as[GiftAidPayments] shouldBe
          GiftAidPayments(
            nonUkCharities = None,
            totalAmount = Some(2.12),
            oneOffAmount = Some(3.12),
            amountTreatedAsPreviousTaxYear = Some(4.12),
            amountTreatedAsSpecifiedTaxYear = Some(5.12)
          )
      }
    }

    "treat all fields as optional" in {
      JsObject.empty.as[GiftAidPayments] shouldBe
        GiftAidPayments(
          nonUkCharities = None,
          totalAmount = None,
          oneOffAmount = None,
          amountTreatedAsPreviousTaxYear = None,
          amountTreatedAsSpecifiedTaxYear = None
        )
    }
  }

  "GiftAidPayments writes" must {
    "write to MTD JSON" in {
      Json.toJson(
        GiftAidPayments(
          nonUkCharities = Some(NonUkCharities(charityNames = Some(Seq("charity 1", "charity 2")), totalAmount = 1.12)),
          totalAmount = Some(2.12),
          oneOffAmount = Some(3.12),
          amountTreatedAsPreviousTaxYear = Some(4.12),
          amountTreatedAsSpecifiedTaxYear = Some(5.12)
        )) shouldBe Json.parse("""{
                                 |   "nonUkCharities": {
                                 |      "charityNames": ["charity 1", "charity 2"],
                                 |      "totalAmount": 1.12
                                 |   },
                                 |   "totalAmount": 2.12,
                                 |   "oneOffAmount": 3.12,
                                 |   "amountTreatedAsPreviousTaxYear": 4.12,
                                 |   "amountTreatedAsSpecifiedTaxYear": 5.12
                                 |}""".stripMargin)
    }
  }

}
