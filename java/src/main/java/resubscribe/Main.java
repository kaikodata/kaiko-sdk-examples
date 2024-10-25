package resubscribe;

import com.evanlennick.retry4j.CallExecutorBuilder;
import com.evanlennick.retry4j.config.RetryConfig;
import com.evanlennick.retry4j.config.RetryConfigBuilder;
import com.evanlennick.retry4j.exception.RetriesExhaustedException;
import com.evanlennick.retry4j.exception.UnexpectedException;
import com.kaiko.sdk.*;
import com.kaiko.sdk.core.InstrumentCriteria;
import com.kaiko.sdk.stream.trades_v1.StreamTradesRequestV1;
import com.kaiko.sdk.stream.trades_v1.StreamTradesResponseV1;
import io.grpc.*;

import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {
        // Setup runtime
        ExecutorService ec = Executors.newSingleThreadExecutor();
        ManagedChannelBuilder builder = ManagedChannelBuilder.forAddress("gateway-v0-grpc.kaiko.ovh", 443)
                .executor(ec);

        // Setup authentication
        Metadata.Key<String> AUTHORIZATION_METADATA_KEY = Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);
        String apiKey = System.getenv().getOrDefault("KAIKO_API_KEY", "1234"); // Put your api key here
        System.out.println(apiKey);
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
        };

        // Create a streaming trades request with SDK
        trades_request(channel, callCredentials);
    }

    public static void trades_request(ManagedChannel channel, CallCredentials callCredentials) {
        // Globbing patterns are also supported on all fields. See http://sdk.kaiko.com/#instrument-selection for all supported patterns
        StreamTradesRequestV1 request = StreamTradesRequestV1.newBuilder()
                .setInstrumentCriteria(
                        InstrumentCriteria.newBuilder()
                                .setExchange("cbse")
                                .setInstrumentClass("spot")
                                .setCode("*")
                                .build()
                )
                .build();

        StreamTradesServiceV1Grpc.StreamTradesServiceV1BlockingStub stub = StreamTradesServiceV1Grpc.newBlockingStub(channel).withCallCredentials(callCredentials);

        subscribe(stub, request);
    }

    public static void subscribe(StreamTradesServiceV1Grpc.StreamTradesServiceV1BlockingStub stub, StreamTradesRequestV1 request) {
        RetryConfig config = new RetryConfigBuilder()
                .retryOnSpecificExceptions(InterruptedException.class)
                .withMaxNumberOfTries(10)
                .withDelayBetweenTries(2, ChronoUnit.SECONDS)
                .withFixedBackoff()
                .build();

        try {
            new CallExecutorBuilder<Void>()
                    .config(config)
                    .beforeNextTryListener(s -> {
                        System.out.println("[TRADES] Stream ended");
                        System.out.println("[TRADES] Resubscribing");
                    })
                    .build()
                    .execute(() -> {
                        Iterator<StreamTradesResponseV1> it = stub.subscribe(request);

                        System.out.println("[TRADES] Stream started");
                        it.forEachRemaining(System.out::println);

                        throw new InterruptedException();
                    });
        } catch (RetriesExhaustedException ree) {
            //the call exhausted all tries without succeeding
            throw ree;
        } catch (UnexpectedException ue) {
            //the call threw an unexpected exception
            throw ue;
        }
    }
}
