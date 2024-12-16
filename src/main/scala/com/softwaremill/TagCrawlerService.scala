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

enum UrlError:
  case MustStartWithHttp
  case IsNotValidUrl
  case UrlNotFound
  case ServerError
  case ClientError
  case BadHtmlError
  case TitleNotFound
  case UnknownError

object TagCrawlerService:

  given Logger[IO] = Slf4jLogger.getLogger[IO]

  private val clientBackend = HttpClientCatsBackend.resource[IO]()

  def getTags(urls: List[String]): IO[Map[String, Either[UrlError, String]]] =
    clientBackend.use { backend =>
      for
        result <- urls.traverse(url => getTag(backend, url).map(url -> _))
      yield result.toMap
    }

  // -----------------------------------------------------------------------
  
  def isValidUrl(url: String): Boolean =
    Try(URL(url).toURI).isSuccess

  def validateProtocol(url: String): Either[UrlError, String] =
    if url.startsWith("http://") then url.asRight
    else UrlError.MustStartWithHttp.asLeft

  def validateUrl(url: String): Either[UrlError, String] =
    if isValidUrl(url) then url.asRight
    else UrlError.IsNotValidUrl.asLeft

  def getHtmlRequest(url: String): RequestT[Identity, Either[UrlError, String], Any] =
    basicRequest
      .get(uri"$url")
      .response(asString)
      .mapResponse {
        case Left(errorMsg) => UrlError.ServerError.asLeft
        case Right(html)    => html.asRight
      }

  def extractNameTag(html: String): Either[UrlError, String] =
    Try {
      Jsoup.parse(html).title() match
        case "" => UrlError.TitleNotFound.asLeft
        case someTitle => someTitle.asRight
    } getOrElse UrlError.BadHtmlError.asLeft

  def getTag(backend: SttpBackend[IO, ?], url: String): IO[Either[UrlError, String]] =
    (for
      _ <- EitherT.fromEither(validateProtocol(url))
      validUrl <- EitherT.fromEither(validateUrl(url))
      html <- EitherT(getHtmlRequest(validUrl).send(backend).map {
        case response if response.code.isSuccess => response.body
        case response if response.code.isServerError => UrlError.ServerError.asLeft
        case response if response.code.isClientError => UrlError.ClientError.asLeft
        case _ => UrlError.UnknownError.asLeft
      })
      title <- EitherT.fromEither(extractNameTag(html))
    yield title).value

end TagCrawlerService

