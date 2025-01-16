#include <iostream>
#include <memory>
#include <sstream>
#include <string>

#include <grpcpp/grpcpp.h>
#include <sdk/sdk.grpc.pb.h>

using google::protobuf::Duration;
using grpc::Channel;
using grpc::ChannelArguments;
using grpc::ClientContext;
using grpc::ClientReader;
using grpc::SecureChannelCredentials;
using grpc::Status;
using kaikosdk::Assets;
using kaikosdk::InstrumentCriteria;
using kaikosdk::StreamAggregatedQuoteRequestV2;
using kaikosdk::StreamAggregatedQuoteResponseV2;
using kaikosdk::StreamAggregatedQuoteServiceV2;
using kaikosdk::StreamAggregatesDirectExchangeRateV2RequestV1;
using kaikosdk::StreamAggregatesDirectExchangeRateV2ResponseV1;
using kaikosdk::StreamAggregatesOHLCVRequestV1;
using kaikosdk::StreamAggregatesOHLCVResponseV1;
using kaikosdk::StreamAggregatesOHLCVServiceV1;
using kaikosdk::StreamAggregatesSpotDirectExchangeRateV2ServiceV1;
using kaikosdk::StreamAggregatesSpotExchangeRateV2RequestV1;
using kaikosdk::StreamAggregatesSpotExchangeRateV2ResponseV1;
using kaikosdk::StreamAggregatesSpotExchangeRateV2ServiceV1;
using kaikosdk::StreamAggregatesVWAPRequestV1;
using kaikosdk::StreamAggregatesVWAPResponseV1;
using kaikosdk::StreamAggregatesVWAPServiceV1;
using kaikosdk::StreamDerivativesInstrumentMetricsRequestV1;
using kaikosdk::StreamDerivativesInstrumentMetricsResponseV1;
using kaikosdk::StreamDerivativesInstrumentMetricsServiceV1;
using kaikosdk::StreamExoticIndicesServiceRequestV1;
using kaikosdk::StreamExoticIndicesServiceResponseV1;
using kaikosdk::StreamExoticIndicesServiceV1;
using kaikosdk::StreamIndexForexRateServiceRequestV1;
using kaikosdk::StreamIndexForexRateServiceResponseV1;
using kaikosdk::StreamIndexForexRateServiceV1;
using kaikosdk::StreamIndexMultiAssetsServiceRequestV1;
using kaikosdk::StreamIndexMultiAssetsServiceResponseV1;
using kaikosdk::StreamIndexMultiAssetsServiceV1;
using kaikosdk::StreamIndexServiceRequestV1;
using kaikosdk::StreamIndexServiceResponseV1;
using kaikosdk::StreamIndexServiceV1;
using kaikosdk::StreamIvSviParametersRequestV1;
using kaikosdk::StreamIvSviParametersResponseV1;
using kaikosdk::StreamIvSviParametersServiceV1;
using kaikosdk::StreamMarketUpdateRequestV1;
using kaikosdk::StreamMarketUpdateResponseV1;
using kaikosdk::StreamMarketUpdateServiceV1;
using kaikosdk::StreamOrderBookL2RequestV1;
using kaikosdk::StreamOrderBookL2ResponseV1;
using kaikosdk::StreamOrderbookL2ServiceV1;
using kaikosdk::StreamTradesRequestV1;
using kaikosdk::StreamTradesResponseV1;
using kaikosdk::StreamTradesServiceV1;

void setupContext(ClientContext *context) {
  // Setting custom metadata to be sent to the server
  std::stringstream authHeader;
  authHeader << "Bearer " << std::getenv("KAIKO_API_KEY");
  std::string auth = authHeader.str();
  context->AddMetadata("authorization", auth);
}

class TradeClient {
public:
  TradeClient(std::shared_ptr<Channel> channel)
      : stub_(StreamTradesServiceV1::NewStub(channel)) {}

