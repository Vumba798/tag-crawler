package com.softwaremill

import cats.data.*
import cats.*
import cats.syntax.all.*
import cats.effect.instances.all.*
import cats.effect.*
import sttp.client3.*
import sttp.model.*
import org.typelevel.log4cats.{Logger, LoggerName}
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.syntax.*
import sttp.capabilities
import sttp.client3.httpclient.cats.HttpClientCatsBackend

import org.jsoup.Jsoup
import org.jsoup.nodes.*

import java.net.URL
import scala.util.Try

trait TitleCrawlerService[F[_]]:
  def getTags(urls: List[String]): F[TitleCrawlerResponse]

object TitleCrawler extends TitleCrawlerService[IO]:

  given Logger[IO] = Slf4jLogger.getLogger[IO]

  val clientBackend = HttpClientCatsBackend.resource[IO]()

  def getTags(urls: List[Url]): IO[TitleCrawlerResponse] =
    clientBackend.use { backend =>
      for
        results <-
          urls.parTraverse { url =>
            getTag(backend, url)
              .map(result => result.bimap(url -> urlErrorToMessage(_), url -> _))
          }
        (failures, successes) = results.partitionMap(identity)
      yield TitleCrawlerResponse(successes.toMap, failures.toMap)
    }

  // -----------------------------------------------------------------------

  def getTag(backend: SttpBackend[IO, ?], url: String): IO[Either[UrlError, String]] =
    (for
      _        <- EitherT.fromEither(validateProtocol(url))
      validUrl <- EitherT.fromEither(validateUrl(url))
      html <-
        EitherT(getHtmlRequest(validUrl).send(backend).map {
          case response if response.code.isSuccess => response.body
          case response if response.code == StatusCode.NotFound => UrlError.UrlNotFound.asLeft
          case response if response.code.isServerError =>
            error"server error response for $validUrl: ${response.statusText}"
            UrlError.ServerError.asLeft
          case response if response.code.isClientError =>
            error"client error response for $validUrl: ${response.statusText}"
            UrlError.ClientError.asLeft
          case _ => UrlError.UnknownError.asLeft
        })
      title <- EitherT.fromEither(extractTitleTag(html))
    yield title).value

  def isValidUrl(url: Url): Boolean =
    Try(URL(url).toURI).isSuccess

  def validateProtocol(url: Url): Either[UrlError, String] =
    if url.startsWith("http://") || url.startsWith("https://") then url.asRight
    else UrlError.InvalidProtocol.asLeft

  def validateUrl(url: String): Either[UrlError, String] =
    if isValidUrl(url) then url.asRight
    else UrlError.IsNotValidUrl.asLeft

  def getHtmlRequest(url: Url): RequestT[Identity, Either[UrlError, String], Any] =
    basicRequest
      .get(uri"$url")
      .response(asString)
      .mapResponse {
        case Left(errorMsg) =>
          error"bad response for $url: $errorMsg"
          UrlError.ServerError.asLeft
        case Right(html)    => html.asRight
      }

  def extractTitleTag(html: String): Either[UrlError, String] =
    Try {
      Jsoup.parse(html).title() match
        case "" => UrlError.TitleNotFound.asLeft
        case someTitle => someTitle.asRight
    } getOrElse UrlError.BadHtmlError.asLeft


end TitleCrawler
