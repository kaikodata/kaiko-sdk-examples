# Node SDK

- [how to use the various endpoints and their APIs](endpoints.ts).
This should be "quickstart" point for any user new to the SDK.
- [how to handle end of stream / resubscription](resubscribe.ts).
Disconnection can happen for lots of reasons (client or server side network, idle consumer for a very long time, etc.) and should be handled by resubscribing. Reconnection is already handled automatically by GRPC client library.

## Requirements

You will need to have Node.js runtime installed on your machine
Installation can be done here (<https://nodejs.or>g) or through third-party tools like NVM (<https://github.com/nvm-sh/nvm>).

The Node SDK offers both support for Node and Typescript/Node.

## Build the example (Typescript/Node)

- Install:

```bash
npm install
```

- Build :

```bash
npm run build
```

- Run :

```bash
npm start
```

Note that for this particular step, you will need to setup an environment variable `KAIKO_API_KEY` with a valid Kaiko API key, otherwise you will get an error such as `PERMISSION_DENIED: not authorized`.

## Build the example (Node only, recommended for production)

- Install:

```bash
npm install
```

- Build :

```bash
npm run build
```

- Run :

```bash
node dist/endpoints.js
```

Note that for this particular step, you will need to setup an environment variable `KAIKO_API_KEY` with a valid Kaiko API key, otherwise you will get an error such as `PERMISSION_DENIED: not authorized`.
