#include <iostream>
#include <memory>
#include <string>
#include <sstream>

#include <grpcpp/grpcpp.h>
#include <sdk/sdk.grpc.pb.h>

using grpc::Channel;
using grpc::ChannelArguments;
using grpc::ClientContext;
using grpc::ClientReader;
using grpc::SecureChannelCredentials;
using grpc::Status;
using kaikosdk::InstrumentCriteria;
using kaikosdk::StreamAggregatesOHLCVRequestV1;
using kaikosdk::StreamAggregatesOHLCVResponseV1;
using kaikosdk::StreamAggregatesOHLCVServiceV1;
using kaikosdk::StreamAggregatesVWAPRequestV1;
using kaikosdk::StreamAggregatesVWAPResponseV1;
using kaikosdk::StreamAggregatesVWAPServiceV1;
using kaikosdk::StreamIndexServiceRequestV1;
using kaikosdk::StreamIndexServiceResponseV1;
using kaikosdk::StreamIndexServiceV1;
using kaikosdk::StreamMarketUpdateRequestV1;
using kaikosdk::StreamMarketUpdateResponseV1;
using kaikosdk::StreamMarketUpdateServiceV1;
using kaikosdk::StreamTradesRequestV1;
using kaikosdk::StreamTradesResponseV1;
using kaikosdk::StreamTradesServiceV1;
using kaikosdk::StreamAggregatedQuoteRequestV2;
using kaikosdk::StreamAggregatedQuoteResponseV2;
using kaikosdk::StreamAggregatedQuoteServiceV2;

void setupContext(ClientContext *context)
{
  // Setting custom metadata to be sent to the server
  std::stringstream authHeader;
  authHeader << "Bearer " << std::getenv("KAIKO_API_KEY");
  std::string auth = authHeader.str();
  context->AddMetadata("authorization", auth);
}

class TradeClient
{
public:
  TradeClient(std::shared_ptr<Channel> channel)
      : stub_(StreamTradesServiceV1::NewStub(channel)) {}

  // Assembles the client's payload, sends it and presents the response back
  // from the server.
  std::string Subscribe()
  {
    // Data we are sending to the server.
    StreamTradesRequestV1 request;

    // Globbing patterns are also supported on all fields. See http://sdk.kaiko.com/#instrument-selection for all supported patterns
    InstrumentCriteria *instrument_criteria = request.mutable_instrument_criteria();
    instrument_criteria->set_exchange("cbse");
    instrument_criteria->set_instrument_class("spot");
    instrument_criteria->set_code("btc-usd");

    // Context for the client. It could be used to convey extra information to
    // the server and/or tweak certain RPC behaviors.
    ClientContext context;
    setupContext(&context);

    std::unique_ptr<ClientReader<StreamTradesResponseV1>> reader(stub_->Subscribe(&context, request));

    // Container for the data we expect from the server.
    StreamTradesResponseV1 response;

    while (reader->Read(&response))
    {
      std::cout << response.DebugString() << std::endl;
    }

    // Act upon its status.
    Status status = reader->Finish();

    if (!status.ok())
    {
      std::stringstream ss;
      ss << "RPC error " << status.error_code() << ":" << status.error_message() << std::endl;

      return ss.str();
    }

    return "";
  }

private:
  std::unique_ptr<StreamTradesServiceV1::Stub> stub_;
};

class MarketUpdateClient
{
public:
  MarketUpdateClient(std::shared_ptr<Channel> channel)
      : stub_(StreamMarketUpdateServiceV1::NewStub(channel)) {}

  // Assembles the client's payload, sends it and presents the response back
  // from the server.
  std::string Subscribe()
  {
    // Data we are sending to the server.
    StreamMarketUpdateRequestV1 request;

    // Globbing patterns are also supported on all fields. See http://sdk.kaiko.com/#instrument-selection for all supported patterns
    InstrumentCriteria *instrument_criteria = request.mutable_instrument_criteria();
    instrument_criteria->set_exchange("cbse");
    instrument_criteria->set_instrument_class("spot");
    instrument_criteria->set_code("btc-usd");

    // Context for the client. It could be used to convey extra information to
    // the server and/or tweak certain RPC behaviors.
    ClientContext context;
    setupContext(&context);

    std::unique_ptr<ClientReader<StreamMarketUpdateResponseV1>> reader(stub_->Subscribe(&context, request));

    // Container for the data we expect from the server.
    StreamMarketUpdateResponseV1 response;

    while (reader->Read(&response))
    {
      std::cout << response.DebugString() << std::endl;
    }

    // Act upon its status.
    Status status = reader->Finish();

    if (!status.ok())
    {
      std::stringstream ss;
      ss << "RPC error " << status.error_code() << ":" << status.error_message() << std::endl;

      return ss.str();
    }

    return "";
  }

private:
  std::unique_ptr<StreamMarketUpdateServiceV1::Stub> stub_;
};

class OHLCVClient
{
public:
  OHLCVClient(std::shared_ptr<Channel> channel)
      : stub_(StreamAggregatesOHLCVServiceV1::NewStub(channel)) {}

  // Assembles the client's payload, sends it and presents the response back
  // from the server.
  std::string Subscribe()
  {
    // Data we are sending to the server.
    StreamAggregatesOHLCVRequestV1 request;

    InstrumentCriteria *instrument_criteria = request.mutable_instrument_criteria();
    instrument_criteria->set_exchange("cbse");
    instrument_criteria->set_instrument_class("spot");
    instrument_criteria->set_code("btc-usd");

    request.set_aggregate("1m");

    // Context for the client. It could be used to convey extra information to
    // the server and/or tweak certain RPC behaviors.
    ClientContext context;
    setupContext(&context);

    std::unique_ptr<ClientReader<StreamAggregatesOHLCVResponseV1>> reader(stub_->Subscribe(&context, request));

    // Container for the data we expect from the server.
    StreamAggregatesOHLCVResponseV1 response;

    while (reader->Read(&response))
    {
      std::cout << response.DebugString() << std::endl;
    }

    // Act upon its status.
    Status status = reader->Finish();

    if (!status.ok())
    {
      std::stringstream ss;
      ss << "RPC error " << status.error_code() << ":" << status.error_message() << std::endl;

      return ss.str();
    }

    return "";
  }

private:
  std::unique_ptr<StreamAggregatesOHLCVServiceV1::Stub> stub_;
};

