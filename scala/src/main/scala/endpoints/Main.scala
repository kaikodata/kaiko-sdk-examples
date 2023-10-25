package endpoints

import com.kaiko.sdk.{StreamAggregatedQuoteServiceV2Grpc, StreamAggregatesOHLCVServiceV1Grpc, StreamAggregatesVWAPServiceV1Grpc, StreamIndexServiceV1Grpc, StreamMarketUpdateServiceV1Grpc, StreamTradesServiceV1Grpc}
import com.kaiko.sdk.core.InstrumentCriteria
import com.kaiko.sdk.stream.aggregated_quote_v2.StreamAggregatedQuoteRequestV2
import com.kaiko.sdk.stream.aggregates_ohlcv_v1.StreamAggregatesOHLCVRequestV1
import com.kaiko.sdk.stream.aggregates_vwap_v1.StreamAggregatesVWAPRequestV1
import com.kaiko.sdk.stream.market_update_v1.StreamMarketUpdateCommodity
import com.kaiko.sdk.stream.market_update_v1.StreamMarketUpdateRequestV1
import com.kaiko.sdk.stream.trades_v1.StreamTradesRequestV1
import com.kaiko.sdk.stream.index_v1.StreamIndexServiceRequestV1
import io.grpc.{CallCredentials, Channel, ManagedChannelBuilder, Metadata, Status}

import java.util.concurrent.Executor
import scala.concurrent.ExecutionContext
import scala.util.Try
import com.kaiko.sdk.StreamIndexMultiAssetsServiceV1Grpc
import com.kaiko.sdk.stream.index_multi_assets_v1.StreamIndexMultiAssetsServiceRequestV1
import com.kaiko.sdk.StreamIndexForexRateServiceV1Grpc
import com.kaiko.sdk.stream.index_forex_rate_v1.StreamIndexForexRateServiceRequestV1

object Main {
  def main(args: Array[String]): Unit = {
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

    // Create a streaming market update request with SDK
    market_update_request(channel, callCredentials)

    // Create a streaming index rate request with SDK
    index_rate_request(channel, callCredentials)

    // Create a streaming index multi asset request with SDK
    index_multi_asset_request(channel, callCredentials)

    // Create a streaming index forex rate request with SDK
    index_forex_rate_request(channel, callCredentials)

    // Create a streaming aggregated quote request with SDK
    aggregated_quote_request(channel, callCredentials)
  }

  def market_update_request(channel: Channel, callCredentials: CallCredentials) = {
    val stub = StreamMarketUpdateServiceV1Grpc.blockingStub(channel).withCallCredentials(callCredentials)

    // Create a request with SDK
    // Globbing patterns are also supported on all fields. See http://sdk.kaiko.com/#instrument-selection for all supported patterns
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

  def trades_request(channel: Channel, callCredentials: CallCredentials) = {
    val stub = StreamTradesServiceV1Grpc.blockingStub(channel).withCallCredentials(callCredentials)

    // Create a request with SDK
    // Globbing patterns are also supported on all fields. See http://sdk.kaiko.com/#instrument-selection for all supported patterns
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

  def index_rate_request(channel: Channel, callCredentials: CallCredentials) = {
    val stub = StreamIndexServiceV1Grpc.blockingStub(channel).withCallCredentials(callCredentials)

    Try {
      // Create a request with SDK
      val request = StreamIndexServiceRequestV1(
        indexCode = "KK_PR_BTCUSD"
      )

      // Run the request and get results
      val results = stub.subscribe(request)
        .take(10)
        .toSeq

      println(results)
    }.recover(println(_))
  }

  def index_multi_asset_request(channel: Channel, callCredentials: CallCredentials) = {
    val stub = StreamIndexMultiAssetsServiceV1Grpc.blockingStub(channel).withCallCredentials(callCredentials)

    Try {
      // Create a request with SDK
      val request = StreamIndexMultiAssetsServiceRequestV1(
        indexCode = "KT15"
      )

      // Run the request and get results
      val results = stub.subscribe(request)
        .take(10)
        .toSeq

      println(results)
    }.recover(println(_))
  }

  def index_forex_rate_request(channel: Channel, callCredentials: CallCredentials) = {
    val stub = StreamIndexForexRateServiceV1Grpc.blockingStub(channel).withCallCredentials(callCredentials)

    Try {
      // Create a request with SDK
      val request = StreamIndexForexRateServiceRequestV1(
        indexCode = "KK_PR_BTCUSD_EUR"
      )

      // Run the request and get results
      val results = stub.subscribe(request)
        .take(10)
        .toSeq

      println(results)
    }.recover(println(_))
  }

  def aggregated_quote_request(channel: Channel, callCredentials: CallCredentials) = {
    val stub = StreamAggregatedQuoteServiceV2Grpc.blockingStub(channel).withCallCredentials(callCredentials)

    // Create a request with SDK
    // Globbing patterns are also supported on all fields. See http://sdk.kaiko.com/#instrument-selection for all supported patterns
    val request = StreamAggregatedQuoteRequestV2(
      instrumentClass = "spot",
      code = "btc-usd"
    )

    // Run the request and get results
    val results = stub.subscribe(request)
      .take(4)
      .toSeq

    println(results)
  }
}
