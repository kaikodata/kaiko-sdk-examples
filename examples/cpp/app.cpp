#include <iostream>
#include <memory>
#include <string>
#include <sstream>

#include <grpcpp/grpcpp.h>
#include <sdk/sdk.grpc.pb.h>

using google::protobuf::Timestamp;
using grpc::Channel;
using grpc::ChannelArguments;
using grpc::ClientContext;
using grpc::ClientReader;
using grpc::SecureChannelCredentials;
using grpc::Status;
using kaikosdk::InstrumentCriteria;
using kaikosdk::DataInterval;
using kaikosdk::StreamMarketUpdateRequestV1;
using kaikosdk::StreamMarketUpdateResponseV1;
using kaikosdk::StreamMarketUpdateServiceV1;

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
    instrument_criteria->set_instrument_class("*");
    instrument_criteria->set_code("*");

    Timestamp start = google::protobuf::Timestamp();
    start.set_seconds(1660608000);

    Timestamp end = google::protobuf::Timestamp();
    end.set_seconds(1660694400);

    DataInterval* interval = request.mutable_interval();
    interval->set_allocated_start_time(&start);
    interval->set_allocated_end_time(&end);

    // Context for the client. It could be used to convey extra information to
    // the server and/or tweak certain RPC behaviors.
    ClientContext context;
    setupContext(&context);

    std::unique_ptr<ClientReader<StreamMarketUpdateResponseV1>> reader(stub_->Subscribe(&context, request));

    // Container for the data we expect from the server.
    StreamMarketUpdateResponseV1 response;

    while (reader->Read(&response))
    {
      // std::cout << response.DebugString() << std::endl;
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
