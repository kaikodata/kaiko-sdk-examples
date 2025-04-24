package endpoints;

import com.google.protobuf.Duration;
import com.kaiko.sdk.*;
import com.kaiko.sdk.core.InstrumentCriteria;
import com.kaiko.sdk.core.Assets;
import com.kaiko.sdk.stream.aggregated_quote_v2.StreamAggregatedQuoteRequestV2;
import com.kaiko.sdk.stream.aggregated_quote_v2.StreamAggregatedQuoteResponseV2;
import com.kaiko.sdk.stream.aggregates_direct_exchange_rate_v2.StreamAggregatesDirectExchangeRateV2RequestV1;
import com.kaiko.sdk.stream.aggregates_direct_exchange_rate_v2.StreamAggregatesDirectExchangeRateV2ResponseV1;
import com.kaiko.sdk.stream.aggregates_spot_exchange_rate_v2.StreamAggregatesSpotExchangeRateV2RequestV1;
import com.kaiko.sdk.stream.aggregates_spot_exchange_rate_v2.StreamAggregatesSpotExchangeRateV2ResponseV1;
import com.kaiko.sdk.stream.aggregates_ohlcv_v1.StreamAggregatesOHLCVRequestV1;
import com.kaiko.sdk.stream.aggregates_ohlcv_v1.StreamAggregatesOHLCVResponseV1;
import com.kaiko.sdk.stream.aggregates_vwap_v1.StreamAggregatesVWAPRequestV1;
import com.kaiko.sdk.stream.aggregates_vwap_v1.StreamAggregatesVWAPResponseV1;
import com.kaiko.sdk.stream.derivatives_instrument_metrics_v1.StreamDerivativesInstrumentMetricsRequestV1;
import com.kaiko.sdk.stream.derivatives_instrument_metrics_v1.StreamDerivativesInstrumentMetricsResponseV1;
import com.kaiko.sdk.stream.index_v1.StreamIndexCommodity;
import com.kaiko.sdk.stream.index_v1.StreamIndexServiceRequestV1;
import com.kaiko.sdk.stream.index_v1.StreamIndexServiceResponseV1;
import com.kaiko.sdk.stream.iv_svi_parameters_v1.StreamIvSviParametersRequestV1;
import com.kaiko.sdk.stream.iv_svi_parameters_v1.StreamIvSviParametersResponseV1;
import com.kaiko.sdk.stream.index_multi_assets_v1.StreamIndexMultiAssetsServiceRequestV1;
import com.kaiko.sdk.stream.index_multi_assets_v1.StreamIndexMultiAssetsServiceResponseV1;
import com.kaiko.sdk.stream.index_forex_rate_v1.StreamIndexForexRateServiceRequestV1;
import com.kaiko.sdk.stream.index_forex_rate_v1.StreamIndexForexRateServiceResponseV1;
import com.kaiko.sdk.stream.market_update_v1.StreamMarketUpdateCommodity;
import com.kaiko.sdk.stream.market_update_v1.StreamMarketUpdateRequestV1;
import com.kaiko.sdk.stream.market_update_v1.StreamMarketUpdateResponseV1;
import com.kaiko.sdk.stream.trades_v1.StreamTradesRequestV1;
import com.kaiko.sdk.stream.trades_v1.StreamTradesResponseV1;
import com.kaiko.sdk.stream.exotic_indices_v1.StreamExoticIndicesServiceRequestV1;
import com.kaiko.sdk.stream.exotic_indices_v1.StreamExoticIndicesServiceResponseV1;
import com.kaiko.sdk.stream.constant_duration_indices_v1.StreamConstantDurationIndicesServiceRequestV1;
import com.kaiko.sdk.stream.constant_duration_indices_v1.StreamConstantDurationIndicesServiceResponseV1;

import io.grpc.*;

