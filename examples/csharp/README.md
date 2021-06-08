# CSharp SDK

## Requirements

You will need .Net Core 5 or later installed on your machine.
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
