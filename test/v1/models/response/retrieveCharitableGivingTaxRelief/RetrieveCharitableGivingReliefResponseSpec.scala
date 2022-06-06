/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package v1.models.response.retrieveCharitableGivingTaxRelief

import play.api.libs.json.Json
import support.UnitSpec

class RetrieveCharitableGivingReliefResponseSpec extends UnitSpec with RetrieveCharitableGivingReliefFixture {

  "RetrieveCharitableGivingReliefResponse reads" must {
    "read from downstream JSON" in {
      charitableGivingReliefResponseDownstreamJson.as[RetrieveCharitableGivingReliefResponse] shouldBe charitableGivingReliefResponse
    }
  }

  "RetrieveCharitableGivingReliefResponse writes" must {
    "write to MTD JSON" in {
      Json.toJson(charitableGivingReliefResponse) shouldBe charitableGivingReliefResponseMtdJson
    }
  }

}