  // Assembles the client's payload, sends it and presents the response back
  // from the server.
  std::string Subscribe() {
    // Data we are sending to the server.
    StreamTradesRequestV1 request;

    // Globbing patterns are also supported on all fields. See
    // http://sdk.kaiko.com/#instrument-selection for all supported patterns
    InstrumentCriteria *instrument_criteria =
        request.mutable_instrument_criteria();
    instrument_criteria->set_exchange("cbse");
    instrument_criteria->set_instrument_class("spot");
    instrument_criteria->set_code("btc-usd");

    // Context for the client. It could be used to convey extra information to
    // the server and/or tweak certain RPC behaviors.
    ClientContext context;
    setupContext(&context);

    std::unique_ptr<ClientReader<StreamTradesResponseV1>> reader(
        stub_->Subscribe(&context, request));

    // Container for the data we expect from the server.
    StreamTradesResponseV1 response;

    while (reader->Read(&response)) {
      std::cout << response.DebugString() << std::endl;
    }

    // Act upon its status.
    Status status = reader->Finish();

    if (!status.ok()) {
      std::stringstream ss;
      ss << "RPC error " << status.error_code() << ":" << status.error_message()
         << std::endl;

      return ss.str();
    }

    return "";
  }

private:
  std::unique_ptr<StreamTradesServiceV1::Stub> stub_;
};

class MarketUpdateClient {
public:
  MarketUpdateClient(std::shared_ptr<Channel> channel)
      : stub_(StreamMarketUpdateServiceV1::NewStub(channel)) {}

  // Assembles the client's payload, sends it and presents the response back
  // from the server.
  std::string Subscribe() {
    // Data we are sending to the server.
    StreamMarketUpdateRequestV1 request;

    // Globbing patterns are also supported on all fields. See
    // http://sdk.kaiko.com/#instrument-selection for all supported patterns
    InstrumentCriteria *instrument_criteria =
        request.mutable_instrument_criteria();
    instrument_criteria->set_exchange("cbse");
    instrument_criteria->set_instrument_class("spot");
    instrument_criteria->set_code("btc-usd");

    // Context for the client. It could be used to convey extra information to
    // the server and/or tweak certain RPC behaviors.
    ClientContext context;
    setupContext(&context);

    std::unique_ptr<ClientReader<StreamMarketUpdateResponseV1>> reader(
        stub_->Subscribe(&context, request));

    // Container for the data we expect from the server.
    StreamMarketUpdateResponseV1 response;

    while (reader->Read(&response)) {
      std::cout << response.DebugString() << std::endl;
    }

    // Act upon its status.
    Status status = reader->Finish();

    if (!status.ok()) {
      std::stringstream ss;
      ss << "RPC error " << status.error_code() << ":" << status.error_message()
         << std::endl;

      return ss.str();
    }

    return "";
  }

private:
  std::unique_ptr<StreamMarketUpdateServiceV1::Stub> stub_;
};

class OHLCVClient {
public:
  OHLCVClient(std::shared_ptr<Channel> channel)
      : stub_(StreamAggregatesOHLCVServiceV1::NewStub(channel)) {}

  // Assembles the client's payload, sends it and presents the response back
  // from the server.
  std::string Subscribe() {
    // Data we are sending to the server.
    StreamAggregatesOHLCVRequestV1 request;

    InstrumentCriteria *instrument_criteria =
        request.mutable_instrument_criteria();
    instrument_criteria->set_exchange("cbse");
    instrument_criteria->set_instrument_class("spot");
    instrument_criteria->set_code("btc-usd");

    request.set_aggregate("1m");

    // Context for the client. It could be used to convey extra information to
    // the server and/or tweak certain RPC behaviors.
    ClientContext context;
    setupContext(&context);

    std::unique_ptr<ClientReader<StreamAggregatesOHLCVResponseV1>> reader(
        stub_->Subscribe(&context, request));

    // Container for the data we expect from the server.
    StreamAggregatesOHLCVResponseV1 response;

    while (reader->Read(&response)) {
      std::cout << response.DebugString() << std::endl;
    }

    // Act upon its status.
    Status status = reader->Finish();

    if (!status.ok()) {
      std::stringstream ss;
      ss << "RPC error " << status.error_code() << ":" << status.error_message()
         << std::endl;

      return ss.str();
    }

    return "";
  }

private:
  std::unique_ptr<StreamAggregatesOHLCVServiceV1::Stub> stub_;
};

class VWAPClient {
public:
  VWAPClient(std::shared_ptr<Channel> channel)
      : stub_(StreamAggregatesVWAPServiceV1::NewStub(channel)) {}

