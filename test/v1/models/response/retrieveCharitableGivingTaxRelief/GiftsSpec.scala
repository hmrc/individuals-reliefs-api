/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package v1.models.response.retrieveCharitableGivingTaxRelief

import play.api.libs.json.{JsObject, Json}
import support.UnitSpec

class GiftsSpec extends UnitSpec {

  "Gifts reads" must {
    "include the nonUkCharities object in the model" when {
      "a nonUkCharities value is present in the downstream JSON" in {
        Json
          .parse("""{
                   |   "investmentsNonUkCharitiesCharityNames": ["charity 1", "charity 2"],
                   |   "investmentsNonUkCharities": 1.12,
                   |   "landAndBuildings": 2.12,
                   |   "sharesOrSecurities": 3.12
                   |}""".stripMargin)
          .as[Gifts] shouldBe
          Gifts(
            nonUkCharities = Some(NonUkCharities(charityNames = Some(Seq("charity 1", "charity 2")), totalAmount = 1.12)),
            landAndBuildings = Some(2.12),
            sharesOrSecurities = Some(3.12)
          )
      }
    }

    "omit the nonUkCharities object from the model" when {
      "no nonUkCharities value is present in the downstream JSON" in {
        Json
          .parse("""{
                   |   "landAndBuildings": 2.12,
                   |   "sharesOrSecurities": 3.12
                   |}""".stripMargin)
          .as[Gifts] shouldBe
          Gifts(
            nonUkCharities = None,
            landAndBuildings = Some(2.12),
            sharesOrSecurities = Some(3.12)
          )
      }
    }

    "treat all fields as optional" in {
      JsObject.empty.as[Gifts] shouldBe
        Gifts(
          nonUkCharities = None,
          landAndBuildings = None,
          sharesOrSecurities = None
        )
    }
  }

  "Gifts writes" must {
    "write to MTD JSON" in {
      Json.toJson(
        Gifts(
          nonUkCharities = Some(NonUkCharities(charityNames = Some(Seq("charity 1", "charity 2")), totalAmount = 1.12)),
          landAndBuildings = Some(2.12),
          sharesOrSecurities = Some(3.12)
        )) shouldBe Json.parse("""{
                                       |      "nonUkCharities": {
                                       |         "charityNames": ["charity 1", "charity 2"],
                                       |         "totalAmount": 1.12
                                       |      },
                                       |      "landAndBuildings": 2.12,
                                       |      "sharesOrSecurities": 3.12
                                       |   }""".stripMargin)
    }
  }

}