import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Main {
        public static void main(String[] args) {
                // Setup runtime
                ExecutorService ec = Executors.newSingleThreadExecutor();
                ManagedChannelBuilder builder = ManagedChannelBuilder.forAddress("gateway-v0-grpc.kaiko.ovh", 443)
                                .executor(ec);

                // Setup authentication
                Metadata.Key<String> AUTHORIZATION_METADATA_KEY = Metadata.Key.of("Authorization",
                                Metadata.ASCII_STRING_MARSHALLER);
                String apiKey = System.getenv().getOrDefault("KAIKO_API_KEY", "1234"); // Put your api key here
                Metadata headers = new Metadata();
                headers.put(AUTHORIZATION_METADATA_KEY, "Bearer " + apiKey);

                ManagedChannel channel = builder.build();
                CallCredentials callCredentials = new CallCredentials() {
                        @Override
                        public void applyRequestMetadata(RequestInfo requestInfo, Executor executor,
                                        MetadataApplier applier) {
                                executor.execute(() -> {
                                        try {
                                                applier.apply(headers);
                                        } catch (Throwable e) {
                                                applier.fail(Status.UNAUTHENTICATED.withCause(e));
                                        }
                                });
                        }
                };

                // Create a streaming trades request with SDK
                trades_request(channel, callCredentials);

                // Create a streaming ohlcv request with SDK
                ohlcv_request(channel, callCredentials);

                // Create a streaming vwap request with SDK
                vwap_request(channel, callCredentials);

                // Create a streaming market update request with SDK
                market_update_request(channel, callCredentials);

                // Create a streaming index rate request with SDK
                index_rate_request(channel, callCredentials);

                // Create a streaming index multi asset request with SDK
                index_multi_asset_request(channel, callCredentials);

                // Create a streaming index forex rate request with SDK
                index_forex_rate_request(channel, callCredentials);

                // Create a streaming aggregated quote request with SDK
                aggregated_quote_request(channel, callCredentials);

                // Create a streaming spot exchange rate request with SDK
                aggregates_spot_exchange_rate(channel, callCredentials);

                // Create a streaming direct exchange rate request with SDK
                aggregates_spot_direct_exchange_rate(channel, callCredentials);

                // Create a streaming iv svi parameters request with SDK
                iv_svi_parameters(channel, callCredentials);

                // Create a streaming derivatives instrument metrics request with SDK
                derivatives_instrument_metrics(channel, callCredentials);

                // Create a streaming exotic indices request with SDK
                exotic_indices_v1(channel, callCredentials);

                // Create a streaming constant duration indices request with SDK
                custom_duration_indices_v1(channel, callCredentials);
        }

        public static void ohlcv_request(ManagedChannel channel, CallCredentials callCredentials) {
                StreamAggregatesOHLCVRequestV1 request = StreamAggregatesOHLCVRequestV1.newBuilder()
                                .setInstrumentCriteria(
                                                InstrumentCriteria.newBuilder()
                                                                .setExchange("cbse")
                                                                .setInstrumentClass("spot")
                                                                .setCode("btc-usd")
                                                                .build())
                                .setAggregate("1s")
                                .build();

                StreamAggregatesOHLCVServiceV1Grpc.StreamAggregatesOHLCVServiceV1BlockingStub stub = StreamAggregatesOHLCVServiceV1Grpc
                                .newBlockingStub(channel).withCallCredentials(callCredentials);

                // Run the request and get results
                List<StreamAggregatesOHLCVResponseV1> elts = StreamSupport.stream(
                                Spliterators.spliteratorUnknownSize(
                                                stub.subscribe(request),
                                                Spliterator.ORDERED),
                                false)
                                .limit(10)
                                .collect(Collectors.toList());

                System.out.println(elts);
        }

        public static void vwap_request(ManagedChannel channel, CallCredentials callCredentials) {
                StreamAggregatesVWAPRequestV1 request = StreamAggregatesVWAPRequestV1.newBuilder()
                                .setInstrumentCriteria(
                                                InstrumentCriteria.newBuilder()
                                                                .setExchange("binc")
                                                                .setInstrumentClass("spot")
                                                                .setCode("btc-usdt")
                                                                .build())
                                .setAggregate("1s")
                                .build();

                StreamAggregatesVWAPServiceV1Grpc.StreamAggregatesVWAPServiceV1BlockingStub stub = StreamAggregatesVWAPServiceV1Grpc
                                .newBlockingStub(channel).withCallCredentials(callCredentials);

                // Run the request and get results
                List<StreamAggregatesVWAPResponseV1> elts = StreamSupport.stream(
                                Spliterators.spliteratorUnknownSize(
                                                stub.subscribe(request),
                                                Spliterator.ORDERED),
                                false)
                                .limit(10)
                                .collect(Collectors.toList());

                System.out.println(elts);
        }

        public static void market_update_request(ManagedChannel channel, CallCredentials callCredentials) {
                // Globbing patterns are also supported on all fields. See
                // http://sdk.kaiko.com/#instrument-selection for all supported patterns
                StreamMarketUpdateRequestV1 request = StreamMarketUpdateRequestV1.newBuilder()
                                .setInstrumentCriteria(
                                                InstrumentCriteria.newBuilder()
                                                                .setExchange("krkn")
                                                                .setInstrumentClass("spot")
                                                                .setCode("*")
                                                                .build())
                                .addCommodities(StreamMarketUpdateCommodity.SMUC_TRADE)
                                .build();

                StreamMarketUpdateServiceV1Grpc.StreamMarketUpdateServiceV1BlockingStub stub = StreamMarketUpdateServiceV1Grpc
                                .newBlockingStub(channel).withCallCredentials(callCredentials);

                // Run the request and get results
                List<StreamMarketUpdateResponseV1> elts = StreamSupport.stream(
                                Spliterators.spliteratorUnknownSize(
                                                stub.subscribe(request),
                                                Spliterator.ORDERED),
                                false)
                                .limit(10)
                                .collect(Collectors.toList());

                System.out.println(elts);
        }

        public static void trades_request(ManagedChannel channel, CallCredentials callCredentials) {
                // Globbing patterns are also supported on all fields. See
                // http://sdk.kaiko.com/#instrument-selection for all supported patterns
                StreamTradesRequestV1 request = StreamTradesRequestV1.newBuilder()
                                .setInstrumentCriteria(
                                                InstrumentCriteria.newBuilder()
                                                                .setExchange("cbse")
                                                                .setInstrumentClass("spot")
                                                                .setCode("btc-usd")
                                                                .build())
                                .build();

                StreamTradesServiceV1Grpc.StreamTradesServiceV1BlockingStub stub = StreamTradesServiceV1Grpc
                                .newBlockingStub(channel).withCallCredentials(callCredentials);

                // Run the request and get results
                List<StreamTradesResponseV1> elts = StreamSupport.stream(
                                Spliterators.spliteratorUnknownSize(
                                                stub.subscribe(request),
                                                Spliterator.ORDERED),
                                false)
                                .limit(10)
                                .collect(Collectors.toList());

                System.out.println(elts);
        }

        public static void index_rate_request(ManagedChannel channel, CallCredentials callCredentials) {
                StreamIndexServiceRequestV1 request = StreamIndexServiceRequestV1.newBuilder()
                                .setIndexCode("KK_BRR_BTCUSD")
                                .build();

                StreamIndexServiceV1Grpc.StreamIndexServiceV1BlockingStub stub = StreamIndexServiceV1Grpc
                                .newBlockingStub(channel).withCallCredentials(callCredentials);

                // Run the request and get results
                try {
                        List<StreamIndexServiceResponseV1> elts = StreamSupport.stream(
                                        Spliterators.spliteratorUnknownSize(
                                                        stub.subscribe(request),
                                                        Spliterator.ORDERED),
                                        false)
                                        .limit(10)
                                        .collect(Collectors.toList());

                        System.out.println(elts);
                } catch (Exception e) {
                        System.out.println(e.getMessage());
                }
        }

        public static void index_forex_rate_request(ManagedChannel channel, CallCredentials callCredentials) {
                StreamIndexForexRateServiceRequestV1 request = StreamIndexForexRateServiceRequestV1.newBuilder()
                                .setIndexCode("KK_BRR_BTCUSD_EUR")
                                .build();

                StreamIndexForexRateServiceV1Grpc.StreamIndexForexRateServiceV1BlockingStub stub = StreamIndexForexRateServiceV1Grpc
                                .newBlockingStub(channel).withCallCredentials(callCredentials);

                // Run the request and get results
                try {
                        List<StreamIndexForexRateServiceResponseV1> elts = StreamSupport.stream(
                                        Spliterators.spliteratorUnknownSize(
                                                        stub.subscribe(request),
                                                        Spliterator.ORDERED),
                                        false)
                                        .limit(10)
                                        .collect(Collectors.toList());

                        System.out.println(elts);
                } catch (Exception e) {
                        System.out.println(e.getMessage());
                }
        }

        public static void index_multi_asset_request(ManagedChannel channel, CallCredentials callCredentials) {
                StreamIndexMultiAssetsServiceRequestV1 request = StreamIndexMultiAssetsServiceRequestV1.newBuilder()
                                .setIndexCode("KT15")
                                .build();

                StreamIndexMultiAssetsServiceV1Grpc.StreamIndexMultiAssetsServiceV1BlockingStub stub = StreamIndexMultiAssetsServiceV1Grpc
                                .newBlockingStub(channel).withCallCredentials(callCredentials);

                // Run the request and get results
                try {
                        List<StreamIndexMultiAssetsServiceResponseV1> elts = StreamSupport.stream(
                                        Spliterators.spliteratorUnknownSize(
                                                        stub.subscribe(request),
                                                        Spliterator.ORDERED),
                                        false)
                                        .limit(10)
                                        .collect(Collectors.toList());

                        System.out.println(elts);
                } catch (Exception e) {
                        System.out.println(e.getMessage());
                }
        }

        public static void aggregated_quote_request(ManagedChannel channel, CallCredentials callCredentials) {
                // Globbing patterns are also supported on all fields. See
                // http://sdk.kaiko.com/#instrument-selection for all supported patterns
                StreamAggregatedQuoteRequestV2 request = StreamAggregatedQuoteRequestV2.newBuilder()
                                .setInstrumentClass("spot")
                                .setCode("btc-usd")
                                .build();

                StreamAggregatedQuoteServiceV2Grpc.StreamAggregatedQuoteServiceV2BlockingStub stub = StreamAggregatedQuoteServiceV2Grpc
                                .newBlockingStub(channel).withCallCredentials(callCredentials);

                // Run the request and get results
                List<StreamAggregatedQuoteResponseV2> elts = StreamSupport.stream(
                                Spliterators.spliteratorUnknownSize(
                                                stub.subscribe(request),
                                                Spliterator.ORDERED),
                                false)
                                .limit(10)
                                .collect(Collectors.toList());

                System.out.println(elts);
        }

        public static void aggregates_spot_exchange_rate(ManagedChannel channel, CallCredentials callCredentials) {
                StreamAggregatesSpotExchangeRateV2RequestV1 request = StreamAggregatesSpotExchangeRateV2RequestV1
                                .newBuilder()
                                .setAssets(Assets.newBuilder().setBase("btc").setQuote("usd").build())
                                .setWindow(Duration.newBuilder().setSeconds(10).build())
                                .setUpdateFrequency(Duration.newBuilder().setSeconds(2).build())
                                .build();

                StreamAggregatesSpotExchangeRateV2ServiceV1Grpc.StreamAggregatesSpotExchangeRateV2ServiceV1BlockingStub stub = StreamAggregatesSpotExchangeRateV2ServiceV1Grpc
                                .newBlockingStub(channel).withCallCredentials(callCredentials);

                // Run the request and get results
                List<StreamAggregatesSpotExchangeRateV2ResponseV1> elts = StreamSupport.stream(
                                Spliterators.spliteratorUnknownSize(
                                                stub.subscribe(request),
                                                Spliterator.ORDERED),
                                false)
                                .limit(10)
                                .collect(Collectors.toList());

                System.out.println(elts);
        }

        public static void aggregates_spot_direct_exchange_rate(ManagedChannel channel,
                        CallCredentials callCredentials) {
                StreamAggregatesDirectExchangeRateV2RequestV1 request = StreamAggregatesDirectExchangeRateV2RequestV1
                                .newBuilder()
                                .setAssets(Assets.newBuilder().setBase("btc").setQuote("usd").build())
                                .setWindow(Duration.newBuilder().setSeconds(10).build())
                                .setUpdateFrequency(Duration.newBuilder().setSeconds(2).build())
                                .build();

                StreamAggregatesSpotDirectExchangeRateV2ServiceV1Grpc.StreamAggregatesSpotDirectExchangeRateV2ServiceV1BlockingStub stub = StreamAggregatesSpotDirectExchangeRateV2ServiceV1Grpc
                                .newBlockingStub(channel).withCallCredentials(callCredentials);

                // Run the request and get results
                List<StreamAggregatesDirectExchangeRateV2ResponseV1> elts = StreamSupport.stream(
                                Spliterators.spliteratorUnknownSize(
                                                stub.subscribe(request),
                                                Spliterator.ORDERED),
                                false)
                                .limit(10)
                                .collect(Collectors.toList());

                System.out.println(elts);
        }

        public static void iv_svi_parameters(ManagedChannel channel,
                        CallCredentials callCredentials) {
                StreamIvSviParametersRequestV1 request = StreamIvSviParametersRequestV1
                                .newBuilder()
                                .setAssets(Assets.newBuilder().setBase("btc").setQuote("usd").build())
                                .setExchanges("drbt")
                                .build();

                StreamIvSviParametersServiceV1Grpc.StreamIvSviParametersServiceV1BlockingStub stub = StreamIvSviParametersServiceV1Grpc
                                .newBlockingStub(channel).withCallCredentials(callCredentials);

                // Run the request and get results
                List<StreamIvSviParametersResponseV1> elts = StreamSupport.stream(
                                Spliterators.spliteratorUnknownSize(
                                                stub.subscribe(request),
                                                Spliterator.ORDERED),
                                false)
                                .limit(10)
                                .collect(Collectors.toList());

                System.out.println(elts);
        }

        public static void derivatives_instrument_metrics(ManagedChannel channel,
                        CallCredentials callCredentials) {
                StreamDerivativesInstrumentMetricsRequestV1 request = StreamDerivativesInstrumentMetricsRequestV1
                                .newBuilder()
                                .setInstrumentCriteria(
                                                InstrumentCriteria.newBuilder()
                                                                .setExchange("*")
                                                                .setInstrumentClass("perpetual-future")
                                                                .setCode("btc-usd")
                                                                .build())
                                .build();

                StreamDerivativesInstrumentMetricsServiceV1Grpc.StreamDerivativesInstrumentMetricsServiceV1BlockingStub stub = StreamDerivativesInstrumentMetricsServiceV1Grpc
                                .newBlockingStub(channel).withCallCredentials(callCredentials);

                // Run the request and get results
                List<StreamDerivativesInstrumentMetricsResponseV1> elts = StreamSupport.stream(
                                Spliterators.spliteratorUnknownSize(
                                                stub.subscribe(request),
                                                Spliterator.ORDERED),
                                false)
                                .limit(10)
                                .collect(Collectors.toList());

                System.out.println(elts);
        }

        public static void exotic_indices_v1(ManagedChannel channel,
        CallCredentials callCredentials) {
                StreamExoticIndicesServiceRequestV1 request = StreamExoticIndicesServiceRequestV1
                        .newBuilder()
                        .setIndexCode("KT10TCUSD")
                        .addCommodities(StreamIndexCommodity.SIC_REAL_TIME)
                        .build();

                StreamExoticIndicesServiceV1Grpc.StreamExoticIndicesServiceV1BlockingStub stub = StreamExoticIndicesServiceV1Grpc
                        .newBlockingStub(channel).withCallCredentials(callCredentials);

                // Run the request and get results
                List<StreamExoticIndicesServiceResponseV1> elts = StreamSupport.stream(
                                Spliterators.spliteratorUnknownSize(
                                        stub.subscribe(request),
                                        Spliterator.ORDERED),
                                        false
                                )
                                .limit(10)
                                .collect(Collectors.toList());

                System.out.println(elts);
        }

        public static void custom_duration_indices_v1(ManagedChannel channel,
        CallCredentials callCredentials) {
                StreamConstantDurationIndicesServiceRequestV1 request = StreamConstantDurationIndicesServiceRequestV1
                        .newBuilder()
                        .setIndexCode("<YOUR_INDEX_CODE>")
                        .build();

                StreamConstantDurationIndicesServiceV1Grpc.StreamConstantDurationIndicesServiceV1BlockingStub stub = StreamConstantDurationIndicesServiceV1Grpc
                        .newBlockingStub(channel).withCallCredentials(callCredentials);

                // Run the request and get results
                List<StreamConstantDurationIndicesServiceResponseV1> elts = StreamSupport.stream(
                                Spliterators.spliteratorUnknownSize(
                                        stub.subscribe(request),
                                        Spliterator.ORDERED),
                                        false
                                )
                                .limit(10)
                                .collect(Collectors.toList());

                System.out.println(elts);
        }
}
