package controllers

import auth.Auth0Action
import auth.Auth0Models._
import com.google.inject._
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json, Reads, Writes}

import scala.concurrent.{ExecutionContext, Future}
import play.api.mvc._

@Singleton
class Auth0Controller @Inject()(val controllerComponents: ControllerComponents, auth0Action: Auth0Action)(
  implicit ec: ExecutionContext)
    extends BaseController {

  implicit val auth0LoginRequestReads: Reads[Auth0LoginRequest] = Json.reads[Auth0LoginRequest]
  implicit val auth0TokenResponseWrites: Writes[Auth0TokenResponse] = Json.writes[Auth0TokenResponse]

  // [GET] /auth0/get-users
  def getUsers: Action[AnyContent] =
    auth0Action.async { implicit request =>
      for {
        response <- auth0Action.getUsers(request)
      } yield {
        response
      }
    }

  // [GET] /auth0/get-management-token
  def getManagementToken: Action[AnyContent] = auth0Action.async { implicit request =>
    for {
      response <- auth0Action.getAuth0ManagementToken(request)
    } yield {
      response
    }
  }

  // [POST] /auth0/login
  def makeLogin: Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[Auth0LoginRequest] match {
      case JsSuccess(body, _) =>
        auth0Action.login(body.email, body.password).map {
          case Right(token)       => Ok(Json.toJson(token))
          case Left(errorMessage) => Unauthorized(Json.obj("error" -> errorMessage))
        }
      case JsError(error) =>
        Future.successful(BadRequest(Json.obj("message" -> "Invalid request", "details" -> JsError.toJson(error))))
    }
  }

  // [POST] /auth0/logout
  def makeLogout: Action[AnyContent] =
    auth0Action.async { implicit request =>
      for {
        response <- auth0Action.revokeToken(request)
      } yield {
        response
      }
    }
}
