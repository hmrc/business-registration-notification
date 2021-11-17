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

import play.core.PlayVersion
import sbt._

private object AppDependencies {
  def apply(): Seq[ModuleID] = MainDependencies() ++ UnitTestDependencies() ++ IntegrationTestDependencies()
}

object MainDependencies {
  private val bootstrapPlay = "5.16.0"
  private val domainVersion = "6.2.0-play-28"
  private val jodaTimeVersion = "2.9.2"

  def apply() = Seq(
    "uk.gov.hmrc" %% "bootstrap-backend-play-28" % bootstrapPlay,
    "uk.gov.hmrc" %% "domain" % domainVersion,
    "com.typesafe.play" %% "play-json-joda" % jodaTimeVersion
  )
}

trait TestDependencies {
  val scalaTestPlusVersion = "5.1.0"
  val flexmarkVersion = "0.36.8"
  val mockitoCoreVersion = "4.0.0"
  val scalatestMockitoVersion = "3.2.10.0"
  val wiremockVersion = "2.31.0"

  val scope: Configuration
  val test: Seq[ModuleID]

  lazy val coreTestDependencies = Seq(
    "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusVersion % scope,
    "com.vladsch.flexmark" % "flexmark-all" % flexmarkVersion % scope,
    "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
    "org.mockito" % "mockito-core" % mockitoCoreVersion % scope,
    "org.scalatestplus" %% "mockito-3-4" % scalatestMockitoVersion % scope
  )
}

object UnitTestDependencies extends TestDependencies {
  override val scope = Test
  override val test: Seq[ModuleID] = coreTestDependencies

  def apply(): Seq[ModuleID] = test
}

object IntegrationTestDependencies extends TestDependencies {
  override val scope = IntegrationTest
  override val test: Seq[ModuleID] = coreTestDependencies ++ Seq(
    "com.github.tomakehurst" % "wiremock-jre8-standalone" % wiremockVersion % scope
  )

  def apply(): Seq[ModuleID] = test
}

