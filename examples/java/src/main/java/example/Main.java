package example;

import com.kaiko.sdk.StreamAggregatesOHLCVServiceV1Grpc;
import com.kaiko.sdk.StreamAggregatesSpotExchangeRateServiceV1Grpc;
import com.kaiko.sdk.StreamAggregatesVWAPServiceV1Grpc;
import com.kaiko.sdk.StreamMarketUpdateServiceV1Grpc;
import com.kaiko.sdk.StreamTradesServiceV1Grpc;
import com.kaiko.sdk.core.InstrumentCriteria;
import com.kaiko.sdk.stream.aggregates_ohlcv_v1.StreamAggregatesOHLCVRequestV1;
import com.kaiko.sdk.stream.aggregates_ohlcv_v1.StreamAggregatesOHLCVResponseV1;
import com.kaiko.sdk.stream.aggregates_spot_exchange_rate_v1.StreamAggregatesSpotExchangeRateRequestV1;
import com.kaiko.sdk.stream.aggregates_spot_exchange_rate_v1.StreamAggregatesSpotExchangeRateResponseV1;
import com.kaiko.sdk.stream.aggregates_vwap_v1.StreamAggregatesVWAPRequestV1;
import com.kaiko.sdk.stream.aggregates_vwap_v1.StreamAggregatesVWAPResponseV1;
import com.kaiko.sdk.stream.market_update_v1.StreamMarketUpdateCommodity;
import com.kaiko.sdk.stream.market_update_v1.StreamMarketUpdateRequestV1;
import com.kaiko.sdk.stream.market_update_v1.StreamMarketUpdateResponseV1;
import com.kaiko.sdk.stream.trades_v1.StreamTradesRequestV1;
import com.kaiko.sdk.stream.trades_v1.StreamTradesResponseV1;
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
        Metadata.Key<String> AUTHORIZATION_METADATA_KEY = Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);
        String apiKey = System.getenv().getOrDefault("KAIKO_API_KEY", "1234"); // Put your api key here
        Metadata headers = new Metadata();
        headers.put(AUTHORIZATION_METADATA_KEY, "Bearer " + apiKey);

        ManagedChannel channel = builder.build();
        CallCredentials callCredentials = new CallCredentials() {
            @Override
            public void applyRequestMetadata(RequestInfo requestInfo, Executor executor, MetadataApplier applier) {
                executor.execute(() -> {
                    try {
                        applier.apply(headers);
                    } catch (Throwable e) {
                        applier.fail(Status.UNAUTHENTICATED.withCause(e));
                    }
                });
            }

            @Override
            public void thisUsesUnstableApi() {

            }
        };

        // Create a streaming trades request with SDK
        trades_request(channel, callCredentials);

        // Create a streaming ohlcv request with SDK
        ohlcv_request(channel, callCredentials);

        // Create a streaming vwap request with SDK
        vwap_request(channel, callCredentials);

        // Create a streaming spot exchange rate request with SDK
        spot_exchange_rate_request(channel, callCredentials);

        // Create a streaming market update request with SDK
        market_update_request(channel, callCredentials);
    }

    public static void ohlcv_request(ManagedChannel channel, CallCredentials callCredentials) {
        StreamAggregatesOHLCVRequestV1 request = StreamAggregatesOHLCVRequestV1.newBuilder()
                .setInstrumentCriteria(
                        InstrumentCriteria.newBuilder()
                                .setExchange("cbse")
                                .setInstrumentClass("spot")
                                .setCode("btc-usd")
                                .build()
                )
                .setAggregate("1s")
                .build();

        StreamAggregatesOHLCVServiceV1Grpc.StreamAggregatesOHLCVServiceV1BlockingStub stub = StreamAggregatesOHLCVServiceV1Grpc.newBlockingStub(channel).withCallCredentials(callCredentials);

        // Run the request and get results
        List<StreamAggregatesOHLCVResponseV1> elts = StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(
                        stub.subscribe(request),
                        Spliterator.ORDERED)
                , false)
                .limit(10)
                .collect(Collectors.toList());

        System.out.println(elts);
    }

    public static void vwap_request(ManagedChannel channel, CallCredentials callCredentials) {
        StreamAggregatesVWAPRequestV1 request = StreamAggregatesVWAPRequestV1.newBuilder()
                .setInstrumentCriteria(
                        InstrumentCriteria.newBuilder()
                                .setExchange("bnce")
                                .setInstrumentClass("spot")
                                .setCode("btc-usdt")
                                .build()
                )
                .setAggregate("1s")
                .build();

        StreamAggregatesVWAPServiceV1Grpc.StreamAggregatesVWAPServiceV1BlockingStub stub = StreamAggregatesVWAPServiceV1Grpc.newBlockingStub(channel).withCallCredentials(callCredentials);

        // Run the request and get results
        List<StreamAggregatesVWAPResponseV1> elts = StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(
                        stub.subscribe(request),
                        Spliterator.ORDERED)
                , false)
                .limit(10)
                .collect(Collectors.toList());

        System.out.println(elts);
    }

    public static void spot_exchange_rate_request(ManagedChannel channel, CallCredentials callCredentials) {
        StreamAggregatesSpotExchangeRateRequestV1 request = StreamAggregatesSpotExchangeRateRequestV1.newBuilder()
                .setCode("btc-usd")
                .setAggregate("1s")
                .build();

        StreamAggregatesSpotExchangeRateServiceV1Grpc.StreamAggregatesSpotExchangeRateServiceV1BlockingStub stub = StreamAggregatesSpotExchangeRateServiceV1Grpc.newBlockingStub(channel).withCallCredentials(callCredentials);

        // Run the request and get results
        List<StreamAggregatesSpotExchangeRateResponseV1> elts = StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(
                        stub.subscribe(request),
                        Spliterator.ORDERED)
                , false)
                .limit(10)
                .collect(Collectors.toList());

        System.out.println(elts);
    }

    public static void market_update_request(ManagedChannel channel, CallCredentials callCredentials) {
        StreamMarketUpdateRequestV1 request = StreamMarketUpdateRequestV1.newBuilder()
                .setInstrumentCriteria(
                        InstrumentCriteria.newBuilder()
                                .setExchange("krkn")
                                .setInstrumentClass("spot")
                                .setCode("*")
                                .build()
                )
		.addCommodities(StreamMarketUpdateCommodity.SMUC_TRADE)
                .build();

        StreamMarketUpdateServiceV1Grpc.StreamMarketUpdateServiceV1BlockingStub stub = StreamMarketUpdateServiceV1Grpc.newBlockingStub(channel).withCallCredentials(callCredentials);

        // Run the request and get results
        List<StreamMarketUpdateResponseV1> elts = StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(
                        stub.subscribe(request),
                        Spliterator.ORDERED)
                , false)
                .limit(10)
                .collect(Collectors.toList());

        System.out.println(elts);
    }

    public static void trades_request(ManagedChannel channel, CallCredentials callCredentials) {
        StreamTradesRequestV1 request = StreamTradesRequestV1.newBuilder()
                .setInstrumentCriteria(
                        InstrumentCriteria.newBuilder()
                                .setExchange("cbse")
                                .setInstrumentClass("spot")
                                .setCode("btc-usd")
                                .build()
                )
                .build();

        StreamTradesServiceV1Grpc.StreamTradesServiceV1BlockingStub stub = StreamTradesServiceV1Grpc.newBlockingStub(channel).withCallCredentials(callCredentials);

        // Run the request and get results
        List<StreamTradesResponseV1> elts = StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(
                        stub.subscribe(request),
                        Spliterator.ORDERED)
                , false)
                .limit(10)
                .collect(Collectors.toList());

        System.out.println(elts);
    }
}
