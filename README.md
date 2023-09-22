# kaiko-sdk-examples

This repository shows how to use Kaiko SDK in different languages and integrating with common build tools for these platforms.

## How it works

Kaiko SDK offers a ready to use client in each language for accessing Kaiko API, leveraging gRPC Protocol and its capabilities.
gRPC overview documentation is available here: <https://grpc.io/docs/what-is-grpc/introduction/>.

## Using stream

Stream works with *subscriptions*. Once you have a client connected to our services, you can one or more *subscriptions*.
Generally speaking, when the endpoint is providing this ability, you should have only 1 subscription for all instruments you're interested in.

This is done through [globbing patterns](http://sdk.kaiko.com/#instrument-selection).
A simple example of this would be : `krkn,bfnx:*:btc-usd` (means `Kraken and Bitfinex exchange AND all kinds of class AND btc-usd`).
In stream terminology, it would generally be split in an `InstrumentCriteria`, with `<exchange>:<class>:<code>` corresponding to criteria fields (see [criteria documentation](http://sdk.kaiko.com/#tocS_kaikosdkInstrumentCriteria))

Re-connection is handled automatically by the SDK.

## Documentation

High level documentation on SDK can be found here: <http://sdk.kaiko.com>.

General SDK API can be found here: <https://sdk-documentation.kaiko.ovh>.

API References for each language :

- CSharp: <https://sdk-documentation.kaiko.ovh/csharp>
- Go : <https://sdk-documentation.kaiko.ovh/go>
- Java : <https://sdk-documentation.kaiko.ovh/java>
- Node : <https://sdk-documentation.kaiko.ovh/node>
- Python: <https://sdk-documentation.kaiko.ovh/python>
- Scala : <https://sdk-documentation.kaiko.ovh/scala>

## Requirements

Each language folder specifies tool requirements.
