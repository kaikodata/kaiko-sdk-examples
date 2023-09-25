# Rust SDK

- [how to use the various endpoints and their APIs](src/endpoints.rs).
This should be "quickstart" point for any user new to the SDK.
- [how to handle end of stream / resubscription](src/resubscribe.rs).
Disconnection can happen for lots of reasons (client or server side network, idle consumer for a very long time, etc.) and should be handled by resubscribing. Reconnection is already handled automatically by gRPC client library.

## Requirements

Rust stable toolchain (with cargo).

## Build the example (endpoints)

- Run :

```bash
cargo run --bin=endpoints
```

Note that for this particular step, you will need to setup an environment variable `KAIKO_API_KEY` with a valid Kaiko API key, otherwise you will get an error such as `PERMISSION_DENIED: not authorized`.

## Build the example (resubscribe)

- Run :

```bash
cargo run --bin=resubscribe
```

Note that for this particular step, you will need to setup an environment variable `KAIKO_API_KEY` with a valid Kaiko API key, otherwise you will get an error such as `PERMISSION_DENIED: not authorized`.
