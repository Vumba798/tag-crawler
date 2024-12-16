package tag.crawler

type Url = String

enum UrlError:
  case InvalidProtocol
  case IsNotValidUrl
  case UrlNotFound
  case ServerError
  case ClientError
  case BadHtmlError
  case TitleNotFound
  case UnknownError

def urlErrorToMessage(error: UrlError): String =
  import UrlError.*
  error match
    case InvalidProtocol => "protocol is invalid"
    case IsNotValidUrl   => "url is not valid"
    case UrlNotFound     => "resource is not found"
    case ServerError     => "server is not responding"
    case ClientError     => "failed while fetching data"
    case BadHtmlError    => "couldn't parse html"
    case TitleNotFound   => "title tag not found"
    case UnknownError    => "unknown failure while processing url"

case class TitleCrawlerResponse(
  successful: Map[Url, String],
  failed: Map[Url, String]
)
