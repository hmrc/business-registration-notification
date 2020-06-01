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

package basicauth

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.http.HeaderNames.{AUTHORIZATION, WWW_AUTHENTICATE}
import play.api.mvc._

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
    authConfig.enabled match {
      case false => None // bypassed
      case _ => {
        val creds = BasicAuthCredentials.fromAuthorizationHeader(request.headers.get(AUTHORIZATION))
        if (authConfig.matches(creds)) {
          None // valid credentials: allow subsequent operations to proceed
        } else {
          Some(Results.Unauthorized.withHeaders(WWW_AUTHENTICATE -> authConfig.basicRealm))
        }
      }
    }
  }
}

class BasicAuthenticationFilterConfiguration(realm: String, val enabled: Boolean,
                                             username: String, password: String) {
  val basicRealm = "Basic realm=\"" + realm + "\""

  def matches(other: Option[BasicAuthCredentials]): Boolean = {
    other match {
      case None => false
      case Some(creds) => username == creds.username && password == creds.password
    }
  }
}

trait BasicAuthentication {
  val config: Configuration

  def getBasicAuthConfig(): BasicAuthenticationFilterConfiguration = {
    def key(k: String) = s"basicAuthentication.$k"

    val enabled = config.getString(key("enabled")).get.toBoolean
    val realm = config.getString(key("realm")).get
    val username = config.getString(key("username")).get
    val password = config.getString(key("password")).get

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
