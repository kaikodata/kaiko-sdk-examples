package reconnect

import com.evanlennick.retry4j.CallExecutorBuilder
import com.evanlennick.retry4j.config.RetryConfigBuilder
import com.kaiko.sdk._
import com.kaiko.sdk.core.InstrumentCriteria
import com.kaiko.sdk.stream.trades_v1.StreamTradesRequestV1
import io.grpc._

import java.time.temporal.ChronoUnit
import java.util.concurrent.Executor
import scala.concurrent.ExecutionContext
import scala.util.Try

object Main {
  def main(args: Array[String]): Unit = {
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

    // Create a streaming trades request with SDK
    trades_request(channel, callCredentials)
  }

  def trades_request(channel: Channel, callCredentials: CallCredentials) = {
    val stub = StreamTradesServiceV1Grpc.blockingStub(channel).withCallCredentials(callCredentials)

    // Create a request with SDK
    // Globbing patterns are also supported on all fields. See http://sdk.kaiko.com/#instrument-selection for all supported patterns
    val request = StreamTradesRequestV1(
      instrumentCriteria = Some(InstrumentCriteria(
        exchange = "cbse",
        instrumentClass = "spot",
        code = "btc-usd"
      ))
    )

    // Run the request and get results
    subscribe(stub, request)
  }

  def subscribe(stub: StreamTradesServiceV1Grpc.StreamTradesServiceV1BlockingStub, request: StreamTradesRequestV1) = {
    val config = new RetryConfigBuilder()
      .retryOnSpecificExceptions(classOf[InterruptedException])
      .withMaxNumberOfTries(10)
      .withDelayBetweenTries(2, ChronoUnit.SECONDS)
      .withFixedBackoff.build

    Try {
      new CallExecutorBuilder()
        .config(config)
        .beforeNextTryListener((_: com.evanlennick.retry4j.Status[_]) => {
          println("[TRADES] Stream ended")
          println("[TRADES] Resubscribing")
        })
        .build()
        .execute(() => {
          val it = stub.subscribe(request)
          println("[TRADES] Stream started")

          it.foreach(System.out.println)

          throw new InterruptedException
        })
    }.recover(println(_))
  }
}
