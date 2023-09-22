package test;

import com.kaiko.sdk.StreamAggregatesOHLCVServiceV1Grpc;
import com.kaiko.sdk.stream.aggregates_ohlcv_v1.StreamAggregatesOHLCVRequestV1;
import com.kaiko.sdk.stream.aggregates_ohlcv_v1.StreamAggregatesOHLCVResponseV1;
import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.mock;

@RunWith(JUnit4.class)
public class GrpcTest {
    /**
     * This rule manages automatic graceful shutdown for the registered servers and channels at the
     * end of test.
     */
    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    private final StreamAggregatesOHLCVServiceV1Grpc.StreamAggregatesOHLCVServiceV1ImplBase serviceImpl =
            mock(StreamAggregatesOHLCVServiceV1Grpc.StreamAggregatesOHLCVServiceV1ImplBase.class, delegatesTo(
                    new StreamAggregatesOHLCVServiceV1Grpc.StreamAggregatesOHLCVServiceV1ImplBase() {
                        // By default, the client will receive Status.UNIMPLEMENTED for all RPCs.
                        // You might need to implement necessary behaviors for your test here, like this:
                        @Override
                        public void subscribe(StreamAggregatesOHLCVRequestV1 request, StreamObserver<StreamAggregatesOHLCVResponseV1> responseObserver) {
                            responseObserver.onNext(
                                    StreamAggregatesOHLCVResponseV1.newBuilder()
                                            .setAggregate("1s")
                                            .setCode("btc-usd")
                                            .setExchange("cbse")
                                            .setHigh("42")
                                            .build()
                            );
                            responseObserver.onCompleted();
                        }

                    }));

    private StreamAggregatesOHLCVServiceV1Grpc.StreamAggregatesOHLCVServiceV1BlockingStub client;

    @Before
    public void setUp() throws Exception {
        // Generate a unique in-process server name.
        String serverName = InProcessServerBuilder.generateName();

        // Create a server, add service, start, and register for automatic graceful shutdown.
        grpcCleanup.register(InProcessServerBuilder
                .forName(serverName).directExecutor().addService(serviceImpl).build().start());

        // Create a client channel and register for automatic graceful shutdown.
        ManagedChannel channel = grpcCleanup.register(
                InProcessChannelBuilder.forName(serverName).directExecutor().build());

        // Create a HelloWorldClient using the in-process channel;
        client = StreamAggregatesOHLCVServiceV1Grpc.newBlockingStub(channel);
    }

    /**
     * To test the client, call from the client against the fake server, and verify behaviors or state
     * changes from the server side.
     */
    @Test
    public void grpcImpl_replyMessage() {
        Iterator<StreamAggregatesOHLCVResponseV1> reply = client.subscribe(StreamAggregatesOHLCVRequestV1.newBuilder().build());
        assertEquals("42", reply.next().getHigh());
    }
}

