/*
 * Copyright 2024 HM Revenue & Customs
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

package routing

import play.api.routing.Router
import shared.config.SharedAppConfig
import shared.routing._

import javax.inject.{Inject, Singleton}

@Singleton case class ReliefsVersionRoutingMap @Inject()(
                                                        appConfig: SharedAppConfig,
                                                        defaultRouter: Router,
                                                        v1Router: v1.Routes,
                                                        v2Router: v2.Routes
                                                      ) extends VersionRoutingMap {

  /** Routes corresponding to available versions.
   */
  val map: Map[Version, Router] = Map(
    Version1 -> v1Router,
    Version2 -> v2Router
  )

}
