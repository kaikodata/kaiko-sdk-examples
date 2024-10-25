package iterator;

import com.kaiko.sdk.*;

import com.kaiko.sdk.core.InstrumentCriteria;
import com.kaiko.sdk.stream.market_update_v1.StreamMarketUpdateCommodity;
import com.kaiko.sdk.stream.market_update_v1.StreamMarketUpdateRequestV1;
import com.kaiko.sdk.stream.market_update_v1.StreamMarketUpdateResponseV1;
import io.grpc.*;

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

                // Create a streaming market update request with SDK
                market_update_request(channel, callCredentials);
        }

        public static void market_update_request(ManagedChannel channel, CallCredentials callCredentials) {
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

                StreamMarketUpdateServiceV1Grpc.StreamMarketUpdateServiceV1BlockingStub stub = StreamMarketUpdateServiceV1Grpc
                                .newBlockingStub(channel).withCallCredentials(callCredentials);

                Iterator<StreamMarketUpdateResponseV1> sub = stub.subscribe(request);
                try {
                        while (sub.hasNext()) {
                                StreamMarketUpdateResponseV1 elt = sub.next();
                                System.out.println(elt);
                        }
                } catch (StatusRuntimeException e) {
                        System.out.println(e);
                }
        }
}
