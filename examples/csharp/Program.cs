using Grpc.Net.Client;
using Grpc.Core;
using KaikoSdk;
using KaikoSdk.Stream.MarketUpdateV1;
using KaikoSdk.Stream.AggregatesOHLCVV1;
using KaikoSdk.Stream.AggregatesVWAPV1;
using KaikoSdk.Stream.AggregatesDirectExchangeRateV1;
using KaikoSdk.Stream.AggregatesSpotExchangeRateV1;
using KaikoSdk.Stream.AggregatedPriceV1;
using KaikoSdk.Stream.IndexV1;
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

            var channelOptions = new GrpcChannelOptions { Credentials = Program.CreateAuthenticatedChannel(pass) };
            GrpcChannel channel = GrpcChannel.ForAddress("https://gateway-v0-grpc.kaiko.ovh", channelOptions);

            // trades
            await Program.tradesRequest(channel);

            // market update
            await Program.marketUpdateRequest(channel);

            // ohlcv
            await Program.ohlcvRequest(channel);

            // vwap
            await Program.vwapRequest(channel);

            // direct exchange rate
            await Program.directExchangeRateRequest(channel);

            // spot exchange rate
            await Program.spotExchangeRateRequest(channel);

            // index
            await Program.indicesRequest(channel);

            // aggregated quote
            await Program.aggregatedQuoteRequest(channel);

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

        private static async Task directExchangeRateRequest(GrpcChannel channel)
        {
            var clientder = new StreamAggregatesDirectExchangeRateServiceV1.StreamAggregatesDirectExchangeRateServiceV1Client(channel);

            // Setup runtime (run for few seconds or stop after receiving some results)
            var sourceser = new CancellationTokenSource();
            sourceser.CancelAfter(TimeSpan.FromSeconds(5));

            // Create a streaming ser request with SDK
            try
            {
                var req = new StreamAggregatesDirectExchangeRateRequestV1
                {
                    Code = "btc-usd",
                    Aggregate = "1s"
                };
                var reply = clientder.Subscribe(req, null, null, sourceser.Token);
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
        }

        private static async Task spotExchangeRateRequest(GrpcChannel channel)
        {
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
        }

        private static async Task indicesRequest(GrpcChannel channel)
        {
            var clientindex = new StreamIndexServiceV1.StreamIndexServiceV1Client(channel);

            // Setup runtime (run for few seconds or stop after receiving some results)
            var sourceindex = new CancellationTokenSource();
            sourceindex.CancelAfter(TimeSpan.FromSeconds(5));

            // Create a streaming index request with SDK
            try
            {
                var req = new StreamIndexServiceRequestV1
                {
                    IndexCode = "indexCode", // fill it with actual value
                };
                var reply = clientindex.Subscribe(req, null, null, sourceindex.Token);
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
            var clientaq = new StreamAggregatedPriceServiceV1.StreamAggregatedPriceServiceV1Client(channel);

            // Setup runtime (run for few seconds or stop after receiving some results)
            var sourcet = new CancellationTokenSource();
            sourcet.CancelAfter(TimeSpan.FromSeconds(20));

            // Create a streaming aggregated quote request with SDK
            try
            {
                // Globbing patterns are also supported on all fields. See http://sdk.kaiko.com/#instrument-selection for all supported patterns
                var req = new StreamAggregatedPriceRequestV1
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
    }
}


