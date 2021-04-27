/*
 * Copyright 2021 HM Revenue & Customs
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

package basicauth

import play.api.Configuration
import play.api.http.HeaderNames.{AUTHORIZATION, WWW_AUTHENTICATE}
import play.api.mvc._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PassThroughAction @Inject()(cc: ControllerComponents) extends ActionBuilder[Request, AnyContent] with ActionFilter[Request] {

  override val parser: BodyParser[AnyContent] = cc.parsers.defaultBodyParser
  override protected val executionContext: ExecutionContext = cc.executionContext

  def filter[A](request: Request[A]): Future[Option[Result]] = Future.successful(None)
}

class BasicAuthenticatedAction(authConfig: BasicAuthenticationFilterConfiguration, cc: ControllerComponents)
  extends ActionBuilder[Request, AnyContent]
    with ActionFilter[Request] {

  override val parser: BodyParser[AnyContent] = cc.parsers.defaultBodyParser
  override protected val executionContext: ExecutionContext = cc.executionContext

  def filter[A](request: Request[A]): Future[Option[Result]] = Future.successful(doFilter(request))

  private def doFilter[A](request: Request[A]): Option[Result] = {
    if (authConfig.enabled) {
      val creds = BasicAuthCredentials.fromAuthorizationHeader(request.headers.get(AUTHORIZATION))
      if (authConfig.matches(creds)) {
        None // valid credentials: allow subsequent operations to proceed
      } else {
        Some(Results.Unauthorized.withHeaders(WWW_AUTHENTICATE -> authConfig.basicRealm))
      }
    } else {
      None
    }
  }
}

class BasicAuthenticationFilterConfiguration(realm: String, val enabled: Boolean,
                                             username: String, password: String) {
  val basicRealm: String = "Basic realm=\"" + realm + "\""

  def matches(other: Option[BasicAuthCredentials]): Boolean = {
    other match {
      case Some(creds) => username == creds.username && password == creds.password
      case None => false
    }
  }
}

trait BasicAuthentication {
  val config: Configuration

  def getBasicAuthConfig: BasicAuthenticationFilterConfiguration = {
    def key(k: String) = s"basicAuthentication.$k"

    val enabled = config.get[Boolean](key("enabled"))
    val realm = config.get[String](key("realm"))
    val username = config.get[String](key("username"))
    val password = config.get[String](key("password"))

    new BasicAuthenticationFilterConfiguration(realm, enabled, username, password)
  }
}

//object BasicAuthenticationFilterConfiguration extends RunMode {
//  /* Required in app-config - the password MUST be encrypted by WebOps
//    basicAuthentication.enabled: true
//    basicAuthentication.realm: 'Production'
//    basicAuthentication.username: 'xxx'
//    basicAuthentication.password: 'yyy'
//  */
//  def parse(mode: Mode, configuration: Configuration): BasicAuthenticationFilterConfiguration = {
//    def key(k: String) = s"basicAuthentication.$k"
//
//    val enabled = mustGetConfigString(mode, configuration, key("enabled")).toBoolean
//    val realm = mustGetConfigString(mode, configuration, key("realm"))
//    val username = mustGetConfigString(mode, configuration, key("username"))
//    val password = mustGetConfigString(mode, configuration, key("password"))
//
//    BasicAuthenticationFilterConfiguration(realm,enabled,username,password)
//  }
//}