class VWAPClient
{
public:
  VWAPClient(std::shared_ptr<Channel> channel)
      : stub_(StreamAggregatesVWAPServiceV1::NewStub(channel)) {}

  // Assembles the client's payload, sends it and presents the response back
  // from the server.
  std::string Subscribe()
  {
    // Data we are sending to the server.
    StreamAggregatesVWAPRequestV1 request;

    InstrumentCriteria *instrument_criteria = request.mutable_instrument_criteria();
    instrument_criteria->set_exchange("cbse");
    instrument_criteria->set_instrument_class("spot");
    instrument_criteria->set_code("btc-usd");

    request.set_aggregate("1m");

    // Context for the client. It could be used to convey extra information to
    // the server and/or tweak certain RPC behaviors.
    ClientContext context;
    setupContext(&context);

    std::unique_ptr<ClientReader<StreamAggregatesVWAPResponseV1>> reader(stub_->Subscribe(&context, request));

    // Container for the data we expect from the server.
    StreamAggregatesVWAPResponseV1 response;

    while (reader->Read(&response))
    {
      std::cout << response.DebugString() << std::endl;
    }

    // Act upon its status.
    Status status = reader->Finish();

    if (!status.ok())
    {
      std::stringstream ss;
      ss << "RPC error " << status.error_code() << ":" << status.error_message() << std::endl;

      return ss.str();
    }

    return "";
  }

private:
  std::unique_ptr<StreamAggregatesVWAPServiceV1::Stub> stub_;
};

class IndexClient
{
public:
  IndexClient(std::shared_ptr<Channel> channel)
      : stub_(StreamIndexServiceV1::NewStub(channel)) {}

  // Assembles the client's payload, sends it and presents the response back
  // from the server.
  std::string Subscribe()
  {
    // Data we are sending to the server.
    StreamIndexServiceRequestV1 request;

    request.set_index_code("index_code"); // fill it with actual value

    // Context for the client. It could be used to convey extra information to
    // the server and/or tweak certain RPC behaviors.
    ClientContext context;
    setupContext(&context);

    std::unique_ptr<ClientReader<StreamIndexServiceResponseV1>> reader(stub_->Subscribe(&context, request));

    // Container for the data we expect from the server.
    StreamIndexServiceResponseV1 response;

    while (reader->Read(&response))
    {
      std::cout << response.DebugString() << std::endl;
    }

    // Act upon its status.
    Status status = reader->Finish();

    if (!status.ok())
    {
      std::stringstream ss;
      ss << "RPC error " << status.error_code() << ":" << status.error_message() << std::endl;

      return ss.str();
    }

    return "";
  }

private:
  std::unique_ptr<StreamIndexServiceV1::Stub> stub_;
};

class AggregatedQuoteClient
{
public:
  AggregatedQuoteClient(std::shared_ptr<Channel> channel)
      : stub_(StreamAggregatedQuoteServiceV2::NewStub(channel)) {}

  // Assembles the client's payload, sends it and presents the response back
  // from the server.
  std::string Subscribe()
  {
    // Data we are sending to the server.
    StreamAggregatedQuoteRequestV2 request;

    // Globbing patterns are also supported on all fields. See http://sdk.kaiko.com/#instrument-selection for all supported patterns
    request.set_instrument_class("spot");
    request.set_code("btc-usd");

    // Context for the client. It could be used to convey extra information to
    // the server and/or tweak certain RPC behaviors.
    ClientContext context;
    setupContext(&context);

    std::unique_ptr<ClientReader<StreamAggregatedQuoteResponseV2>> reader(stub_->Subscribe(&context, request));

    // Container for the data we expect from the server.
    StreamAggregatedQuoteResponseV2 response;

    while (reader->Read(&response))
    {
      std::cout << response.DebugString() << std::endl;
    }

    // Act upon its status.
    Status status = reader->Finish();

    if (!status.ok())
    {
      std::stringstream ss;
      ss << "RPC error " << status.error_code() << ":" << status.error_message() << std::endl;

      return ss.str();
    }

    return "";
  }

private:
  std::unique_ptr<StreamAggregatedQuoteServiceV2::Stub> stub_;
};

int main(int argc, char **argv)
{
  ChannelArguments args;

  grpc::SslCredentialsOptions sslOptions = grpc::SslCredentialsOptions();
  auto channel = grpc::CreateCustomChannel("gateway-v0-grpc.kaiko.ovh:443",
                                           grpc::SslCredentials(sslOptions), args);
  TradeClient client = TradeClient(channel);
  // MarketUpdateClient client = MarketUpdateClient(channel);
  // OHLCVClient client = OHLCVClient(channel);
  // VWAPClient client = VWAPClient(channel);
  // SpotExchangeRateClient client = SpotExchangeRateClient(channel);
  // DirectExchangeRateClient client = DirectExchangeRateClient(channel);
  // IndexClient client = IndexClient(channel);
  // AggregatedQuoteClient client = AggregatedQuoteClient(channel);
  
  std::string reply = client.Subscribe();
  std::cout << "Subscribe received: " << reply << std::endl;

  return 0;
}
