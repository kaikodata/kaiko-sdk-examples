package endpoints;

import com.kaiko.sdk.*;

import com.kaiko.sdk.core.InstrumentCriteria;
import com.kaiko.sdk.stream.aggregated_quote_v2.StreamAggregatedQuoteRequestV2;
import com.kaiko.sdk.stream.aggregated_quote_v2.StreamAggregatedQuoteResponseV2;
import com.kaiko.sdk.stream.aggregates_ohlcv_v1.StreamAggregatesOHLCVRequestV1;
import com.kaiko.sdk.stream.aggregates_ohlcv_v1.StreamAggregatesOHLCVResponseV1;
import com.kaiko.sdk.stream.aggregates_vwap_v1.StreamAggregatesVWAPRequestV1;
import com.kaiko.sdk.stream.aggregates_vwap_v1.StreamAggregatesVWAPResponseV1;
import com.kaiko.sdk.stream.market_update_v1.StreamMarketUpdateCommodity;
import com.kaiko.sdk.stream.market_update_v1.StreamMarketUpdateRequestV1;
import com.kaiko.sdk.stream.market_update_v1.StreamMarketUpdateResponseV1;
import com.kaiko.sdk.stream.trades_v1.StreamTradesRequestV1;
import com.kaiko.sdk.stream.trades_v1.StreamTradesResponseV1;
import com.kaiko.sdk.stream.index_v1.StreamIndexServiceRequestV1;
import com.kaiko.sdk.stream.index_v1.StreamIndexServiceResponseV1;
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

        // Create a streaming market update request with SDK
        market_update_request(channel, callCredentials);

        // Create a streaming index request with SDK
        index_request(channel, callCredentials);

        // Create a streaming aggregated quote request with SDK
        aggregated_quote_request(channel, callCredentials);
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

    public static void market_update_request(ManagedChannel channel, CallCredentials callCredentials) {
        // Globbing patterns are also supported on all fields. See http://sdk.kaiko.com/#instrument-selection for all supported patterns
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
        // Globbing patterns are also supported on all fields. See http://sdk.kaiko.com/#instrument-selection for all supported patterns
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

    public static void index_request(ManagedChannel channel, CallCredentials callCredentials) {
        StreamIndexServiceRequestV1 request = StreamIndexServiceRequestV1.newBuilder()
                .setIndexCode("indexCode") // fill it with actual value
                .build();

        StreamIndexServiceV1Grpc.StreamIndexServiceV1BlockingStub stub = StreamIndexServiceV1Grpc.newBlockingStub(channel).withCallCredentials(callCredentials);

        // Run the request and get results
        try {
            List<StreamIndexServiceResponseV1> elts = StreamSupport.stream(
                            Spliterators.spliteratorUnknownSize(
                                    stub.subscribe(request),
                                    Spliterator.ORDERED)
                            , false)
                    .limit(10)
                    .collect(Collectors.toList());

            System.out.println(elts);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void aggregated_quote_request(ManagedChannel channel, CallCredentials callCredentials) {
        // Globbing patterns are also supported on all fields. See http://sdk.kaiko.com/#instrument-selection for all supported patterns
        StreamAggregatedQuoteRequestV2 request = StreamAggregatedQuoteRequestV2.newBuilder()
                .setInstrumentClass("spot")
                .setCode("btc-usd")
                .build();

        StreamAggregatedQuoteServiceV2Grpc.StreamAggregatedQuoteServiceV2BlockingStub stub = StreamAggregatedQuoteServiceV2Grpc.newBlockingStub(channel).withCallCredentials(callCredentials);

        // Run the request and get results
        List<StreamAggregatedQuoteResponseV2> elts = StreamSupport.stream(
                        Spliterators.spliteratorUnknownSize(
                                stub.subscribe(request),
                                Spliterator.ORDERED)
                        , false)
                .limit(10)
                .collect(Collectors.toList());

        System.out.println(elts);
    }
}