  // Assembles the client's payload, sends it and presents the response back
  // from the server.
  std::string Subscribe() {
    // Data we are sending to the server.
    StreamAggregatesVWAPRequestV1 request;

    InstrumentCriteria *instrument_criteria =
        request.mutable_instrument_criteria();
    instrument_criteria->set_exchange("cbse");
    instrument_criteria->set_instrument_class("spot");
    instrument_criteria->set_code("btc-usd");

    request.set_aggregate("1m");

    // Context for the client. It could be used to convey extra information to
    // the server and/or tweak certain RPC behaviors.
    ClientContext context;
    setupContext(&context);

    std::unique_ptr<ClientReader<StreamAggregatesVWAPResponseV1>> reader(
        stub_->Subscribe(&context, request));

    // Container for the data we expect from the server.
    StreamAggregatesVWAPResponseV1 response;

    while (reader->Read(&response)) {
      std::cout << response.DebugString() << std::endl;
    }

    // Act upon its status.
    Status status = reader->Finish();

    if (!status.ok()) {
      std::stringstream ss;
      ss << "RPC error " << status.error_code() << ":" << status.error_message()
         << std::endl;

      return ss.str();
    }

    return "";
  }

private:
  std::unique_ptr<StreamAggregatesVWAPServiceV1::Stub> stub_;
};

class IndexRateClient {
public:
  IndexRateClient(std::shared_ptr<Channel> channel)
      : stub_(StreamIndexServiceV1::NewStub(channel)) {}

  // Assembles the client's payload, sends it and presents the response back
  // from the server.
  std::string Subscribe() {
    // Data we are sending to the server.
    StreamIndexServiceRequestV1 request;

    request.set_index_code("KK_BRR_BTCUSD");

    // Context for the client. It could be used to convey extra information to
    // the server and/or tweak certain RPC behaviors.
    ClientContext context;
    setupContext(&context);

    std::unique_ptr<ClientReader<StreamIndexServiceResponseV1>> reader(
        stub_->Subscribe(&context, request));

    // Container for the data we expect from the server.
    StreamIndexServiceResponseV1 response;

    while (reader->Read(&response)) {
      std::cout << response.DebugString() << std::endl;
    }

    // Act upon its status.
    Status status = reader->Finish();

    if (!status.ok()) {
      std::stringstream ss;
      ss << "RPC error " << status.error_code() << ":" << status.error_message()
         << std::endl;

      return ss.str();
    }

    return "";
  }

private:
  std::unique_ptr<StreamIndexServiceV1::Stub> stub_;
};

class IndexMultiAssetClient {
public:
  IndexMultiAssetClient(std::shared_ptr<Channel> channel)
      : stub_(StreamIndexMultiAssetsServiceV1::NewStub(channel)) {}

  // Assembles the client's payload, sends it and presents the response back
  // from the server.
  std::string Subscribe() {
    // Data we are sending to the server.
    StreamIndexMultiAssetsServiceRequestV1 request;

    request.set_index_code("KT15");

    // Context for the client. It could be used to convey extra information to
    // the server and/or tweak certain RPC behaviors.
    ClientContext context;
    setupContext(&context);

    std::unique_ptr<ClientReader<StreamIndexMultiAssetsServiceResponseV1>>
        reader(stub_->Subscribe(&context, request));

    // Container for the data we expect from the server.
    StreamIndexMultiAssetsServiceResponseV1 response;

    while (reader->Read(&response)) {
      std::cout << response.DebugString() << std::endl;
    }

    // Act upon its status.
    Status status = reader->Finish();

    if (!status.ok()) {
      std::stringstream ss;
      ss << "RPC error " << status.error_code() << ":" << status.error_message()
         << std::endl;

      return ss.str();
    }

    return "";
  }

private:
  std::unique_ptr<StreamIndexMultiAssetsServiceV1::Stub> stub_;
};

class IndexForexRateClient {
public:
  IndexForexRateClient(std::shared_ptr<Channel> channel)
      : stub_(StreamIndexForexRateServiceV1::NewStub(channel)) {}

