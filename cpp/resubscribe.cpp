#include <iostream>
#include <memory>
#include <string>
#include <sstream>

#include <chrono>
#include <thread>

#include <grpcpp/grpcpp.h>
#include <sdk/sdk.grpc.pb.h>

using grpc::Channel;
using grpc::ChannelArguments;
using grpc::ClientContext;
using grpc::ClientReader;
using grpc::SecureChannelCredentials;
using grpc::Status;
using kaikosdk::InstrumentCriteria;
using kaikosdk::StreamTradesRequestV1;
using kaikosdk::StreamTradesResponseV1;
using kaikosdk::StreamTradesServiceV1;

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

int main(int argc, char **argv)
{
  ChannelArguments args;

  grpc::SslCredentialsOptions sslOptions = grpc::SslCredentialsOptions();
  auto channel = grpc::CreateCustomChannel("gateway-v0-grpc.kaiko.ovh:443",
                                           grpc::SslCredentials(sslOptions), args);
  TradeClient client = TradeClient(channel);

  while (true)
  {
    std::cout << "[TRADES] Stream started" << std::endl;
    std::string reply = client.Subscribe();
    std::cout << "[TRADES] Stream ended" << std::endl;

    // break if status error, otherwise resubscribe
    if (reply.length() > 0)
    {
      std::cout << "Subscribe received: " << reply << std::endl;
      break;
    }

    std::cout << "[TRADES] Resubscribing" << std::endl;
    std::this_thread::sleep_for(std::chrono::seconds(2));
  }

  return 0;
}
