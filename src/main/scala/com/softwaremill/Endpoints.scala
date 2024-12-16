package com.softwaremill

import cats.effect.IO
import io.circe.generic.auto.*
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.swagger.bundle.SwaggerInterpreter

object Endpoints:

  val service: TitleCrawlerService[IO] = TitleCrawler

  val getTitles = endpoint.post
    .tag("Title Crawler endpoints")
    .in("titles")
    .in(jsonBody[List[Url]]
      .description("List of full specified urls")
      .default(List(
        "http://web.simmons.edu",
        "https://example.com/",
        "http://google.com/generate_204"
    )))
    .out(jsonBody[TitleCrawlerResponse]
      .description("Url -> result, splited by success and failure")
      .default(TitleCrawlerResponse(
        Map(
          "http://web.simmons.edu" -> "Welcome",
          "https://example.com/" -> "Example Domain",
        ),
        Map("http://google.com/generate_204" -> urlErrorToMessage(UrlError.TitleNotFound))
      ))
    )
    .serverLogicSuccess[IO](service.getTags)

  val apiEndpoints: List[ServerEndpoint[Any, IO]] = List(getTitles)

  val docEndpoints: List[ServerEndpoint[Any, IO]] = SwaggerInterpreter()
    .fromServerEndpoints[IO](apiEndpoints, "Title Crawler", "1.0.0")

  val all: List[ServerEndpoint[Any, IO]] = apiEndpoints ++ docEndpoints
