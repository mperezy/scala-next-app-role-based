package controllers

import javax.inject._
import play.api.libs.json.{JsObject, Json}
import play.api.mvc._

/**
 * This controller creates an `Action` to handle HTTP requests to the application's home page.
 */
@Singleton
class HomeController @Inject() (val controllerComponents: ControllerComponents)
    extends BaseController {

  private type QueryParams = Map[String, Seq[String]]

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method will be called when the application receives a `GET`
   * request with a path of `/`.
   */
  def index(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  private def treatTestQueryParams(queryParams: QueryParams): (Option[(String, String)], Iterable[JsObject]) = {
    val params = for {
      (key, values) <- queryParams
      value <- values
    } yield key -> value

    val hasBadParam = params.find { value =>
      value._1 == "bad-param"
    }

    val result = params.map { value =>
      Json.obj(s"${value._1}" -> value._2)
    }

    (hasBadParam, result)
  }

  private def getTestHandlersResponse(queryParams: QueryParams): Result = {
    val (hasBadParam, result) = treatTestQueryParams(queryParams)

    hasBadParam match {
      case Some(_) =>
        BadRequest(Json.obj(
            "error" -> "Bad request. You sent 'bad-param'.",
            "bad-param" -> result
              .find { jsObject =>
                (jsObject \ "bad-param").isDefined
              }
              .flatMap { jsObject =>
                (jsObject \ "bad-param").asOpt[String]
              }
          ))

      case None =>
        Ok(Json.obj(
            "test" -> 123,
            "params" -> (
              if (result.nonEmpty) result else "No params sent"
            )
          ))
    }
  }

  // [GET] /test
  def testHandler(): Action[AnyContent] =
    Action { implicit request: Request[AnyContent] => getTestHandlersResponse(request.queryString) }
}
