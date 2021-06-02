package example;

import com.kaiko.sdk.StreamAggregatesOHLCVServiceV1Grpc;
import com.kaiko.sdk.StreamTradesServiceV1Grpc;
import com.kaiko.sdk.core.InstrumentCriteria;
import com.kaiko.sdk.stream.aggregates_ohlcv_v1.StreamAggregatesOHLCVRequestV1;
import com.kaiko.sdk.stream.aggregates_ohlcv_v1.StreamAggregatesOHLCVResponseV1;
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
