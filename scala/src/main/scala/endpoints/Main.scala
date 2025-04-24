package endpoints

import scala.language.existentials
import com.kaiko.sdk.StreamAggregatedQuoteServiceV2Grpc
import com.kaiko.sdk.StreamAggregatesOHLCVServiceV1Grpc
import com.kaiko.sdk.StreamAggregatesVWAPServiceV1Grpc
import com.kaiko.sdk.StreamDerivativesInstrumentMetricsServiceV1Grpc
import com.kaiko.sdk.StreamIndexServiceV1Grpc
import com.kaiko.sdk.StreamIvSviParametersServiceV1Grpc
import com.kaiko.sdk.StreamExoticIndicesServiceV1Grpc
import com.kaiko.sdk.StreamConstantDurationIndicesServiceV1Grpc
import com.kaiko.sdk.StreamMarketUpdateServiceV1Grpc
import com.kaiko.sdk.StreamTradesServiceV1Grpc
import com.kaiko.sdk.core.Assets
import com.kaiko.sdk.core.InstrumentCriteria
import com.kaiko.sdk.stream.aggregated_quote_v2.StreamAggregatedQuoteRequestV2
import com.kaiko.sdk.stream.aggregates_spot_exchange_rate_v2.StreamAggregatesSpotExchangeRateV2RequestV1;
import com.kaiko.sdk.stream.aggregates_direct_exchange_rate_v2.StreamAggregatesDirectExchangeRateV2RequestV1;
import com.kaiko.sdk.stream.aggregates_ohlcv_v1.StreamAggregatesOHLCVRequestV1
import com.kaiko.sdk.stream.aggregates_vwap_v1.StreamAggregatesVWAPRequestV1
import com.kaiko.sdk.stream.market_update_v1.StreamMarketUpdateCommodity
import com.kaiko.sdk.stream.market_update_v1.StreamMarketUpdateRequestV1
import com.kaiko.sdk.stream.trades_v1.StreamTradesRequestV1
import com.kaiko.sdk.stream.index_v1.StreamIndexServiceRequestV1
import io.grpc.CallCredentials
import io.grpc.Channel
import io.grpc.ManagedChannelBuilder
import io.grpc.Metadata
import io.grpc.Status
import java.util.concurrent.Executor
import scala.concurrent.ExecutionContext
import scala.util.Try
import scalapb.json4s.JsonFormat
import com.kaiko.sdk.StreamIndexMultiAssetsServiceV1Grpc
import com.kaiko.sdk.stream.index_multi_assets_v1.StreamIndexMultiAssetsServiceRequestV1
import com.kaiko.sdk.StreamIndexForexRateServiceV1Grpc
import com.kaiko.sdk.stream.index_forex_rate_v1.StreamIndexForexRateServiceRequestV1
import com.kaiko.sdk.StreamAggregatesSpotExchangeRateV2ServiceV1Grpc.StreamAggregatesSpotExchangeRateV2ServiceV1
import com.kaiko.sdk.StreamAggregatesSpotExchangeRateV2ServiceV1Grpc
import com.kaiko.sdk.StreamAggregatesSpotDirectExchangeRateV2ServiceV1Grpc
import com.google.protobuf.duration.Duration
import com.kaiko.sdk.stream.derivatives_instrument_metrics_v1.StreamDerivativesInstrumentMetricsRequestV1
import com.kaiko.sdk.stream.iv_svi_parameters_v1.StreamIvSviParametersRequestV1
import com.kaiko.sdk.stream.exotic_indices_v1.StreamExoticIndicesServiceRequestV1
import com.kaiko.sdk.stream.constant_duration_indices_v1.StreamConstantDurationIndicesServiceRequestV1

object Main {

  def main(args: Array[String]): Unit = {
    implicit val ec = ExecutionContext.global

    // Setup runtime
    val builder =
      ManagedChannelBuilder.forAddress("gateway-v0-grpc.kaiko.ovh", 443)
    builder.executor(ec)

    // Setup authentication
    val AUTHORIZATION_METADATA_KEY =
      Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER)
    val apiKey =
      sys.env.getOrElse("KAIKO_API_KEY", "1234") // Put your api key here
    val headers = new Metadata()
    headers.put(AUTHORIZATION_METADATA_KEY, s"Bearer $apiKey")

