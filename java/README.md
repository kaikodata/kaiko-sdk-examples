# Java SDK

- [how to use the various endpoints and their APIs](src/main/java/endpoints/Main.java).
This should be "quickstart" point for any user new to the SDK.
- [how to handle end of stream / resubscription](src/main/java/resubscribe/Main.java).
Disconnection can happen for lots of reasons (client or server side network, idle consumer for a very long time, etc.) and should be handled by resubscribing. Reconnection is already handled automatically by gRPC client library.

## Requirements

You will need a Java 1.8+ JDK installed on your machine.
Installation can be done through third-party tools like SDKMAN (<https://sdkman.io/>).

## Build the example

- Build :

```bash
./gradlew build
```

- Test :

```bash
./gradlew test
```

- Run the example and get data from Kaiko API:

```bash
./gradlew run
```

Note that for this particular step, you will need to setup an environment variable `KAIKO_API_KEY` with a valid Kaiko API key, otherwise you will get an error such as `PERMISSION_DENIED: not authorized`.

## Examples

- Run the example showcasing resubscribe: `./gradlew run runResubscribe`
- Run the example showcasing iterator API: `./gradlew run runIterator`
- Run the example showcasing observer API: `./gradlew run runObserver`

## Check for more recent versions

```bash
./gradlew dependencyUpdates
```

## Closing an existing subscription

Closing a subscription is done through a `CancellableContext` and `context.cancel(...)`, which should be used accordingly with your `executor`.

Example can be found at <https://github.com/grpc/grpc-java/blob/master/examples/src/main/java/io/grpc/examples/cancellation/CancellationClient.java#L66>.

## Proxy support

Proxy is supported through standard Java properties.

See <https://docs.oracle.com/javase/8/docs/technotes/guides/net/proxies.html> and <https://github.com/grpc/grpc-java/issues/9057> for more information.
