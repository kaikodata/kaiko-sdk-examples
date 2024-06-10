import com.kaiko.sdk.StreamAggregatesOHLCVServiceV1Grpc
import com.kaiko.sdk.StreamAggregatesOHLCVServiceV1Grpc.StreamAggregatesOHLCVServiceV1
import com.kaiko.sdk.stream.aggregates_ohlcv_v1.{
  StreamAggregatesOHLCVRequestV1,
  StreamAggregatesOHLCVResponseV1
}
import io.grpc.inprocess.{InProcessChannelBuilder, InProcessServerBuilder}
import io.grpc.stub.StreamObserver
import org.junit.Assert.assertEquals
import org.scalatest.BeforeAndAfter
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContext

class GrpcSpec extends AnyFlatSpec with Matchers with BeforeAndAfter {

  final private val serviceImpl = new StreamAggregatesOHLCVServiceV1 {
    // By default the client will receive Status.UNIMPLEMENTED for all RPCs.
    // You might need to implement necessary behaviors for your test here, like this:
    def subscribe(
        request: StreamAggregatesOHLCVRequestV1,
        responseObserver: StreamObserver[StreamAggregatesOHLCVResponseV1]
    ): Unit = {
      responseObserver.onNext(
        StreamAggregatesOHLCVResponseV1(
          aggregate = "1s",
          code = "btc-usd",
          exchange = "cbse",
          high = "42"
        )
      )
      responseObserver.onCompleted()
    }
  }

  private var client: Option[
    StreamAggregatesOHLCVServiceV1Grpc.StreamAggregatesOHLCVServiceV1BlockingStub
  ] = None

  before {
    val serverName = InProcessServerBuilder.generateName
    InProcessServerBuilder
      .forName(serverName)
      .directExecutor()
      .addService(
        StreamAggregatesOHLCVServiceV1Grpc
          .bindService(serviceImpl, ExecutionContext.global)
      )
      .build
      .start
    val channel =
      InProcessChannelBuilder.forName(serverName).directExecutor.build
    client = Some(StreamAggregatesOHLCVServiceV1Grpc.blockingStub(channel))
  }

  "grcpc call" should "return appropriate values" in {
    val reply = client.map(c => c.subscribe(StreamAggregatesOHLCVRequestV1()))
    assertEquals("42", reply.get.map(_.high).next())
  }

}
