package com.decafhub.decaf.client.core

import cats.effect.{IO, Sync}
import cats.syntax.all._
import com.softwaremill.sttp._
import com.softwaremill.sttp.circe._
import io.circe.generic.auto._
import io.circe.{Decoder, Json}

import scala.concurrent.duration.Duration
import scala.language.higherKinds

/** Provides an *identifiable* record data model for remote *barista* API entities.
  *
  * @tparam T Identifier type parameter.
  */
trait Record[T] {

  /** Returns the identifier of the record.
    *
    * @return The identifier of the record.
    */
  def id: T
}

/** Provides a representation for remote *barista* API version information.
  *
  * @param version Version of the remote *barista* API.
  */
case class Version(version: String)

/** Provides the currency record type.
  *
  * @param code Code of the currency.
  * @param name Name of the currency.
  * @param decimals Number of decimal points for the currency.
  */
case class Currency(
    code: String,
    name: String,
    decimals: Int,
) extends Record[String] {

  /** Returns the identifier of the record.
    *
    * @return The identifier of the record.
    */
  override def id: String = code
}

/** Provides a base trait for the remote *barista* API client algebra.
  *
  * @tparam F Context type parameter.
  */
trait BaristaClient[F[_]] {

  /** Attempts to *GET* the remote API resource(s) at the given `path` as per the given *parameters*.
    *
    * @param path The relative path to remote API resource(s).
    * @param params Parameters for the remote API resource(s) retrieval.
    * @param decoder Implicit JSON decoder for the resource(s) type.
    * @tparam T Resource(s) type.
    * @return Retrieved resource(s).
    */
  def get[T](
      path: String,
      params: BaristaClient.Params,
  )(implicit decoder: Decoder[T]): F[T]

  /** Provides a convenience method to [[BaristaClient#get]] method with no parameters.
    *
    * @param path The relative path to remote API resource(s).
    * @param decoder Implicit JSON decoder for the resource(s) type.
    * @tparam T Resource(s) type.
    * @return Retrieved resource(s).
    */
  def get[T](path: String)(implicit
      decoder: Decoder[T],
  ): F[T] = get(path, Map.empty)

  /** Attempts to *POST* *payload* to a remote endpoint at the given `path` as per the given *parameters*.
    *
    * @param path The relative path to remote API resource(s).
    * @param params Parameters for the remote API endpoint.
    * @param payload [[Json]] content to be post.
    * @param decoder Implicit JSON decoder for the return type.
    * @tparam T Return type.
    * @return Endpoint return value.
    */
  def post[T](
      path: String,
      params: BaristaClient.Params,
      payload: Json,
  )(implicit decoder: Decoder[T]): F[T]

  /** Provides a convenience method to [[BaristaClient#post]] method with no parameters.
    *
    * @param path The relative path to remote API resource(s).
    * @param payload [[Json]] content to be post.
    * @param decoder Implicit JSON decoder for the return type.
    * @tparam T Return type.
    * @return Endpoint return value.
    */
  def post[T](path: String, payload: Json)(
      implicit decoder: Decoder[T],
  ): F[T] = post(path, Map.empty, payload)

  /** Attempts to *DELETE* the remote resource(s).
    *
    * @param path The relative path to remote API resource(s).
    * @param params Parameters for the remote API endpoint.
    * @return Nothing.
    */
  def delete(
      path: String,
      params: BaristaClient.Params,
  ): F[Unit]

  /** Provides a convenience method to [[BaristaClient#delete]] method with no parameters.
    *
    * @param path The relative path to remote API resource(s).
    * @return Nothing.
    */
  def delete(path: String): F[Unit] =
    delete(path, Map.empty)

  /** Returns all the records from the path as for the given filters.
    *
    * Note that the pagination and format parameters will be removed from the filters before hitting the API.
    *
    * @param path The URL path (relative the base API URL) which to retrieve all records from.
    * @param filters Record list filters (pagination and format parameters will be forcefully removed).
    * @tparam A Type of records.
    * @return List of all (filtered) records.
    */
  def getRecords[A <: Record[_]](
      path: String,
      filters: BaristaClient.Params,
  )(implicit dec: Decoder[A]): F[List[A]] = {
    get(
      path,
      (filters -- List(
        "page",
        "page_size",
        "format",
        "_fields",
      )).updated("page_size", "-1"),
    )
  }

  /** Returns the remote API version.
    *
    * @return The remove API version.
    */
  def version: F[Version] =
    get[Version]("version")

  /** Returns the list of defined currencies.
    *
    * @return [[List]] of currencies.
    */
  def currencies: F[List[Currency]] =
    get[List[Currency]](
      "currencies",
      Map("universe" -> "1"),
    )
}

/** Defines an object to provide common API idioms.
  */
object BaristaClient {

  /** Provides a convenience constructor for tagless final encoding.
    */
  def apply[F[_]](implicit
      ev: BaristaClient[F],
  ): BaristaClient[F] = ev