  // Assembles the client's payload, sends it and presents the response back
  // from the server.
  std::string Subscribe() {
    // Data we are sending to the server.
    StreamIndexForexRateServiceRequestV1 request;

    request.set_index_code("KK_BRR_BTCUSD_EUR");

    // Context for the client. It could be used to convey extra information to
    // the server and/or tweak certain RPC behaviors.
    ClientContext context;
    setupContext(&context);

    std::unique_ptr<ClientReader<StreamIndexForexRateServiceResponseV1>> reader(
        stub_->Subscribe(&context, request));

    // Container for the data we expect from the server.
    StreamIndexForexRateServiceResponseV1 response;

    while (reader->Read(&response)) {
      std::cout << response.DebugString() << std::endl;
    }

    // Act upon its status.
    Status status = reader->Finish();

    if (!status.ok()) {
      std::stringstream ss;
      ss << "RPC error " << status.error_code() << ":" << status.error_message()
         << std::endl;

      return ss.str();
    }

    return "";
  }

private:
  std::unique_ptr<StreamIndexForexRateServiceV1::Stub> stub_;
};

class AggregatedQuoteClient {
public:
  AggregatedQuoteClient(std::shared_ptr<Channel> channel)
      : stub_(StreamAggregatedQuoteServiceV2::NewStub(channel)) {}

  // Assembles the client's payload, sends it and presents the response back
  // from the server.
  std::string Subscribe() {
    // Data we are sending to the server.
    StreamAggregatedQuoteRequestV2 request;

    // Globbing patterns are also supported on all fields. See
    // http://sdk.kaiko.com/#instrument-selection for all supported patterns
    request.set_instrument_class("spot");
    request.set_code("btc-usd");

    // Context for the client. It could be used to convey extra information to
    // the server and/or tweak certain RPC behaviors.
    ClientContext context;
    setupContext(&context);

    std::unique_ptr<ClientReader<StreamAggregatedQuoteResponseV2>> reader(
        stub_->Subscribe(&context, request));

    // Container for the data we expect from the server.
    StreamAggregatedQuoteResponseV2 response;

    while (reader->Read(&response)) {
      std::cout << response.DebugString() << std::endl;
    }

    // Act upon its status.
    Status status = reader->Finish();

    if (!status.ok()) {
      std::stringstream ss;
      ss << "RPC error " << status.error_code() << ":" << status.error_message()
         << std::endl;

      return ss.str();
    }

    return "";
  }

private:
  std::unique_ptr<StreamAggregatedQuoteServiceV2::Stub> stub_;
};

class AggregatesSpotExchangeRateClient {
public:
  AggregatesSpotExchangeRateClient(std::shared_ptr<Channel> channel)
      : stub_(StreamAggregatesSpotExchangeRateV2ServiceV1::NewStub(channel)) {}

  // Assembles the client's payload, sends it and presents the response back
  // from the server.
  std::string Subscribe() {
    // Data we are sending to the server.
    StreamAggregatesSpotExchangeRateV2RequestV1 request;

    Assets *assets = request.mutable_assets();
    assets->set_base("btc");
    assets->set_quote("usd");

    Duration *window = request.mutable_window();
    window->set_seconds(10);

    Duration *update_frequency = request.mutable_update_frequency();
    update_frequency->set_seconds(2);

    // Context for the client. It could be used to convey extra information to
    // the server and/or tweak certain RPC behaviors.
    ClientContext context;
    setupContext(&context);

    std::unique_ptr<ClientReader<StreamAggregatesSpotExchangeRateV2ResponseV1>>
        reader(stub_->Subscribe(&context, request));

    // Container for the data we expect from the server.
    StreamAggregatesSpotExchangeRateV2ResponseV1 response;

    while (reader->Read(&response)) {
      std::cout << response.DebugString() << std::endl;
    }

    // Act upon its status.
    Status status = reader->Finish();

    if (!status.ok()) {
      std::stringstream ss;
      ss << "RPC error " << status.error_code() << ":" << status.error_message()
         << std::endl;

      return ss.str();
    }

    return "";
  }

private:
  std::unique_ptr<StreamAggregatesSpotExchangeRateV2ServiceV1::Stub> stub_;
};

class AggregatesDirectExchangeRateClient {
public:
  AggregatesDirectExchangeRateClient(std::shared_ptr<Channel> channel)
      : stub_(StreamAggregatesSpotDirectExchangeRateV2ServiceV1::NewStub(
            channel)) {}

