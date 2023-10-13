# Rust SDK

- [how to use the various endpoints and their APIs](src/endpoints.rs).
This should be "quickstart" point for any user new to the SDK.
- [how to handle end of stream / resubscription](src/resubscribe.rs).
Disconnection can happen for lots of reasons (client or server side network, idle consumer for a very long time, etc.) and should be handled by resubscribing. Reconnection is already handled automatically by gRPC client library.

## Requirements

Rust stable toolchain (with cargo). The gRPC services are backed by Tonic. Follow the [getting-started guide](https://github.com/hyperium/tonic#getting-started) if you have any dependencies issues.

## Build the example

Note that for the steps below, you will need to setup an environment variable `KAIKO_API_KEY` with a valid Kaiko API key, otherwise you will get an error such as `PERMISSION_DENIED: not authorized`.

### Endpoints

- Run :

```bash
cargo run --bin=endpoints
```

### Indices

- Run :

```bash
cargo run --bin=indices
```

### Market Update

- Run :

```bash
cargo run --bin=market_update
```

### Vwap

- Run :

```bash
cargo run --bin=vwap
```

## Build the example (resubscribe)

- Run :

```bash
cargo run --bin=resubscribe
```

Note that for this particular step, you will need to setup an environment variable `KAIKO_API_KEY` with a valid Kaiko API key, otherwise you will get an error such as `PERMISSION_DENIED: not authorized`.
