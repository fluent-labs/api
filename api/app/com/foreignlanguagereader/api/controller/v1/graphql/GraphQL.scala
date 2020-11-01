package com.foreignlanguagereader.api.controller.v1.graphql

import com.foreignlanguagereader.api.controller.v1.graphql.queries.DefinitionQuery
import com.foreignlanguagereader.api.service.definition.DefinitionService
import javax.inject.Inject
import play.api.Logger
import play.api.libs.json.{JsObject, JsString, JsValue, Json}
import play.api.mvc.{Action, BaseController, ControllerComponents, Result}
import sangria.ast.Document
import sangria.execution.{ErrorWithResolver, Executor, QueryAnalysisError}
import sangria.marshalling.playJson._
import sangria.parser.{QueryParser, SyntaxError}
import sangria.renderer.SchemaRenderer
import sangria.schema.Schema

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class GraphQL @Inject()(val controllerComponents: ControllerComponents,
                        val definitionQuery: DefinitionQuery,
                        val definitionService: DefinitionService,
                        implicit val ec: ExecutionContext)
    extends BaseController {

  val logger: Logger = Logger(this.getClass)

  val schema: Schema[DefinitionService, Unit] = {
    val s = Schema(definitionQuery.query)
    logger.info(s"Loaded graphql schema:\n${SchemaRenderer.renderSchema(s)}")
    s
  }

  def parseVariables(variables: String): JsObject =
    if (variables.trim == "" || variables.trim == "null") Json.obj()
    else Json.parse(variables).as[JsObject]

  def graphql: Action[JsValue] = Action.async(parse.json) { request =>
    val query = (request.body \ "query").as[String]
    val operation = (request.body \ "operationName").asOpt[String]
    val variables: Option[JsObject] =
      (request.body \ "variables").toOption.flatMap {
        case JsString(vars) => Some(parseVariables(vars))
        case obj: JsObject  => Some(obj)
        case _              => None
      }

    QueryParser.parse(query) match {
      case Success(queryAst) =>
        executeGraphQLQuery(queryAst, operation, variables)
      case Failure(error: SyntaxError) =>
        Future.successful(BadRequest(Json.obj("error" -> error.getMessage)))
      case _ =>
        Future.successful(
          BadRequest(Json.obj("error" -> "failed to parse query"))
        )
    }
  }

  def executeGraphQLQuery(query: Document,
                          op: Option[String],
                          vars: Option[JsObject]): Future[Result] =
    (vars match {
      case Some(v) =>
        Executor
          .execute(
            schema,
            query,
            definitionService,
            variables = v,
            operationName = op
          )
      case None =>
        Executor
          .execute(schema, query, definitionService, operationName = op)
    }).map(Ok(_))
      .recover {
        case error: QueryAnalysisError => BadRequest(error.resolveError)
        case error: ErrorWithResolver =>
          InternalServerError(error.resolveError)
      }
}