  // Assembles the client's payload, sends it and presents the response back
  // from the server.
  std::string Subscribe() {
    // Data we are sending to the server.
    StreamAggregatesDirectExchangeRateV2RequestV1 request;

    Assets *assets = request.mutable_assets();
    assets->set_base("btc");
    assets->set_quote("usd");

    Duration *window = request.mutable_window();
    window->set_seconds(10);

    Duration *update_frequency = request.mutable_update_frequency();
    update_frequency->set_seconds(2);

    // Context for the client. It could be used to convey extra information to
    // the server and/or tweak certain RPC behaviors.
    ClientContext context;
    setupContext(&context);

    std::unique_ptr<
        ClientReader<StreamAggregatesDirectExchangeRateV2ResponseV1>>
        reader(stub_->Subscribe(&context, request));

    // Container for the data we expect from the server.
    StreamAggregatesDirectExchangeRateV2ResponseV1 response;

    while (reader->Read(&response)) {
      std::cout << response.DebugString() << std::endl;
    }

    // Act upon its status.
    Status status = reader->Finish();

    if (!status.ok()) {
      std::stringstream ss;
      ss << "RPC error " << status.error_code() << ":" << status.error_message()
         << std::endl;

      return ss.str();
    }

    return "";
  }

private:
  std::unique_ptr<StreamAggregatesSpotDirectExchangeRateV2ServiceV1::Stub>
      stub_;
};

class DerivativesInstrumentMetricsClient {
public:
  DerivativesInstrumentMetricsClient(std::shared_ptr<Channel> channel)
      : stub_(StreamDerivativesInstrumentMetricsServiceV1::NewStub(channel)) {}

  // Assembles the client's payload, sends it and presents the response back
  // from the server.
  std::string Subscribe() {
    // Data we are sending to the server.
    StreamDerivativesInstrumentMetricsRequestV1 request;

    // Globbing patterns are also supported on all fields. See
    // http://sdk.kaiko.com/#instrument-selection for all supported patterns
    InstrumentCriteria *instrument_criteria =
        request.mutable_instrument_criteria();
    instrument_criteria->set_exchange("*");
    instrument_criteria->set_instrument_class("perpetual-future");
    instrument_criteria->set_code("btc-usd");

    // Context for the client. It could be used to convey extra information to
    // the server and/or tweak certain RPC behaviors.
    ClientContext context;
    setupContext(&context);

    std::unique_ptr<ClientReader<StreamDerivativesInstrumentMetricsResponseV1>>
        reader(stub_->Subscribe(&context, request));

    // Container for the data we expect from the server.
    StreamDerivativesInstrumentMetricsResponseV1 response;

    while (reader->Read(&response)) {
      std::cout << response.DebugString() << std::endl;
    }

    // Act upon its status.
    Status status = reader->Finish();

    if (!status.ok()) {
      std::stringstream ss;
      ss << "RPC error " << status.error_code() << ":" << status.error_message()
         << std::endl;

      return ss.str();
    }

    return "";
  }

private:
  std::unique_ptr<StreamDerivativesInstrumentMetricsServiceV1::Stub> stub_;
};

class IvSviParametersClient {
public:
  IvSviParametersClient(std::shared_ptr<Channel> channel)
      : stub_(StreamIvSviParametersServiceV1::NewStub(channel)) {}

  // Assembles the client's payload, sends it and presents the response back
  // from the server.
  std::string Subscribe() {
    // Data we are sending to the server.
    StreamIvSviParametersRequestV1 request;

    // Wildcard "*" is also supported, refer to documentation for the available
    // list.
    Assets *assets = request.mutable_assets();
    assets->set_base("btc");
    assets->set_quote("usd");

    // Wildcard "*" is also supported, refer to documentation for the available
    // list.
    request.set_exchanges("drbt");

    // Context for the client. It could be used to convey extra information to
    // the server and/or tweak certain RPC behaviors.
    ClientContext context;
    setupContext(&context);

    std::unique_ptr<ClientReader<StreamIvSviParametersResponseV1>> reader(
        stub_->Subscribe(&context, request));

    // Container for the data we expect from the server.
    StreamIvSviParametersResponseV1 response;

    while (reader->Read(&response)) {
      std::cout << response.DebugString() << std::endl;
    }

    // Act upon its status.
    Status status = reader->Finish();

    if (!status.ok()) {
      std::stringstream ss;
      ss << "RPC error " << status.error_code() << ":" << status.error_message()
         << std::endl;

      return ss.str();
    }

    return "";
  }

private:
  std::unique_ptr<StreamIvSviParametersServiceV1::Stub> stub_;
};

class ExoticIndicesClient {
public:
  ExoticIndicesClient(std::shared_ptr<Channel> channel)
      : stub_(StreamExoticIndicesServiceV1::NewStub(channel)) {}

