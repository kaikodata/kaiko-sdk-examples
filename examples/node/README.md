# Node SDK

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

## Build the example (Node only)

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
node dist/index.js
```

Note that for this particular step, you will need to setup an environment variable `KAIKO_API_KEY` with a valid Kaiko API key, otherwise you will get an error such as `PERMISSION_DENIED: not authorized`.
