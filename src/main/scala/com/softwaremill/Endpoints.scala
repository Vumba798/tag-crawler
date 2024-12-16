package com.softwaremill

import sttp.tapir.*
import cats.effect.IO
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.swagger.bundle.SwaggerInterpreter

import java.net.{MalformedURLException, URL}
import scala.util.Try

object Endpoints:

  val getTags = endpoint.post
    .in("tags")
    .in(jsonBody[List[String]])
    .out(jsonBody[Map[String, Either[UrlError, String]]])
    .serverLogicSuccess[IO](TagCrawlerService.getTags)

  val apiEndpoints: List[ServerEndpoint[Any, IO]] = List(getTags)

  val docEndpoints: List[ServerEndpoint[Any, IO]] = SwaggerInterpreter()
    .fromServerEndpoints[IO](apiEndpoints, "tag-crawler", "1.0.0")

  val all: List[ServerEndpoint[Any, IO]] = apiEndpoints ++ docEndpoints