  // Assembles the client's payload, sends it and presents the response back
  // from the server.
  std::string Subscribe() {
    // Data we are sending to the server.
    StreamExoticIndicesServiceRequestV1 request;

    // Wildcard "*" is also supported, refer to documentation for the available
    // list.
    request.set_index_code("KT10TCUSD");

    // Context for the client. It could be used to convey extra information to
    // the server and/or tweak certain RPC behaviors.
    ClientContext context;
    setupContext(&context);

    std::unique_ptr<ClientReader<StreamExoticIndicesServiceResponseV1>> reader(
        stub_->Subscribe(&context, request));

    // Container for the data we expect from the server.
    StreamExoticIndicesServiceResponseV1 response;

    while (reader->Read(&response)) {
      std::cout << response.DebugString() << std::endl;
    }

    // Act upon its status.
    Status status = reader->Finish();

    if (!status.ok()) {
      std::stringstream ss;
      ss << "RPC error " << status.error_code() << ":" << status.error_message()
         << std::endl;

      return ss.str();
    }

    return "";
  }

private:
  std::unique_ptr<StreamExoticIndicesServiceV1::Stub> stub_;
};

class StreamOrderbookL2Client {
public:
  StreamOrderbookL2Client(std::shared_ptr<Channel> channel)
      : stub_(StreamOrderbookL2ServiceV1::NewStub(channel)) {}

  // Assembles the client's payload, sends it and presents the response back
  // from the server.
  std::string Subscribe() {
    // Data we are sending to the server.
    StreamOrderBookL2RequestV1 request;

    // Globbing patterns are also supported on all fields. See
    // http://sdk.kaiko.com/#instrument-selection for all supported patterns
    InstrumentCriteria *instrument_criteria =
        request.mutable_instrument_criteria();
    instrument_criteria->set_exchange("cbse");
    instrument_criteria->set_instrument_class("spot");
    instrument_criteria->set_code("btc-usd");

    // Context for the client. It could be used to convey extra information to
    // the server and/or tweak certain RPC behaviors.
    ClientContext context;
    setupContext(&context);

    std::unique_ptr<ClientReader<StreamOrderBookL2ResponseV1>> reader(
        stub_->Subscribe(&context, request));

    // Container for the data we expect from the server.
    StreamOrderBookL2ResponseV1 response;

    while (reader->Read(&response)) {
      std::cout << response.DebugString() << std::endl;
    }

    // Act upon its status.
    Status status = reader->Finish();

    if (!status.ok()) {
      std::stringstream ss;
      ss << "RPC error " << status.error_code() << ":" << status.error_message()
         << std::endl;

      return ss.str();
    }

    return "";
  }

private:
  std::unique_ptr<StreamOrderbookL2ServiceV1::Stub> stub_;
};

int main(int argc, char **argv) {
  ChannelArguments args;

  grpc::SslCredentialsOptions sslOptions = grpc::SslCredentialsOptions();
  auto channel = grpc::CreateCustomChannel(
      "gateway-v0-grpc.kaiko.ovh:443", grpc::SslCredentials(sslOptions), args);
  TradeClient client = TradeClient(channel);
  // StreamOrderbookL2Client client = StreamOrderbookL2Client(channel);
  // MarketUpdateClient client = MarketUpdateClient(channel);
  // OHLCVClient client = OHLCVClient(channel);
  // VWAPClient client = VWAPClient(channel);
  // IndexRateClient client = IndexRateClient(channel);
  // IndexMultiAssetClient client = IndexMultiAssetClient(channel);
  // IndexForexRateClient client = IndexForexRateClient(channel);
  // AggregatedQuoteClient client = AggregatedQuoteClient(channel);
  // AggregatesSpotExchangeRateClient client =
  // AggregatesSpotExchangeRateClient(channel);
  // AggregatesDirectExchangeRateClient client =
  // AggregatesDirectExchangeRateClient(channel); IvSviParametersClient client =
  // IvSviParametersClient(channel); DerivativesInstrumentMetricsClient client =
  // DerivativesInstrumentMetricsClient(channel); ExoticIndicesClient client =
  // ExoticIndicesClient(channel);

  std::string reply = client.Subscribe();
  std::cout << "Subscribe received: " << reply << std::endl;

  return 0;
}
