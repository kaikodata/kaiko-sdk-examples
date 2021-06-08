using Grpc.Core;
using KaikoSdk;
using KaikoSdk.Stream.TradesV1;
using KaikoSdk.Core;
using System;
using System.Threading;
using System.Threading.Tasks;

namespace TestSdk
{
    class Program
    {
        static async Task Main(string[] args)
        {
            // Setup authentication
            var pass = Environment.GetEnvironmentVariable("KAIKO_API_KEY") ?? "1234"; // Put your api key here
            var secure = new SslCredentials();
            Channel channel = new Channel("gateway-v0-grpc.kaiko.ovh", Program.CreateAuthenticatedChannel(secure, pass));
            var client = new StreamTradesServiceV1.StreamTradesServiceV1Client(channel);

            // Setup runtime (run for few seconds or stop after receiving some results)
            var source = new CancellationTokenSource();
            source.CancelAfter(TimeSpan.FromSeconds(5));

            // Create a streaming trades request with SDK
            try
            {
                var req = new StreamTradesRequestV1
                {
                    InstrumentCriteria = new InstrumentCriteria
                    {
                        Code = "btc-usd",
                        Exchange = "cbse",
                        InstrumentClass = "spot"
                    }
                };
                var reply = client.Subscribe(req, null, null, source.Token);
                var stream = reply.ResponseStream;

                var i = 0;
                while (await stream.MoveNext())
                {
                    var response = stream.Current;
                    Console.WriteLine(response);

                    if (i > 3)
                    {
                        source.Cancel();
                    }

                    i++;
                }
                channel.ShutdownAsync().Wait();
            }
            catch (RpcException e)
            {
                if (e.StatusCode != StatusCode.Cancelled)
                {
                    Console.WriteLine(e);
                }
            }

        }

        private static ChannelCredentials CreateAuthenticatedChannel(ChannelCredentials creds, string token)
        {
            var interceptor = CallCredentials.FromInterceptor((context, metadata) =>
            {
                if (!string.IsNullOrEmpty(token))
                {
                    metadata.Add("Authorization", $"Bearer {token}");
                }
                return Task.CompletedTask;
            });

            return ChannelCredentials.Create(creds, interceptor);
        }
    }
}
