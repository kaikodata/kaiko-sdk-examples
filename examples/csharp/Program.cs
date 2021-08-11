using Grpc.Core;
using KaikoSdk;
using KaikoSdk.Stream.MarketUpdateV1;
using KaikoSdk.Stream.AggregatesOHLCVV1;
using KaikoSdk.Stream.AggregatesVWAPV1;
using KaikoSdk.Stream.AggregatesSpotExchangeRateV1;
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
            var clientt = new StreamTradesServiceV1.StreamTradesServiceV1Client(channel);

            // Setup runtime (run for few seconds or stop after receiving some results)
            var sourcet = new CancellationTokenSource();
            sourcet.CancelAfter(TimeSpan.FromSeconds(5));

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
                var reply = clientt.Subscribe(req, null, null, sourcet.Token);
                var stream = reply.ResponseStream;

                var i = 0;
                while (await stream.MoveNext())
                {
                    var response = stream.Current;
                    Console.WriteLine(response);

                    if (i > 3)
                    {
                        sourcet.Cancel();
                    }

                    i++;
                }
            }
            catch (RpcException e)
            {
                if (e.StatusCode != StatusCode.Cancelled)
                {
                    Console.WriteLine(e);
                }
            }

            var clientmu = new StreamMarketUpdateServiceV1.StreamMarketUpdateServiceV1Client(channel);

            // Setup runtime (run for few seconds or stop after receiving some results)
            var sourcemu = new CancellationTokenSource();
            sourcemu.CancelAfter(TimeSpan.FromSeconds(5));

            // Create a streaming market update request with SDK
            try
            {
                var req = new StreamMarketUpdateRequestV1
                {
                    InstrumentCriteria = new InstrumentCriteria
                    {
                        Code = "*",
                        Exchange = "krkn",
                        InstrumentClass = "spot"
                    },
                    Commodities = {StreamMarketUpdateCommodity.SmucTrade}
                };
                var reply = clientmu.Subscribe(req, null, null, sourcemu.Token);
                var stream = reply.ResponseStream;

                var i = 0;
                while (await stream.MoveNext())
                {
                    var response = stream.Current;
                    Console.WriteLine(response);

                    if (i > 3)
                    {
                        sourcemu.Cancel();
                    }

                    i++;
                }
            }
            catch (RpcException e)
            {
                if (e.StatusCode != StatusCode.Cancelled)
                {
                    Console.WriteLine(e);
                }
            }

            var clientohlcv = new StreamAggregatesOHLCVServiceV1.StreamAggregatesOHLCVServiceV1Client(channel);

            // Setup runtime (run for few seconds or stop after receiving some results)
            var sourceohlcv = new CancellationTokenSource();
            sourceohlcv.CancelAfter(TimeSpan.FromSeconds(5));

            // Create a streaming ohlcv request with SDK
            try
            {
                var req = new StreamAggregatesOHLCVRequestV1
                {
                    InstrumentCriteria = new InstrumentCriteria
                    {
                        Code = "*",
                        Exchange = "cbse",
                        InstrumentClass = "spot"
                    },
                    Aggregate = "1s"
                };
                var reply = clientohlcv.Subscribe(req, null, null, sourceohlcv.Token);
                var stream = reply.ResponseStream;

                var i = 0;
                while (await stream.MoveNext())
                {
                    var response = stream.Current;
                    Console.WriteLine(response);

                    if (i > 3)
                    {
                        sourceohlcv.Cancel();
                    }

                    i++;
                }
            }
            catch (RpcException e)
            {
                if (e.StatusCode != StatusCode.Cancelled)
                {
                    Console.WriteLine(e);
                }
            }

            var clientser = new StreamAggregatesSpotExchangeRateServiceV1.StreamAggregatesSpotExchangeRateServiceV1Client(channel);

            // Setup runtime (run for few seconds or stop after receiving some results)
            var sourceser = new CancellationTokenSource();
            sourceser.CancelAfter(TimeSpan.FromSeconds(5));

            // Create a streaming ser request with SDK
            try
            {
                var req = new StreamAggregatesSpotExchangeRateRequestV1
                {
                    Code = "btc-usd",
                    Aggregate = "1s"
                };
                var reply = clientser.Subscribe(req, null, null, sourceser.Token);
                var stream = reply.ResponseStream;

                var i = 0;
                while (await stream.MoveNext())
                {
                    var response = stream.Current;
                    Console.WriteLine(response);

                    if (i > 3)
                    {
                        sourceser.Cancel();
                    }

                    i++;
                }
            }
            catch (RpcException e)
            {
                if (e.StatusCode != StatusCode.Cancelled)
                {
                    Console.WriteLine(e);
                }
            }

            var clientvwap = new StreamAggregatesVWAPServiceV1.StreamAggregatesVWAPServiceV1Client(channel);

            // Setup runtime (run for few seconds or stop after receiving some results)
            var sourcevwap = new CancellationTokenSource();
            sourcevwap.CancelAfter(TimeSpan.FromSeconds(5));

            // Create a streaming vwap request with SDK
            try
            {
                var req = new StreamAggregatesVWAPRequestV1
                {
                    InstrumentCriteria = new InstrumentCriteria
                    {
                        Code = "*",
                        Exchange = "bnce",
                        InstrumentClass = "spot"
                    },
                    Aggregate = "1s"
                };
                var reply = clientvwap.Subscribe(req, null, null, sourcevwap.Token);
                var stream = reply.ResponseStream;

                var i = 0;
                while (await stream.MoveNext())
                {
                    var response = stream.Current;
                    Console.WriteLine(response);

                    if (i > 3)
                    {
                        sourcevwap.Cancel();
                    }

                    i++;
                }
            }
            catch (RpcException e)
            {
                if (e.StatusCode != StatusCode.Cancelled)
                {
                    Console.WriteLine(e);
                }
            }

            channel.ShutdownAsync().Wait();
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
