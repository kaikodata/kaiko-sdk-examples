# C++ SDK

This repository contains 2 kinds of example:

- [how to use the various endpoints and their APIs](app.cpp).
This should be "quickstart" point for any user new to the SDK.
- [how to handle end of stream / resubscription](resubscribe.cpp).
Disconnection can happen for lots of reasons (client or server side network, idle consumer for a very long time, etc.) and should be handled by resubscribing. Reconnection is already handled automatically by GRPC client library.

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
This library will allow you to use a wrapper of C++ GRPC using Boost Asio and C++20 `co_await` feature.

An example usage is provided here : <https://github.com/Tradias/asio-grpc/blob/v1.3.1/example/streaming-client.cpp#L62>.

## Check for more recent versions

```bash
git ls-remote -t --refs https://github.com/kaikodata/kaiko-cpp-sdk
```
