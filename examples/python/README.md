# Python SDK

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

## Fix potential SSL issues

If you're having GRPC errors such as `GPRC ERROR 14 - Unavailable` or `OPENSSL_internal:CERTIFICATE_VERIFY_FAILED`, check your machine certificates, and particulary that you have Let's Encrypt root certificate (ISRG Root X1).
Most of the GRPC bindings come with bundled root certificates which do not always reflect actual world since they can be outdated.

One known workaround is to point to your own root certificate, filling `GRPC_DEFAULT_SSL_ROOTS_FILE_PATH`.

For example:

```bash
GRPC_DEFAULT_SSL_ROOTS_FILE_PATH=/etc/ssl/certs/ca-certificates.crt pipenv run python main.py
```

## Check for more recent versions

```bash
pipenv update --outdated
```
