package endpoints;

import com.google.protobuf.Timestamp;
import com.kaiko.sdk.StreamAggregatesOHLCVServiceV1Grpc;
import com.kaiko.sdk.StreamAggregatesDirectExchangeRateServiceV1Grpc;
import com.kaiko.sdk.StreamAggregatesSpotExchangeRateServiceV1Grpc;
import com.kaiko.sdk.StreamAggregatesVWAPServiceV1Grpc;
import com.kaiko.sdk.StreamMarketUpdateServiceV1Grpc;
import com.kaiko.sdk.StreamTradesServiceV1Grpc;
import com.kaiko.sdk.StreamIndexServiceV1Grpc;
import com.kaiko.sdk.StreamAggregatedPriceServiceV1Grpc;

import com.kaiko.sdk.core.DataInterval;
import com.kaiko.sdk.core.InstrumentCriteria;
import com.kaiko.sdk.stream.aggregated_price_v1.StreamAggregatedPriceRequestV1;
import com.kaiko.sdk.stream.aggregated_price_v1.StreamAggregatedPriceResponseV1;
import com.kaiko.sdk.stream.aggregates_ohlcv_v1.StreamAggregatesOHLCVRequestV1;
import com.kaiko.sdk.stream.aggregates_ohlcv_v1.StreamAggregatesOHLCVResponseV1;
import com.kaiko.sdk.stream.aggregates_direct_exchange_rate_v1.StreamAggregatesDirectExchangeRateRequestV1;
import com.kaiko.sdk.stream.aggregates_direct_exchange_rate_v1.StreamAggregatesDirectExchangeRateResponseV1;
import com.kaiko.sdk.stream.aggregates_spot_exchange_rate_v1.StreamAggregatesSpotExchangeRateRequestV1;
import com.kaiko.sdk.stream.aggregates_spot_exchange_rate_v1.StreamAggregatesSpotExchangeRateResponseV1;
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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
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

        // Create a streaming market update request with SDK
        market_update_request(channel, callCredentials);
    }

    public static void market_update_request(ManagedChannel channel, CallCredentials callCredentials) {
        Instant end = Instant.now();
        Instant start = end.minus(1, ChronoUnit.MINUTES);

        DataInterval interval = DataInterval.newBuilder()
                .setStartTime(Timestamp
                        .newBuilder()
                        .setSeconds(start.getEpochSecond())
                        .setNanos(start.getNano())
                        .build())
                .setEndTime(Timestamp
                        .newBuilder()
                        .setSeconds(end.getEpochSecond())
                        .setNanos(end.getNano())
                        .build()
                ).build();

        // Globbing patterns are also supported on all fields. See http://sdk.kaiko.com/#instrument-selection for all supported patterns
        StreamMarketUpdateRequestV1 request = StreamMarketUpdateRequestV1.newBuilder()
                .setInstrumentCriteria(
                        InstrumentCriteria.newBuilder()
                                .setExchange("*")
                                .setInstrumentClass("spot")
                                .setCode("*")
                                .build()
                )
                .setInterval(interval)
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
}
