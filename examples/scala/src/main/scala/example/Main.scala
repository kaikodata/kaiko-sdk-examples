package example

import com.kaiko.sdk.StreamAggregatesOHLCVServiceV1Grpc
import com.kaiko.sdk.core.InstrumentCriteria
import com.kaiko.sdk.stream.aggregates_ohlcv_v1.StreamAggregatesOHLCVRequestV1
import io.grpc.{CallCredentials, ManagedChannelBuilder, Metadata, Status}

import java.util.concurrent.Executor
import scala.concurrent.ExecutionContext
import scala.util.Try

object Main {
  def main(args: Array[String]) = {
    implicit val ec = ExecutionContext.global

    // Setup runtime
    val builder = ManagedChannelBuilder.forAddress("gateway-v0-grpc.kaiko.ovh", 443)
    builder.executor(ec)

    // Setup authentication
    val AUTHORIZATION_METADATA_KEY = Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER)
    val apiKey = sys.env.getOrElse("KAIKO_API_KEY", "1234") // Put your api key here
    val headers = new Metadata()
    headers.put(AUTHORIZATION_METADATA_KEY, s"Bearer $apiKey")

    val channel = builder.build()
    val callCredentials = new CallCredentials {
      override def applyRequestMetadata(requestInfo: CallCredentials.RequestInfo, executor: Executor, applier: CallCredentials.MetadataApplier): Unit = {
        Try {
          applier.apply(headers)
        }.recover {
          case e: Throwable => applier.fail(Status.UNAUTHENTICATED.withCause(e))
        }
      }

      override def thisUsesUnstableApi(): Unit = {}
    }
    val stub = StreamAggregatesOHLCVServiceV1Grpc.blockingStub(channel).withCallCredentials(callCredentials)

    // Create a request with SDK
    val request = StreamAggregatesOHLCVRequestV1(
      instrumentCriteria = Some(InstrumentCriteria(
        exchange = "cbse",
        instrumentClass = "spot",
        code = "btc-usd"
      )),
      aggregate = "1s",
    )

    // Run the request and get results
    val results = stub.subscribe(request)
      .take(10)
      .toSeq

    println(results)
  }
}