    val channel = builder.build()
    val callCredentials = new CallCredentials {
      override def applyRequestMetadata(
          requestInfo: CallCredentials.RequestInfo,
          executor: Executor,
          applier: CallCredentials.MetadataApplier
      ): Unit = {
        Try {
          applier.apply(headers)
        }.recover { case e: Throwable =>
          applier.fail(Status.UNAUTHENTICATED.withCause(e))
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

    // Create a streaming spot exchange rate request with SDK
    aggregates_spot_exchange_rate(channel, callCredentials)

    // Create a streaming direct exchange rate request with SDK
    aggregates_spot_direct_exchange_rate(channel, callCredentials)

    // Create a streaming derivatives instrument metrics request with SDK
    derivatives_instrument_metrics(channel, callCredentials)

    // Create a streaming iv svi parameters request with SDK
    iv_svi_parameters(channel, callCredentials)

    // Create a streaming exotic indices request with SDK
    exotic_indices_v1(channel, callCredentials)
  }

  def market_update_request(
      channel: Channel,
      callCredentials: CallCredentials
  ) = {
    val stub = StreamMarketUpdateServiceV1Grpc
      .blockingStub(channel)
      .withCallCredentials(callCredentials)

    // Create a request with SDK
    // Globbing patterns are also supported on all fields. See http://sdk.kaiko.com/#instrument-selection for all supported patterns
    val request = StreamMarketUpdateRequestV1(
      instrumentCriteria = Some(
        InstrumentCriteria(
          exchange = "krkn",
          instrumentClass = "spot",
          code = "*"
        )
      ),
      commodities = List(StreamMarketUpdateCommodity.SMUC_TRADE)
    )

    // Run the request and get results
    val results = stub
      .subscribe(request)
      .take(10)
      .toSeq
      .map(JsonFormat.toJsonString)

    println(results)
  }

  def ohlcv_request(channel: Channel, callCredentials: CallCredentials) = {
    val stub = StreamAggregatesOHLCVServiceV1Grpc
      .blockingStub(channel)
      .withCallCredentials(callCredentials)

    // Create a request with SDK
    val request = StreamAggregatesOHLCVRequestV1(
      instrumentCriteria = Some(
        InstrumentCriteria(
          exchange = "cbse",
          instrumentClass = "spot",
          code = "btc-usd"
        )
      ),
      aggregate = "1s"
    )

    // Run the request and get results
    val results = stub
      .subscribe(request)
      .take(10)
      .toSeq
      .map(JsonFormat.toJsonString)

    println(results)
  }

  def vwap_request(channel: Channel, callCredentials: CallCredentials) = {
    val stub = StreamAggregatesVWAPServiceV1Grpc
      .blockingStub(channel)
      .withCallCredentials(callCredentials)

    // Create a request with SDK
    val request = StreamAggregatesVWAPRequestV1(
      instrumentCriteria = Some(
        InstrumentCriteria(
          exchange = "cbse",
          instrumentClass = "spot",
          code = "btc-usd"
        )
      ),
      aggregate = "1s"
    )

    // Run the request and get results
    val results = stub
      .subscribe(request)
      .take(10)
      .toSeq
      .map(JsonFormat.toJsonString)

    println(results)
  }

  def trades_request(channel: Channel, callCredentials: CallCredentials) = {
    val stub = StreamTradesServiceV1Grpc
      .blockingStub(channel)
      .withCallCredentials(callCredentials)

    // Create a request with SDK
    // Globbing patterns are also supported on all fields. See http://sdk.kaiko.com/#instrument-selection for all supported patterns
    val request = StreamTradesRequestV1(
      instrumentCriteria = Some(
        InstrumentCriteria(
          exchange = "cbse",
          instrumentClass = "spot",
          code = "btc-usd"
        )
      )
    )

    // Run the request and get results
    val results = stub
      .subscribe(request)
      .take(10)
      .toSeq
      .map(JsonFormat.toJsonString)

    println(results)
  }

  def index_rate_request(channel: Channel, callCredentials: CallCredentials) = {
    val stub = StreamIndexServiceV1Grpc
      .blockingStub(channel)
      .withCallCredentials(callCredentials)

    Try {
      // Create a request with SDK
      val request = StreamIndexServiceRequestV1(
        indexCode = "KK_BRR_BTCUSD"
      )

      // Run the request and get results
      val results = stub
        .subscribe(request)
        .take(10)
        .toSeq
        .map(JsonFormat.toJsonString)

      println(results)
    }.recover(println(_))
  }

  def index_multi_asset_request(
      channel: Channel,
      callCredentials: CallCredentials
  ) = {
    val stub = StreamIndexMultiAssetsServiceV1Grpc
      .blockingStub(channel)
      .withCallCredentials(callCredentials)

    Try {
      // Create a request with SDK
      val request = StreamIndexMultiAssetsServiceRequestV1(
        indexCode = "KT15"
      )

      // Run the request and get results
      val results = stub
        .subscribe(request)
        .take(10)
        .toSeq
        .map(JsonFormat.toJsonString)

      println(results)
    }.recover(println(_))
  }

  def index_forex_rate_request(
      channel: Channel,
      callCredentials: CallCredentials
  ) = {
    val stub = StreamIndexForexRateServiceV1Grpc
      .blockingStub(channel)
      .withCallCredentials(callCredentials)

    Try {
      // Create a request with SDK
      val request = StreamIndexForexRateServiceRequestV1(
        indexCode = "KK_BRR_BTCUSD_EUR"
      )

      // Run the request and get results
      val results = stub
        .subscribe(request)
        .take(10)
        .toSeq
        .map(JsonFormat.toJsonString)

      println(results)
    }.recover(println(_))
  }

  def aggregated_quote_request(
      channel: Channel,
      callCredentials: CallCredentials
  ) = {
    val stub = StreamAggregatedQuoteServiceV2Grpc
      .blockingStub(channel)
      .withCallCredentials(callCredentials)

    // Create a request with SDK
    // Globbing patterns are also supported on all fields. See http://sdk.kaiko.com/#instrument-selection for all supported patterns
    val request = StreamAggregatedQuoteRequestV2(
      instrumentClass = "spot",
      code = "btc-usd"
    )

    // Run the request and get results
    val results = stub
      .subscribe(request)
      .take(10)
      .toSeq
      .map(JsonFormat.toJsonString)

    println(results)
  }

  def aggregates_spot_exchange_rate(
      channel: Channel,
      callCredentials: CallCredentials
  ) = {
    val stub = StreamAggregatesSpotExchangeRateV2ServiceV1Grpc
      .blockingStub(channel)
      .withCallCredentials(callCredentials)

    // Create a request with SDK
    val request = StreamAggregatesSpotExchangeRateV2RequestV1(
      assets = Some(Assets(base = "btc", quote = "usd")),
      window = Some(Duration(seconds = 10)),
      updateFrequency = Some(Duration(seconds = 2))
    )

    // Run the request and get results
    val results = stub
      .subscribe(request)
      .take(10)
      .toSeq
      .map(JsonFormat.toJsonString)

    println(results)
  }

  def aggregates_spot_direct_exchange_rate(
      channel: Channel,
      callCredentials: CallCredentials
  ) = {
    val stub = StreamAggregatesSpotDirectExchangeRateV2ServiceV1Grpc
      .blockingStub(channel)
      .withCallCredentials(callCredentials)

    // Create a request with SDK
    val request = StreamAggregatesDirectExchangeRateV2RequestV1(
      assets = Some(Assets(base = "btc", quote = "usd")),
      window = Some(Duration(seconds = 10)),
      updateFrequency = Some(Duration(seconds = 2))
    )

    // Run the request and get results
    val results = stub
      .subscribe(request)
      .take(10)
      .toSeq
      .map(JsonFormat.toJsonString)

    println(results)
  }

  def derivatives_instrument_metrics(
      channel: Channel,
      callCredentials: CallCredentials
  ) = {
    val stub = StreamDerivativesInstrumentMetricsServiceV1Grpc
      .blockingStub(channel)
      .withCallCredentials(callCredentials)

    // Create a request with SDK
    val request = StreamDerivativesInstrumentMetricsRequestV1(
      instrumentCriteria = Some(
        InstrumentCriteria(
          exchange = "*",
          instrumentClass = "perpetual-future",
          code = "btc-usd"
        )
      )
    )

    // Run the request and get results
    val results = stub
      .subscribe(request)
      .take(10)
      .toSeq
      .map(JsonFormat.toJsonString)

    println(results)
  }

  def iv_svi_parameters(
      channel: Channel,
      callCredentials: CallCredentials
  ) = {
    val stub = StreamIvSviParametersServiceV1Grpc
      .blockingStub(channel)
      .withCallCredentials(callCredentials)

    // Create a request with SDK
    val request = StreamIvSviParametersRequestV1(
      assets = Some(Assets(base = "btc", quote = "usd")),
      exchanges = "drbt"
    )

    // Run the request and get results
    val results = stub
      .subscribe(request)
      .take(10)
      .toSeq
      .map(JsonFormat.toJsonString)

    println(results)
  }

  def exotic_indices_v1(
      channel: Channel,
      callCredentials: CallCredentials
  ) = {
    val stub = StreamExoticIndicesServiceV1Grpc
      .blockingStub(channel)
      .withCallCredentials(callCredentials)

    // Create a request with SDK
    val request = StreamExoticIndicesServiceRequestV1(
      indexCode = "KT10TCUSD",
    )

    // Run the request and get results
    val results = stub
      .subscribe(request)
      .take(10)
      .toSeq
      .map(JsonFormat.toJsonString)

    println(results)
  }

  def constant_duration_indices_v1(
      channel: Channel,
      callCredentials: CallCredentials
  ) = {
    val stub = StreamConstantDurationIndicesServiceV1Grpc
      .blockingStub(channel)
      .withCallCredentials(callCredentials)

    // Create a request with SDK
    val request = StreamConstantDurationIndicesServiceRequestV1(
      indexCode = "<YOUR_INDEX_CODE>",
    )

    // Run the request and get results
    val results = stub
      .subscribe(request)
      .take(10)
      .toSeq
      .map(JsonFormat.toJsonString)

    println(results)
  }
}
