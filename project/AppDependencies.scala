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

  val playVersion             =  "-play-28"
  val bootstrapPlay           =  "7.13.0"
  val domainVersion           = s"8.1.0$playVersion"
  val scalaTestPlusVersion    =  "5.1.0"
  val flexmarkVersion         =  "0.64.0"
  val scalatestVersion        =  "3.2.15"
  val scalatestMockitoVersion =  "3.2.12.0"
  val wiremockVersion         =  "2.35.0"

  val compile = Seq(
    "uk.gov.hmrc"               %% s"bootstrap-backend$playVersion"     % bootstrapPlay,
    "uk.gov.hmrc"               %%  "domain"                            % domainVersion
  )

  val test = Seq(
    "uk.gov.hmrc"               %% s"bootstrap-test$playVersion"        % bootstrapPlay               % "test, it",
    "org.scalatest"             %%  "scalatest"                         % scalatestVersion            % "test, it",
    "org.scalatestplus.play"    %%  "scalatestplus-play"                % scalaTestPlusVersion        % "test, it",
    "org.scalatestplus"         %%  "mockito-4-5"                       % scalatestMockitoVersion     % "test, it",
    "com.vladsch.flexmark"      %   "flexmark-all"                      % flexmarkVersion             % "test, it",
    "com.typesafe.play"         %%  "play-test"                         % PlayVersion.current         % "test, it",
    "com.github.tomakehurst"    %   "wiremock-jre8-standalone"          % wiremockVersion             % "it"
  )

  def apply(): Seq[ModuleID] = compile ++ test
}

