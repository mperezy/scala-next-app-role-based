package auth

import Auth0Models._
import Auth0ImplicitFormats._

import com.google.inject.Inject
import pdi.jwt.JwtClaim
import play.api.http.HeaderNames
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.libs.ws._
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class Auth0Action @Inject()(
  ws: WSClient,
  bodyParser: BodyParsers.Default,
  auth0Model: Auth0Model
)(implicit ec: ExecutionContext)
    extends ActionBuilder[UserRequest, AnyContent] {
  private type RequestWithToken[T] = (JwtClaim, String, Request[T])
  private val headerTokenRegex = """Bearer (.+?)""".r

  override def parser: BodyParser[AnyContent] = bodyParser
  override protected def executionContext: ExecutionContext = ec

  override def invokeBlock[A](request: Request[A], block: UserRequest[A] => Future[Result]): Future[Result] =
    processRequestWithToken(request,
      (requestWithToken: RequestWithToken[A]) =>
        block(UserRequest(requestWithToken._1, requestWithToken._2, requestWithToken._3)))

  private def processRequestWithToken[T](request: Request[T],
    callback: RequestWithToken[T] => Future[Result]): Future[Result] =
    extractBearerToken(request) map { token =>
      auth0Model.validateJwt(token) match {
        case Success(claim: JwtClaim) => callback(claim, token, request)
        case Failure(exception) =>
          Future.successful(
            Results.Unauthorized(
              Json.obj(
                "message" -> "You are not authorized.",
                "error" -> exception.getMessage
              )
            )
          )
      }
    } getOrElse {
      Future.successful(
        Results.Unauthorized(
          Json.obj(
            "message" -> "You are not authorized."
          )
        )
      )
    }

  private def extractBearerToken[A](request: Request[A]): Option[String] = {
    request.headers.get(HeaderNames.AUTHORIZATION) collect { case headerTokenRegex(token) =>
      token
    }
  }

  def login(email: String, password: String): Future[Either[JsValue, Auth0TokenResponse]] = {
    val auth0Url = s"${auth0Model.getAuth0URL}/oauth/token"

    val requestBody = Json.obj(
      "grant_type" -> "password",
      "username" -> email,
      "password" -> password,
      "client_id" -> auth0Model.getClientId,
      "client_secret" -> auth0Model.getClientSecret,
      "audience" -> auth0Model.getIdentifier,
      "scope" -> "openid profile email",
      "connection" -> "Username-Password-Authentication"
    )

    ws.url(auth0Url)
      .addHttpHeaders("Content-Type" -> "application/json")
      .post(requestBody)
      .map { response =>
        response.status match {
          case 200 =>
            response.json.validate[Auth0TokenResponse] match {
              case JsSuccess(token, _) => Right(token)
              case JsError(errors) =>
                Left(Json.obj(
                  "message" -> "Invalid token response format",
                  "details" -> JsError.toJson(errors)
                ))

            }
          case _ =>
            Left(Json.obj(
              "message" -> "Error from Auth0",
              "details" -> response.json
            ))
        }
      }
  }

  def getAuth0ManagementToken[T](request: Request[T]): Future[Result] = {
    processRequestWithToken(request,
      (requestWithToken: RequestWithToken[T]) => {
        val auth0ManagementURL = s"${auth0Model.getAuth0URL}/oauth/token"
        val requestBody = Json.obj(
          "client_id" -> auth0Model.getClientId,
          "client_secret" -> auth0Model.getClientSecret,
          "audience" -> auth0Model.getIdentifier,
          "grant_type" -> "client_credentials"
        )

        ws.url(auth0ManagementURL)
          .addHttpHeaders("Content-Type" -> "application/json")
          .post(requestBody)
          .map { response =>
            response.status match {
              case 200 =>
                response.json.validate[Auth0ManagementTokenResponse] match {
                  case JsSuccess(response, _) => Results.Ok(Json.toJson(response))
                  case JsError(errors) =>
                    Results
                      .InternalServerError(Json.obj("message" -> "Bad schema", "details" -> JsError.toJson(errors)))
                }
              case _ => Results.InternalServerError(Json.obj("message" -> "", "details" -> response.json))
            }
          }
      })
  }

  def revokeToken[T](request: Request[T]): Future[Result] =
    processRequestWithToken(
      request,
      (requestWithToken: RequestWithToken[T]) =>
        {
          val revokeUrl = s"${auth0Model.getAuth0URL}/oauth/revoke"
          val requestBody = Json.obj(
            "client_id" -> auth0Model.getClientId,
            "client_secret" -> auth0Model.getClientSecret,
            "token" -> requestWithToken._2
          )

          ws.url(revokeUrl)
            .addHttpHeaders("Content-Type" -> "application/json")
            .post(requestBody)
            .map { response =>
              response.status match {
                case 200 => Results.Ok(Json.obj("message" -> "Token revoked successfully"))
                case _ =>
                  Results.BadRequest(Json.obj("message" -> "Failed to revoke token", "details" -> response.json))
              }
            }
        }.recover {
          case ex: java.net.SocketTimeoutException =>
            Results.GatewayTimeout(
              Json.obj("message" -> "Request timed out", "error" -> ex.getMessage)
            )
          case ex: java.net.UnknownHostException =>
            Results.BadGateway(
              Json.obj("message" -> "Service unavailable", "error" -> ex.getMessage)
            )
          case ex: Exception =>
            Results.InternalServerError(
              Json.obj("message" -> "An unexpected error occurred", "error" -> ex.getMessage)
            )
        }
    )

  def getUsers[T](request: Request[T]): Future[Result] =
    processRequestWithToken(
      request,
      (requestWithToken: RequestWithToken[T]) =>
        ws.url(s"${auth0Model.getIssuerURL}/api/v2/users")
          .addHttpHeaders(HeaderNames.AUTHORIZATION -> s"Bearer ${requestWithToken._2}")
          .get()
          .map { response =>
            response.status match {
              case 200 =>
                response.json.validate[List[Auth0UserResponse]] match {
                  case JsSuccess(response, _) => Results.Ok(Json.obj("users" -> Json.toJson(response)))
                  case JsError(errors) =>
                    Results
                      .InternalServerError(Json.obj("message" -> "Bad schema", "details" -> JsError.toJson(errors)))
                }
              case status => Results.Status(status)(response.json)
            }
          }
          .recover {
            case ex: java.net.SocketTimeoutException =>
              Results.GatewayTimeout(
                Json.obj("message" -> "Request timed out", "error" -> ex.getMessage)
              )
            case ex: java.net.UnknownHostException =>
              Results.BadGateway(
                Json.obj("message" -> "Service unavailable", "error" -> ex.getMessage)
              )
            case ex: Exception =>
              Results.InternalServerError(
                Json.obj("message" -> "An unexpected error occurred", "error" -> ex.getMessage)
              )
          }
    )
}
