# C++ SDK

## Requirements

You will need `python3`, `pip`, and `conan`, and `make` installed on your machine.
You can install `conan` through `pip` with this command:

```bash
pip install conan
```

## Build the example

- Build :

```bash
CMAKE_BUILD_PARALLEL_LEVEL=8 ./build.sh
```

- Run the example and get data from Kaiko API:

```bash
./build/app
```

Note that for this particular step, you will need to setup an environment variable `KAIKO_API_KEY` with a valid Kaiko API key, otherwise you will get an error such as `PERMISSION_DENIED: not authorized`.

## Fast rebuild

- Build :

Once you have run the `build.sh` script once, you can do fast rebuild through `make`:

```bash
(cd build && make all)
```

## Check for more recent versions

```bash
git ls-remote -t --refs https://github.com/challengerdeep/kaiko-cpp-sdk
```
