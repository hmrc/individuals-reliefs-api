/*
 * Copyright 2026 HM Revenue & Customs
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

package api.routing

import api.utils.UnitSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.routing.Router

class VersionRoutingMapSpec extends UnitSpec with GuiceOneAppPerSuite {

  private val defaultRouter: Router = mock[Router]
  private val v2Routes: v2.Routes   = app.injector.instanceOf[v2.Routes]
  private val v3Routes: v3.Routes   = app.injector.instanceOf[v3.Routes]

  private val versionRoutingMap: VersionRoutingMapImpl = VersionRoutingMapImpl(
    defaultRouter = defaultRouter,
    v2Router = v2Routes,
    v3Router = v3Routes
  )

  "VersionRoutingMap" when {
    "routing to v2" should {
      "return the v2 router" in {
        versionRoutingMap.map(Version2) shouldBe v2Routes
      }
    }

    "routing to v3" should {
      "return the v3 router" in {
        versionRoutingMap.map(Version3) shouldBe v3Routes
      }
    }
  }

}
