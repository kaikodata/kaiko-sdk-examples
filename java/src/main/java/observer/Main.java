package observer;

import com.kaiko.sdk.StreamMarketUpdateServiceV1Grpc;
import com.kaiko.sdk.StreamMarketUpdateServiceV1Grpc.StreamMarketUpdateServiceV1Stub;
import com.kaiko.sdk.core.InstrumentCriteria;
import com.kaiko.sdk.stream.market_update_v1.StreamMarketUpdateCommodity;
import com.kaiko.sdk.stream.market_update_v1.StreamMarketUpdateRequestV1;
import com.kaiko.sdk.stream.market_update_v1.StreamMarketUpdateResponseV1;
import io.grpc.*;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.StreamObserver;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) throws InterruptedException {
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

        StreamObserver<StreamMarketUpdateResponseV1> observer = new StreamObserver<>() {
            @Override
            public void onNext(StreamMarketUpdateResponseV1 value) {
                System.out.println(value);
            }

            @Override
            public void onError(Throwable t) {
                System.out.println(t);
            }

            @Override
            public void onCompleted() {
                System.out.println("completed");
            }
        };

        try {
            // Create a streaming market update request with SDK
            market_update_request(channel, callCredentials, observer);

            while (true) {
                boolean done = ec.awaitTermination(1, TimeUnit.SECONDS);
                if (done) {
                    break;
                }
            }
        } finally {
            channel.shutdownNow().awaitTermination(1, TimeUnit.SECONDS);
        }
    }

    public static void market_update_request(ManagedChannel channel, CallCredentials callCredentials, StreamObserver<StreamMarketUpdateResponseV1> observer) {
        // StreamMarketUpdateRequestV1.newBuilder().
        // Globbing patterns are also supported on all fields. See
        // http://sdk.kaiko.com/#instrument-selection for all supported patterns
        StreamMarketUpdateRequestV1 request = StreamMarketUpdateRequestV1.newBuilder()
                .setInstrumentCriteria(
                        InstrumentCriteria.newBuilder()
                                .setExchange("binc")
                                .setInstrumentClass("spot")
                                .setCode("*")
                                .build())
                .addCommodities(StreamMarketUpdateCommodity.SMUC_TOP_OF_BOOK)
                .build();

        StreamMarketUpdateServiceV1Stub stub1 = StreamMarketUpdateServiceV1Grpc
                .newStub(channel)
                .withCallCredentials(callCredentials);

        stub1.subscribe(request, observer);
    }
}
