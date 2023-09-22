# Go SDK

- [how to use the various endpoints and their APIs](endpoints/main.go).
This should be "quickstart" point for any user new to the SDK.
- [how to handle end of stream / resubscription](resubscribe/main.go).
Disconnection can happen for lots of reasons (client or server side network, idle consumer for a very long time, etc.) and should be handled by resubscribing. Reconnection is already handled automatically by GRPC client library.

## Requirements

You will need a Golang 1.16+ installed on your machine.
Installation can be via <https://golang.org/doc/install> or through third-party tools like `brew` (<https://formulae.brew.sh/formula/go>).

## Run the example

- Run the example and get data from Kaiko API :

```bash
(cd examples/go/endpoints && go run main.go)
```

Note that for this particular step, you will need to setup an environment variable `KAIKO_API_KEY` with a valid Kaiko API key, otherwise you will get an error such as `PERMISSION_DENIED: not authorized`.

## Check for more recent versions

Check latest version of <https://github.com/kaikodata/kaiko-go-sdk>
