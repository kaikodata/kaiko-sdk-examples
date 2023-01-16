# CSharp SDK

- [how to use the various endpoints and their APIs](Program.cs).
This should be "quickstart" point for any user new to the SDK.
- [how to handle end of stream / resubscription](Resubscribe.cs).
Disconnection can happen for lots of reasons (client or server side network, idle consumer for a very long time, etc.) and should be handled by resubscribing. Reconnection is already handled automatically by GRPC client library.

## Requirements

You will need .Net Core 6 or later installed on your machine.
Installation can be done through official website <https://docs.microsoft.com/en-us/dotnet/core/install/>.

## Build the example

- Build :

```bash
dotnet build
```

- Run the example and get data from Kaiko API:

```bash
dotnet run
```

Note that for this particular step, you will need to setup an environment variable `KAIKO_API_KEY` with a valid Kaiko API key, otherwise you will get an error such as `PERMISSION_DENIED: not authorized`.

## Fix potential SSL issues

If you're having GRPC errors such as `GPRC ERROR 14 - Unavailable` or `OPENSSL_internal:CERTIFICATE_VERIFY_FAILED`, check your machine certificates, and particulary that you have Let's Encrypt root certificate (ISRG Root X1).
Most of the GRPC bindings come with bundled root certificates which do not always reflect actual world since they can be outdated.

One known workaround is to point to your own root certificate, filling `GRPC_DEFAULT_SSL_ROOTS_FILE_PATH`.

For example:

```bash
GRPC_DEFAULT_SSL_ROOTS_FILE_PATH=/etc/ssl/certs/ca-certificates.crt dotnet run
```

## Check for more recent versions

```bash
dotnet list package --outdated
```
