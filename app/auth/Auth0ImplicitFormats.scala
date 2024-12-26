package auth

import Auth0Models._
import play.api.libs.json.{Format, JsError, JsResult, JsString, JsSuccess, JsValue, Json, Reads, Writes}

object Auth0ImplicitFormats {
  implicit val auth0TokenResponseReads: Reads[Auth0TokenResponse] = Json.reads[Auth0TokenResponse]

  implicit val auth0ManagementTokenResponseReads: Reads[Auth0ManagementTokenResponse] =
    Json.reads[Auth0ManagementTokenResponse]
  implicit val auth0ManagementTokenResponseWrites: Writes[Auth0ManagementTokenResponse] =
    Json.writes[Auth0ManagementTokenResponse]

  implicit val roleFormat: Format[Role] = new Format[Role] {
    override def writes(role: Role): JsValue = JsString(role.toString)
    override def reads(json: JsValue): JsResult[Role] = json match {
      case JsString("Admin")    => JsSuccess(Admin)
      case JsString("Moderator") => JsSuccess(Moderator)
      case JsString("User")      => JsSuccess(User)
      case _                     => JsError("Unknown role")
    }
  }

  implicit val appMetadataFormat: Format[AppMetadata] = Json.format[AppMetadata]
  implicit val auth0UserResponseReads: Reads[Auth0UserResponse] = Json.reads[Auth0UserResponse]
  implicit val auth0UserResponseWrites: Writes[Auth0UserResponse] = Json.writes[Auth0UserResponse]
}
