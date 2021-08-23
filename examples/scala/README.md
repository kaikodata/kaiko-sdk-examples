# Scala SDK

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
