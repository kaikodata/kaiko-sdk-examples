using Grpc.Net.Client;
using Grpc.Core;
using KaikoSdk;
using KaikoSdk.Stream.TradesV1;
using KaikoSdk.Core;
using System;
using System.Threading.Tasks;
using Polly;


// Run with `dotnet run -p:StartupObject=TestSdk.Resubscribe`
namespace TestSdk
{
    class Resubscribe
    {
        static async Task Main(string[] args)
        {
            // Setup authentication
            var pass = Environment.GetEnvironmentVariable("KAIKO_API_KEY") ?? "1234"; // Put your api key here

            var channelOptions = new GrpcChannelOptions { Credentials = Resubscribe.CreateAuthenticatedChannel(pass) };
            GrpcChannel channel = GrpcChannel.ForAddress("https://gateway-v0-grpc.kaiko.ovh", channelOptions);

            // trades
            await Resubscribe.tradesRequest(channel);

            channel.ShutdownAsync().Wait();
        }

        private static ChannelCredentials CreateAuthenticatedChannel(string token)
        {
            var interceptor = CallCredentials.FromInterceptor((context, metadata) =>
            {
                if (!string.IsNullOrEmpty(token))
                {
                    metadata.Add("Authorization", $"Bearer {token}");
                }
                return Task.CompletedTask;
            });

            return ChannelCredentials.Create(new SslCredentials(), interceptor);
        }

        private static async Task tradesRequest(GrpcChannel channel)
        {
            var clientt = new StreamTradesServiceV1.StreamTradesServiceV1Client(channel);

            // Create a streaming trades request with SDK
            var req = new StreamTradesRequestV1
            {
                // Globbing patterns are also supported on all fields. See http://sdk.kaiko.com/#instrument-selection for all supported patterns
                InstrumentCriteria = new InstrumentCriteria
                {
                    Code = "*",
                    Exchange = "cbse",
                    InstrumentClass = "spot"
                }
            };

            var polly = Policy
            .HandleResult<RpcException>((e) => true)
            .WaitAndRetryForeverAsync((retryAttempt) =>
            {
                Console.WriteLine("[TRADES] Resubscribing");

                return TimeSpan.FromSeconds(2);
            });

            await polly.ExecuteAsync(() => subscribe(clientt, req));
        }

        private static async Task<RpcException> subscribe(StreamTradesServiceV1.StreamTradesServiceV1Client client, StreamTradesRequestV1 req)
        {
            Console.WriteLine("[TRADES] Stream started");

            // Add logic to be executed before each retry, such as logging  
            var reply = client.Subscribe(req);
            var stream = reply.ResponseStream;

            while (await stream.MoveNext())
            {
                var response = stream.Current;
                Console.WriteLine(response);
            }

            Console.WriteLine("[TRADES] Stream ended");

            return new RpcException(Status.DefaultCancelled);
        }
    }
}


