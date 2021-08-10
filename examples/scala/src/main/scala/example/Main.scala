package example

import com.kaiko.sdk.StreamAggregatesOHLCVServiceV1Grpc
import com.kaiko.sdk.StreamAggregatesSpotExchangeRateServiceV1Grpc
import com.kaiko.sdk.StreamAggregatesVWAPServiceV1Grpc
import com.kaiko.sdk.StreamMarketUpdateServiceV1Grpc
import com.kaiko.sdk.StreamTradesServiceV1Grpc
import com.kaiko.sdk.core.InstrumentCriteria
import com.kaiko.sdk.stream.aggregates_ohlcv_v1.StreamAggregatesOHLCVRequestV1
import com.kaiko.sdk.stream.aggregates_spot_exchange_rate_v1.StreamAggregatesSpotExchangeRateRequestV1
import com.kaiko.sdk.stream.aggregates_vwap_v1.StreamAggregatesVWAPRequestV1
import com.kaiko.sdk.stream.market_update_v1.StreamMarketUpdateCommodity
import com.kaiko.sdk.stream.market_update_v1.StreamMarketUpdateRequestV1
import com.kaiko.sdk.stream.trades_v1.StreamTradesRequestV1
import io.grpc.{CallCredentials, Channel, ManagedChannelBuilder, Metadata, Status}

import java.util.concurrent.Executor
import scala.concurrent.ExecutionContext
import scala.util.Try

object Main {
  def main(args: Array[String]) = {
    implicit val ec = ExecutionContext.global

    // Setup runtime
    val builder = ManagedChannelBuilder.forAddress("gateway-v0-grpc.kaiko.ovh", 443)
    builder.executor(ec)

    // Setup authentication
    val AUTHORIZATION_METADATA_KEY = Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER)
    val apiKey = sys.env.getOrElse("KAIKO_API_KEY", "1234") // Put your api key here
    val headers = new Metadata()
    headers.put(AUTHORIZATION_METADATA_KEY, s"Bearer $apiKey")

    val channel = builder.build()
    val callCredentials = new CallCredentials {
      override def applyRequestMetadata(requestInfo: CallCredentials.RequestInfo, executor: Executor, applier: CallCredentials.MetadataApplier): Unit = {
        Try {
          applier.apply(headers)
        }.recover {
          case e: Throwable => applier.fail(Status.UNAUTHENTICATED.withCause(e))
        }
      }

      override def thisUsesUnstableApi(): Unit = {}
    }

    // Create a streaming trades request with SDK
    trades_request(channel, callCredentials)

    // Create a streaming ohlcv request with SDK
    ohlcv_request(channel, callCredentials)

    // Create a streaming vwap request with SDK
    vwap_request(channel, callCredentials)

    // Create a streaming spot exchange rate request with SDK
    spot_exchange_rate_request(channel, callCredentials)

    // Create a streaming market update request with SDK
    market_update_request(channel, callCredentials)
  }

  def market_update_request(channel: Channel, callCredentials: CallCredentials) = {
    val stub = StreamMarketUpdateServiceV1Grpc.blockingStub(channel).withCallCredentials(callCredentials)

    // Create a request with SDK
    val request = StreamMarketUpdateRequestV1(
      instrumentCriteria = Some(InstrumentCriteria(
        exchange = "krkn",
        instrumentClass = "spot",
        code = "*"
      )),
      commodities = List(StreamMarketUpdateCommodity.SMUC_TRADE)
    )

    // Run the request and get results
    val results = stub.subscribe(request)
      .take(10)
      .toSeq

    println(results)
  }

  def ohlcv_request(channel: Channel, callCredentials: CallCredentials) = {
    val stub = StreamAggregatesOHLCVServiceV1Grpc.blockingStub(channel).withCallCredentials(callCredentials)

    // Create a request with SDK
    val request = StreamAggregatesOHLCVRequestV1(
      instrumentCriteria = Some(InstrumentCriteria(
        exchange = "cbse",
        instrumentClass = "spot",
        code = "btc-usd"
      )),
      aggregate = "1s",
    )

    // Run the request and get results
    val results = stub.subscribe(request)
      .take(10)
      .toSeq

    println(results)
  }

  def vwap_request(channel: Channel, callCredentials: CallCredentials) = {
    val stub = StreamAggregatesVWAPServiceV1Grpc.blockingStub(channel).withCallCredentials(callCredentials)

    // Create a request with SDK
    val request = StreamAggregatesVWAPRequestV1(
      instrumentCriteria = Some(InstrumentCriteria(
        exchange = "cbse",
        instrumentClass = "spot",
        code = "btc-usd"
      )),
      aggregate = "1s",
    )

    // Run the request and get results
    val results = stub.subscribe(request)
      .take(10)
      .toSeq

    println(results)
  }

  def spot_exchange_rate_request(channel: Channel, callCredentials: CallCredentials) = {
    val stub = StreamAggregatesSpotExchangeRateServiceV1Grpc.blockingStub(channel).withCallCredentials(callCredentials)

    // Create a request with SDK
    val request = StreamAggregatesSpotExchangeRateRequestV1(
      aggregate = "1s",
      code = "btc-usd"
    )

    // Run the request and get results
    val results = stub.subscribe(request)
      .take(10)
      .toSeq

    println(results)
  }

  def trades_request(channel: Channel, callCredentials: CallCredentials) = {
    val stub = StreamTradesServiceV1Grpc.blockingStub(channel).withCallCredentials(callCredentials)

    // Create a request with SDK
    val request = StreamTradesRequestV1(
      instrumentCriteria = Some(InstrumentCriteria(
        exchange = "cbse",
        instrumentClass = "spot",
        code = "btc-usd"
      ))
    )

    // Run the request and get results
    val results = stub.subscribe(request)
      .take(10)
      .toSeq

    println(results)
  }
}
