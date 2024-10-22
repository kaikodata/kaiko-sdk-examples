# Go SDK

- [how to use the various endpoints and their APIs](endpoints/main.go).
This should be "quickstart" point for any user new to the SDK.
- [how to handle end of stream / resubscription](resubscribe/main.go).
Disconnection can happen for lots of reasons (client or server side network, idle consumer for a very long time, etc.) and should be handled by resubscribing. Reconnection is already handled automatically by gRPC client library.

## Requirements

You will need a Golang 1.16+ installed on your machine.
Installation can be via <https://golang.org/doc/install> or through third-party tools like `brew` (<https://formulae.brew.sh/formula/go>).

## Run the example

- Run the example and get data from Kaiko API :

```bash
(cd examples/go/endpoints && go run main.go)
```

Note that for this particular step, you will need to setup an environment variable `KAIKO_API_KEY` with a valid Kaiko API key, otherwise you will get an error such as `PERMISSION_DENIED: not authorized`.

## Debugging connection gRPC / HTTP2 connection

If you have trouble connecting using Golang SDK, you can troubleshoot the issue using <https://github.com/grpc/grpc-go?tab=readme-ov-file#how-to-turn-on-logging>.

This typically involves setting env variables with `GRPC_GO_LOG_VERBOSITY_LEVEL=99 GRPC_GO_LOG_SEVERITY_LEVEL=info` (warning: it's very verbose!).

For example with the demo application use :

```bash
GODEBUG=http2debug=2 GRPC_GO_LOG_VERBOSITY_LEVEL=99 GRPC_GO_LOG_SEVERITY_LEVEL=info go run main.go
```

## Check for more recent versions

Check latest version of <https://github.com/kaikodata/kaiko-go-sdk>
