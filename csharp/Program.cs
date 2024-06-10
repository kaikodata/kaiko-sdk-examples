using Grpc.Net.Client;
using Grpc.Core;
using KaikoSdk;
using KaikoSdk.Stream.MarketUpdateV1;
using KaikoSdk.Stream.AggregatesOHLCVV1;
using KaikoSdk.Stream.AggregatesVWAPV1;
using KaikoSdk.Stream.AggregatedQuoteV2;
using KaikoSdk.Stream.IndexV1;
using KaikoSdk.Stream.IndexMultiAssetsV1;
using KaikoSdk.Stream.IndexForexRateV1;
using KaikoSdk.Stream.TradesV1;
using KaikoSdk.Core;
using System;
using System.Threading;
using System.Threading.Tasks;
using KaikoSdk.Stream.AggregatesSpotExchangeRateV2;
using Google.Protobuf.WellKnownTypes;

// Run with `dotnet run -p:StartupObject=TestSdk.Program` or `dotnet run`
namespace TestSdk
{
    class Program
    {
        static async Task Main(string[] args)
        {
            // Setup authentication
            var pass = Environment.GetEnvironmentVariable("KAIKO_API_KEY") ?? "1234"; // Put your api key here

            var channelOptions = new GrpcChannelOptions { Credentials = Program.CreateAuthenticatedChannel(pass) };
            GrpcChannel channel = GrpcChannel.ForAddress("https://gateway-v0-grpc.kaiko.ovh", channelOptions);

            // trades
            await tradesRequest(channel);

            // market update
            await marketUpdateRequest(channel);

            // ohlcv
            await ohlcvRequest(channel);

            // vwap
            await vwapRequest(channel);

            // index rates
            await indexRatesRequest(channel);

            // index multi asset
            await indexMultiAssetRequest(channel);

            // index forex rate
            await indexForexRateRequest(channel);

            // aggregated quote
            await aggregatedQuoteRequest(channel);

            // spot exchange rate
            await aggregatesSpotExchangeRateRequest(channel);

            // spot direct exchange rate
            await aggregatesSpotDirectExchangeRateRequest(channel);

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

            // Setup runtime (run for few seconds or stop after receiving some results)
            var sourcet = new CancellationTokenSource();
            sourcet.CancelAfter(TimeSpan.FromSeconds(5));

            // Create a streaming trades request with SDK
            try
            {
                var req = new StreamTradesRequestV1
                {
                    // Globbing patterns are also supported on all fields. See http://sdk.kaiko.com/#instrument-selection for all supported patterns
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
        }

        private static async Task marketUpdateRequest(GrpcChannel channel)
        {
            var clientmu = new StreamMarketUpdateServiceV1.StreamMarketUpdateServiceV1Client(channel);

            // Setup runtime (run for few seconds or stop after receiving some results)
            var sourcemu = new CancellationTokenSource();
            sourcemu.CancelAfter(TimeSpan.FromSeconds(5));

            // Create a streaming market update request with SDK
            try
            {
                var req = new StreamMarketUpdateRequestV1
                {
                    // Globbing patterns are also supported on all fields. See http://sdk.kaiko.com/#instrument-selection for all supported patterns
                    InstrumentCriteria = new InstrumentCriteria
                    {
                        Code = "*",
                        Exchange = "krkn",
                        InstrumentClass = "spot"
                    },
                    Commodities = { StreamMarketUpdateCommodity.SmucTrade }
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
        }

        private static async Task ohlcvRequest(GrpcChannel channel)
        {
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
        }

        private static async Task vwapRequest(GrpcChannel channel)
        {
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
                        Exchange = "binc",
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
        }

        private static async Task indexRatesRequest(GrpcChannel channel)
        {
            var client = new StreamIndexServiceV1.StreamIndexServiceV1Client(channel);

            // Setup runtime (run for few seconds or stop after receiving some results)
            var sourceindex = new CancellationTokenSource();
            sourceindex.CancelAfter(TimeSpan.FromSeconds(5));

            // Create a streaming index request with SDK
            try
            {
                var req = new StreamIndexServiceRequestV1
                {
                    IndexCode = "KK_PR_BTCUSD",
                };
                var reply = client.Subscribe(req, null, null, sourceindex.Token);
                var stream = reply.ResponseStream;

                var i = 0;
                while (await stream.MoveNext())
                {
                    var response = stream.Current;
                    Console.WriteLine(response);

                    if (i > 3)
                    {
                        sourceindex.Cancel();
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
        }

        private static async Task indexMultiAssetRequest(GrpcChannel channel)
        {
            var client = new StreamIndexMultiAssetsServiceV1.StreamIndexMultiAssetsServiceV1Client(channel);

            // Setup runtime (run for few seconds or stop after receiving some results)
            var sourceindex = new CancellationTokenSource();
            sourceindex.CancelAfter(TimeSpan.FromSeconds(5));

            // Create a streaming index request with SDK
            try
            {
                var req = new StreamIndexMultiAssetsServiceRequestV1
                {
                    IndexCode = "KT15",
                };
                var reply = client.Subscribe(req, null, null, sourceindex.Token);
                var stream = reply.ResponseStream;

                var i = 0;
                while (await stream.MoveNext())
                {
                    var response = stream.Current;
                    Console.WriteLine(response);

                    if (i > 3)
                    {
                        sourceindex.Cancel();
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
        }

        private static async Task indexForexRateRequest(GrpcChannel channel)
        {
            var client = new StreamIndexForexRateServiceV1.StreamIndexForexRateServiceV1Client(channel);

            // Setup runtime (run for few seconds or stop after receiving some results)
            var sourceindex = new CancellationTokenSource();
            sourceindex.CancelAfter(TimeSpan.FromSeconds(5));

            // Create a streaming index request with SDK
            try
            {
                var req = new StreamIndexForexRateServiceRequestV1
                {
                    IndexCode = "KK_PR_BTCUSD_EUR",
                };
                var reply = client.Subscribe(req, null, null, sourceindex.Token);
                var stream = reply.ResponseStream;

                var i = 0;
                while (await stream.MoveNext())
                {
                    var response = stream.Current;
                    Console.WriteLine(response);

                    if (i > 3)
                    {
                        sourceindex.Cancel();
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
        }

        private static async Task aggregatedQuoteRequest(GrpcChannel channel)
        {
            var clientaq = new StreamAggregatedQuoteServiceV2.StreamAggregatedQuoteServiceV2Client(channel);

            // Setup runtime (run for few seconds or stop after receiving some results)
            var sourcet = new CancellationTokenSource();
            sourcet.CancelAfter(TimeSpan.FromSeconds(20));

            // Create a streaming aggregated quote request with SDK
            try
            {
                // Globbing patterns are also supported on all fields. See http://sdk.kaiko.com/#instrument-selection for all supported patterns
                var req = new StreamAggregatedQuoteRequestV2
                {
                    InstrumentClass = "spot",
                    Code = "btc-usd"
                };
                var reply = clientaq.Subscribe(req, null, null, sourcet.Token);
                var stream = reply.ResponseStream;

                var i = 0;
                while (await stream.MoveNext())
                {
                    var response = stream.Current;
                    Console.WriteLine(response);

                    if (i > 4)
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
        }

        private static async Task aggregatesSpotExchangeRateRequest(GrpcChannel channel)
        {
            var clientaq = new StreamAggregatesSpotExchangeRateV2ServiceV1.StreamAggregatesSpotExchangeRateV2ServiceV1Client(channel);

            // Setup runtime (run for few seconds or stop after receiving some results)
            var sourcet = new CancellationTokenSource();
            sourcet.CancelAfter(TimeSpan.FromSeconds(20));

            // Create a streaming spot exchange rate request with SDK
            try
            {
                var req = new StreamAggregatesSpotExchangeRateV2RequestV1
                {
                    Assets = new Assets { Base = "btc", Quote = "usd" },
                    Window = Duration.FromTimeSpan(TimeSpan.FromSeconds(10)),
                    UpdateFrequency = Duration.FromTimeSpan(TimeSpan.FromSeconds(2))
                };
                var reply = clientaq.Subscribe(req, null, null, sourcet.Token);
                var stream = reply.ResponseStream;

                var i = 0;
                while (await stream.MoveNext())
                {
                    var response = stream.Current;
                    Console.WriteLine(response);

                    if (i > 4)
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
        }

        private static async Task aggregatesSpotDirectExchangeRateRequest(GrpcChannel channel)
        {
            var clientaq = new StreamAggregatesSpotExchangeRateV2ServiceV1.StreamAggregatesSpotExchangeRateV2ServiceV1Client(channel);

            // Setup runtime (run for few seconds or stop after receiving some results)
            var sourcet = new CancellationTokenSource();
            sourcet.CancelAfter(TimeSpan.FromSeconds(20));

            // Create a streaming spot direct exchange rate request with SDK
            try
            {
                var req = new StreamAggregatesSpotExchangeRateV2RequestV1
                {
                    Assets = new Assets { Base = "btc", Quote = "usd" },
                    Window = Duration.FromTimeSpan(TimeSpan.FromSeconds(10)),
                    UpdateFrequency = Duration.FromTimeSpan(TimeSpan.FromSeconds(2))
                };
                var reply = clientaq.Subscribe(req, null, null, sourcet.Token);
                var stream = reply.ResponseStream;

                var i = 0;
                while (await stream.MoveNext())
                {
                    var response = stream.Current;
                    Console.WriteLine(response);

                    if (i > 4)
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
        }
    }
}


