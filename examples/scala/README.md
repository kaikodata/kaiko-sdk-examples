# Scala SDK

- [how to use the various endpoints and their APIs](src/main/scala/endpoints/Main.scala).
This should be "quickstart" point for any user new to the SDK.
- [how to handle end of stream / resubscription](src/main/scala/resubscribe/Main.scala).
Disconnection can happen for lots of reasons (client or server side network, idle consumer for a very long time, etc.) and should be handled by resubscribing. Reconnection is already handled automatically by GRPC client library.

## Requirements

You will need a Java 1.8+ JDK and Scala 2.12 or 2.13 installed on your machine.
Installation can be done through third-party tools like SDKMAN (<https://sdkman.io/>).

## Build the example

- Build :

```bash
./sbtx compile
```

- Test :

```bash
./sbtx test
```

- Run :

```bash
./sbtx run
```

Note that for this particular step, you will need to setup an environment variable `KAIKO_API_KEY` with a valid Kaiko API key, otherwise you will get an error such as `PERMISSION_DENIED: not authorized`.

## Check for more recent versions

```bash
./sbtx dependencyUpdates
```
