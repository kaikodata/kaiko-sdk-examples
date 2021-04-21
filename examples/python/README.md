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
pipenv run python main.py
```

Note that for this particular step, you will need to setup an environment variable `KAIKO_API_KEY` with a valid Kaiko API key, otherwise you will get an error such as `PERMISSION_DENIED: not authorized`.

## Run with local python installation

- Install:

```bash
pip install -r requirements.txt
```

- Build :

```bash
python main.py
```

Note that for this particular step, you will need to setup an environment variable `KAIKO_API_KEY` with a valid Kaiko API key, otherwise you will get an error such as `PERMISSION_DENIED: not authorized`.
