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
import sbt.*

private object AppDependencies {

  val playVersion             =  "-play-30"
  val bootstrapPlay           =  "8.5.0"
  val domainVersion           =  "9.0.0"
  val scalaTestPlusVersion    =  "7.0.1"
  val flexmarkVersion         =  "0.64.8"
  val scalatestVersion        =  "3.2.18"
  val scalatestMockitoVersion =  "3.2.12.0"
  val wiremockVersion         =  "2.35.1"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"               %% s"bootstrap-backend$playVersion"     % bootstrapPlay,
    "uk.gov.hmrc"               %% s"domain$playVersion"                % domainVersion
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"               %% s"bootstrap-test$playVersion"        % bootstrapPlay               % "test",
    "org.scalatest"             %%  "scalatest"                         % scalatestVersion            % "test",
    "org.scalatestplus.play"    %%  "scalatestplus-play"                % scalaTestPlusVersion        % "test",
    "org.scalatestplus"         %%  "mockito-4-5"                       % scalatestMockitoVersion     % "test",
    "com.vladsch.flexmark"      %   "flexmark-all"                      % flexmarkVersion             % "test",
    "org.playframework"         %%  "play-test"                         % PlayVersion.current         % "test",
    "com.github.tomakehurst"    %   "wiremock-jre8-standalone"          % wiremockVersion             % "test"
  )

  def apply(): Seq[ModuleID] = compile ++ test
}

