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
using kaikosdk::StreamMarketUpdateRequestV1;
using kaikosdk::StreamMarketUpdateResponseV1;
using kaikosdk::StreamMarketUpdateServiceV1;
using kaikosdk::StreamMarketUpdateCommodity;

void setupContext(ClientContext *context)
{
  // Setting custom metadata to be sent to the server
  std::stringstream authHeader;
  authHeader << "Bearer " << std::getenv("KAIKO_API_KEY");
  std::string auth = authHeader.str();
  context->AddMetadata("authorization", auth);
}

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
    instrument_criteria->set_exchange("*");
    instrument_criteria->set_instrument_class("spot");
    instrument_criteria->set_code("*");

    request.add_commodities(StreamMarketUpdateCommodity::SMUC_TOP_OF_BOOK);

    // Context for the client. It could be used to convey extra information to
    // the server and/or tweak certain RPC behaviors.
    ClientContext context;
    setupContext(&context);

    std::unique_ptr<ClientReader<StreamMarketUpdateResponseV1>> reader(stub_->Subscribe(&context, request));

    // Container for the data we expect from the server.
    StreamMarketUpdateResponseV1 response;

    while (reader->Read(&response))
    {
      std::cout << response.price() << " / " << response.amount() << std::endl; // price/amount infos
      std::cout << response.update_type() << std::endl; // ASK (3) or BID (4) top of BOOK
      std::cout << response.exchange() << ":" << response.class_() << ":" << response.code() <<  std::endl; // which instrument
      std::cout << response.ts_event().seconds() << " / " << response.ts_collection().value().seconds() <<  std::endl; // various timestamps...
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

int main(int argc, char **argv)
{
  ChannelArguments args;

  grpc::SslCredentialsOptions sslOptions = grpc::SslCredentialsOptions();
  auto channel = grpc::CreateCustomChannel("gateway-v0-grpc.kaiko.ovh:443",
                                           grpc::SslCredentials(sslOptions), args);
  MarketUpdateClient client = MarketUpdateClient(channel);

  std::string reply = client.Subscribe();
  std::cout << "Subscribe received: " << reply << std::endl;

  return 0;
}