  /** Defines a type alias for query string parameters.
    */
  type Params = Map[String, String]

  /** Defines a fatal error class for denoting remote API interaction errors.
    *
    * @param msg Error message.
    */
  case class Failure(msg: String)
      extends Exception(msg)

  /** Default the default instance for `Logger[IO]`
    */
  def io(
      url: Uri,
      key: String,
      secret: String,
  ): SyncBaristaClient[IO] = {
    new SyncBaristaClient[IO](
      url: Uri,
      key: String,
      secret: String,
    )
  }
}

/** Provides the canonical [[BaristaClient]] implementation for *barista*.
  *
  * @param url Base API URL.
  * @param key API authentication key.
  * @param secret API authentication secret.
  * @param F Evidence for context type parameter.
  * @tparam F Context type parameter.
  */
class SyncBaristaClient[F[_]](
    url: Uri,
    key: String,
    secret: String,
)(implicit F: Sync[F])
    extends BaristaClient[F] {

  /** Defines the STTP backend to be used.
    */
  implicit private val backend
      : SttpBackend[Id, Nothing] =
    HttpURLConnectionBackend()

  /** Defines the sanitised base URL.
    */
  private val baseurl: Uri =
    uri"${url.toString.stripSuffix("/")}"

  /** Defines default headers which will be send along with each request.
    */
  private val defaultHeaders = Map(
    "Accept"        -> "application/json",
    "Authorization" -> s"Key $key:$secret",
  )

  /** Defines the base STTP client.
    */
  private val client = F.pure(
    sttp
      .headers(defaultHeaders)
      .readTimeout(Duration.Inf),
  )

  /** Defines a function to build remote API URLs.
    *
    * @param path Relative Path to the endpoint of interest.
    * @param params URL Parameters.
    * @return A [[Uri]] to the remote endpoint.
    */
  private def buildURL(
      path: String,
      params: BaristaClient.Params,
  ): Uri = {
    uri"$baseurl/${path.stripPrefix("/").stripSuffix("/")}/"
      .params(params)
  }

  /** Attempts to *GET* the remote API resource(s) at the given `path` as per the given *parameters*.
    *
    * @param path    The relative path to remote API resource(s).
    * @param params  Parameters for the remote API resource(s) retrieval.
    * @param decoder Implicit JSON decoder for the resource(s) type.
    * @tparam T Resource(s) type.
    * @return Retrieved resource(s).
    */
  override def get[T](
      path: String,
      params: BaristaClient.Params,
  )(implicit decoder: Decoder[T]): F[T] =
    client.map { c =>
      // Get the URL to hit:
      val url = buildURL(path, params)

      // Proceed:
      c.get(url)
        .response(asJson[T])
        .send()
        .body match {
        case Left(message) =>
          throw BaristaClient.Failure(
            s"Remote API problem while hitting $url: $message",
          )
        case Right(response) =>
          response match {
            case Left(message) =>
              throw BaristaClient.Failure(
                s"Remote API content problem while hitting $url: ${message.message}, ${message.original}",
              )
            case Right(retval) =>
              retval
          }
      }
    }

  /** Attempts to *POST* *payload* to a remote endpoint at the given `path` as per the given *parameters*.
    *
    * @param path    The relative path to remote API resource(s).
    * @param params  Parameters for the remote API resource(s) retrieval.
    * @param payload [[Json]] content to be post.
    * @param decoder Implicit JSON decoder for the return type.
    * @tparam T Return type.
    * @return Endpoint return value.
    */
  override def post[T](
      path: String,
      params: BaristaClient.Params,
      payload: Json,
  )(implicit decoder: Decoder[T]): F[T] =
    client.map { c =>
      // Get the URL to hit:
      val url = buildURL(path, params)

      // Define  the request:
      val request = c
        .post(url)
        .header(
          "Content-Type",
          "application/json",
        )
        .body(payload.noSpaces)
        .response(asJson[T])

      // Proceed:
      request.send().body match {
        case Left(message) =>
          throw BaristaClient.Failure(
            s"Remote API problem while hitting $url: $message",
          )
        case Right(response) =>
          response match {
            case Left(message) =>
              throw BaristaClient.Failure(
                s"Remote API content problem while hitting $url: ${message.message}, ${message.original}",
              )
            case Right(retval) =>
              retval
          }
      }
    }

  /** Attempts to *DELETE* the remote resource(s).
    *
    * @param path   The relative path to remote API resource(s).
    * @param params Parameters for the remote API endpoint.
    * @return Nothing.
    */
  override def delete(
      path: String,
      params: BaristaClient.Params,
  ): F[Unit] = client.map { c =>
    // Get the URL to hit:
    val url = buildURL(path, params)

    // Proceed:
    c.delete(url)
      .response(ignore)
      .send()
      .body match {
      case Left(message) =>
        throw BaristaClient.Failure(
          s"Remote API problem while hitting $url: $message",
        )
      case Right(_) => ()
    }
  }
}
