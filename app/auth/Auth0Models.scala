package auth

import pdi.jwt.JwtClaim
import play.api.mvc.{Request, WrappedRequest}

object Auth0Models {
  case class UserRequest[A](jwt: JwtClaim, token: String, request: Request[A]) extends WrappedRequest[A](request)
  case class Auth0LoginRequest(email: String, password: String)
  case class Auth0TokenResponse(access_token: String, token_type: String)

  sealed trait Role
  case object Admin extends Role
  case object Moderator extends Role
  case object User extends Role
  case class AppMetadata(
    role: Role
  )
  case class Auth0UserResponse(
    name: String,
    email_verified: Boolean,
    created_at: String,
    picture: String,
    updated_at: String,
    last_password_reset: Option[String],
    user_id: String,
    email: String,
    nickname: String,
    last_login: Option[String],
    logins_count: Option[Int],
    app_metadata: Option[AppMetadata]
  )
  case class Auth0ManagementTokenResponse(access_token: String, scope: String, expires_in: Int, token_type: String)
}
