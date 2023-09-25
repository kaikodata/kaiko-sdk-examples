# C++ SDK

This repository contains 2 kinds of example:

- [how to use the various endpoints and their APIs](app.cpp).
This should be "quickstart" point for any user new to the SDK.
- [how to handle end of stream / resubscription](resubscribe.cpp).
Disconnection can happen for lots of reasons (client or server side network, idle consumer for a very long time, etc.) and should be handled by resubscribing. Reconnection is already handled automatically by gRPC client library.

## Build the example using CMake

This is the most common tool used for building C++ projects.

### Requirements (CMake)

You will need `cmake` and a C++11 compatible compiler installed on your machine.

### Build (CMake)

- Build :

```bash
CMAKE_BUILD_PARALLEL_LEVEL=8 ./build_cmake.sh
```

- Run the example and get data from Kaiko API:

```bash
./build_cmake/app
```

Note that for this particular step, you will need to setup an environment variable `KAIKO_API_KEY` with a valid Kaiko API key, otherwise you will get an error such as `PERMISSION_DENIED: not authorized`.

### WARNING on Windows C++ implementation of the gRPC client

- When using Microsoft Visual C++ (MSVC) compiler, you'll need to add the following parameter **-DCMAKE_MSVC_RUNTIME_LIBRARY=MultiThreadedDebug** in *build_cmake.sh* to avoid the following error:

`error LNK2038: mismatch detected for 'RuntimeLibrary': value 'MTd_StaticDebug' doesn't match value 'MDd_DynamicDebug' in app.obj` 

```bash
cmake .. -DFETCHCONTENT_QUIET=OFF -DCMAKE_BUILD_TYPE=Release -DCMAKE_MSVC_RUNTIME_LIBRARY=MultiThreadedDebug -DINCLUDE_DIR=$INCLUDE_DIR
```

- On Windows, when connecting to a TLS enabled gRPC endpoint, C++ grpc client doesn't load the trusted certs

`E0206 [...] ssl_security_connector.cc:420] Could not get default pem root certs.`
`E0206 [...] secure_channel_create.cc:88] Failed to create secure subchannel for secure name 'gateway-v0-grpc.kaiko.ovh:443'`
`E0206 [...] secure_channel_create.cc:48] Failed to create channel args during subchannel creation.`

This is a known issue in the Windows C++ implementation of the gRPC client.
There is a note in the official gRPC [documentation](https://grpc.io/docs/guides/auth/#using-client-side-ssltls) stating:
/!\ *Non-POSIX-compliant systems (such as Windows) need to specify the root certificates in SslCredentialsOptions, since the defaults are only configured for POSIX filesystems.*

There is a feature [request](https://github.com/grpc/grpc/issues/25533) on the gRPC repository to add out-of-the-box support for this on Windows to make the behavior of default `SslCredentials()` the same on all systems.
Until this functionality is added in a future version of gRPC, you can add a snippet of code, in order to populate `SslCredentialsOptions`,  as described [here](https://github.com/grpc/grpc/issues/25167)

### Fast rebuild (CMake)

- Build :

Once you have run the `build_cmake.sh` script once, you can do fast rebuild through `make`:

```bash
(cd build && make all)
```

## Build the example with conan (pre built binaries)

This allows you building faster as binaries are pre-built.

### Requirements (conan)

You will need `python3`, `pip`, and `conan` (>=1.47), and make installed on your machine. You can install conan through `pip` with this command: `pip install conan`

### Build (conan)

- Build :

```bash
CMAKE_BUILD_PARALLEL_LEVEL=8 ./build_conan.sh
```

- Run the example and get data from Kaiko API:

```bash
./build_conan/bin/app
```

## Build and run the example with docker

You will only need `docker` and `docker-compose` installed on your machine.

- Build and run :

```bash
./build_docker.sh
```

Note that for this particular step, you will need to setup an environment variable `KAIKO_API_KEY` with a valid Kaiko API key, otherwise you will get an error such as `PERMISSION_DENIED: not authorized`.

## Non-blocking (async) API using standard gRPC

C++ gRPC offers 2 different APIs (sync and async). Most of the examples are written using sync API (thread pool handled by gRPC), but an example of async API use is provided [here](https://github.com/kaikodata/kaiko-sdk-examples/blob/master/examples/cpp/async_api.cpp).

This API should mostly be used for high traffic subscriptions (such as Market update with all commodities for example).

## Non-blocking (async) API using C++20 coroutines and asio-boost

If you're looking for non-blocking API because you have strict constraints against thread-pooling, take a look at <https://github.com/Tradias/asio-grpc>.
This library will allow you to use a wrapper of C++ gRPC using Boost Asio and C++20 `co_await` feature.

An example usage is provided here : <https://github.com/Tradias/asio-grpc/blob/v1.3.1/example/streaming-client.cpp#L62>.

## Check for more recent versions

```bash
git ls-remote -t --refs https://github.com/kaikodata/kaiko-cpp-sdk
```
