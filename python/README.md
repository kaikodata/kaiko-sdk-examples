# Python SDK

- [how to use the various endpoints and their APIs](endpoints.py).
This should be "quickstart" point for any user new to the SDK.
- [how to handle end of stream / resubscription](resubscribe.py).
Disconnection can happen for lots of reasons (client or server side network, idle consumer for a very long time, etc.) and should be handled by resubscribing. Reconnection is already handled automatically by gRPC client library.

## Requirements

You will need to have Python 3.9 installed on your machine
Installation can be done via this tutorial (<https://realpython.com/installing-python/>) if not already present.

You will also need  dependency tools

- `pip` (usually already present in your Python 3 installation)
- `pipenv` that you can install with `pip install pipenv` (or other means, see <https://docs.pipenv.org/#install-pipenv-today>)

If you are using other tools like `conda`, `virtualenv` or `venv`, please refer to this documentation to see migration paths (<https://docs.pipenv.org/advanced/#pipenv-and-conda>) or your own tool documentation.

## Run with pipenv

- Install:

```bash
pipenv install
```

- Run :

```bash
pipenv run python endpoints.py
```

Note that for this particular step, you will need to setup an environment variable `KAIKO_API_KEY` with a valid Kaiko API key, otherwise you will get an error such as `PERMISSION_DENIED: not authorized`.

## Run with local python installation

- Install:

```bash
pip install -r requirements.txt
```

- Build :

```bash
python endpoints.py
```

Note that for this particular step, you will need to setup an environment variable `KAIKO_API_KEY` with a valid Kaiko API key, otherwise you will get an error such as `PERMISSION_DENIED: not authorized`.

## Default JSON serialization warning

These examples contain usage of `MessageToJson` method to serialize data to JSON for debug purpose.

Please note however that this method is known to not perform well under heavy load / traffic and **should never be used in the reading loop in production**. It will be very CPU intensive and most likely make your code not keep up with the traffic of endpoints such as `market_update_v1`.

## Fix potential SSL issues

If you're having gRPC errors such as `GPRC ERROR 14 - Unavailable` or `OPENSSL_internal:CERTIFICATE_VERIFY_FAILED`, check your machine certificates, and particulary that you have Let's Encrypt root certificate (ISRG Root X1).
Most of the gRPC bindings come with bundled root certificates which do not always reflect actual world since they can be outdated.

One known workaround is to point to your own root certificate, filling `gRPC_DEFAULT_SSL_ROOTS_FILE_PATH`.

For example:

```bash
gRPC_DEFAULT_SSL_ROOTS_FILE_PATH=/etc/ssl/certs/ca-certificates.crt pipenv run python main.py
```

## Check for more recent versions

```bash
pipenv update --outdated
```

## Update documentation on docs.kaiko.com

```bash
cd code-example-generator-script
python3 generate_examples.py
```