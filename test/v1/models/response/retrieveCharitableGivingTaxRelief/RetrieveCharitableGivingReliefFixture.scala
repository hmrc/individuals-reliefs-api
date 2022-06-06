/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package v1.models.response.retrieveCharitableGivingTaxRelief

import play.api.libs.json.{JsValue, Json}

trait RetrieveCharitableGivingReliefFixture {

  val charitableGivingReliefResponseMtdJson: JsValue = Json.parse("""
      |{
      |   "giftAidPayments": {
      |      "nonUkCharities": {
      |         "charityNames": ["charity 1", "charity 2"],
      |         "totalAmount": 1.12
      |      },
      |      "totalAmount": 2.12,
      |      "oneOffAmount": 3.12,
      |      "amountTreatedAsPreviousTaxYear": 4.12,
      |      "amountTreatedAsSpecifiedTaxYear": 5.12
      |   },
      |   "gifts": {
      |      "nonUkCharities": {
      |         "charityNames": ["charity 3", "charity 4"],
      |         "totalAmount": 6.12
      |      },
      |      "landAndBuildings": 7.12,
      |      "sharesOrSecurities": 8.12
      |   }
      |}
      |""".stripMargin)

  val charitableGivingReliefResponse: RetrieveCharitableGivingReliefResponse =
    RetrieveCharitableGivingReliefResponse(
      giftAidPayments = Some(
        GiftAidPayments(
          nonUkCharities = Some(NonUkCharities(charityNames = Some(Seq("charity 1", "charity 2")), totalAmount = 1.12)),
          totalAmount = Some(2.12),
          oneOffAmount = Some(3.12),
          amountTreatedAsPreviousTaxYear = Some(4.12),
          amountTreatedAsSpecifiedTaxYear = Some(5.12)
        )),
      gifts = Some(
        Gifts(
          nonUkCharities = Some(NonUkCharities(charityNames = Some(Seq("charity 3", "charity 4")), totalAmount = 6.12)),
          landAndBuildings = Some(7.12),
          sharesOrSecurities = Some(8.12)
        ))
    )

  val charitableGivingReliefResponseDownstreamJson: JsValue = Json.parse("""
       |{
       |   "giftAidPayments": {
       |      "nonUkCharitiesCharityNames":["charity 1", "charity 2"],
       |      "nonUkCharities": 1.12,
       |      "currentYear": 2.12,
       |      "oneOffCurrentYear": 3.12,
       |      "currentYearTreatedAsPreviousYear": 4.12,
       |      "nextYearTreatedAsCurrentYear": 5.12
       |   },
       |   "gifts": {
       |      "investmentsNonUkCharitiesCharityNames": ["charity 3", "charity 4"],
       |      "investmentsNonUkCharities": 6.12,
       |      "landAndBuildings": 7.12,
       |      "sharesOrSecurities": 8.12
       |   }
       |}
       |""".stripMargin)

}
