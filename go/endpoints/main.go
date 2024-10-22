// Example use of Kaiko SDK.
package main

import (
	"context"
	"fmt"
	"io"
	"log"
	"os"
	"time"

	pb "github.com/kaikodata/kaiko-go-sdk"
	"github.com/kaikodata/kaiko-go-sdk/stream/index_v1"
	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials/insecure"
	"google.golang.org/grpc/keepalive"
	"google.golang.org/grpc/metadata"
)

func main() {
	ctx, cancel := context.WithCancel(context.Background())
	defer cancel()

	t := time.NewTicker(10 * time.Second)
	go func() {
		defer t.Stop()

		for {
			select {
			case <-ctx.Done():
				return
			case ts := <-t.C:
				fmt.Println(ts)
			}
		}
	}()

	t100 := time.NewTicker(100 * time.Second)
	go func() {
		defer t100.Stop()

		for {
			select {
			case <-ctx.Done():
				return
			case ts := <-t100.C:
				fmt.Println(ts)
			}
		}
	}()

	// GODEBUG=http2debug=2 GRPC_GO_LOG_VERBOSITY_LEVEL=99 GRPC_GO_LOG_SEVERITY_LEVEL=info IP_DIRECT="<ip>" go run main.go

	ipDirect := getEnv("IP_DIRECT", "")

	// Set up a connection to the server.
	// WARNING: reconnection is automatically handled by client, you should never have
	// 2 connections at the same time otherwise you're likely to leak connections.
	conn, err := grpc.NewClient(
		// "gateway-v0-grpc.kaiko.ovh:443",
		ipDirect,
		// grpc.WithTransportCredentials(credentials.NewTLS(&tls.Config{})),
		grpc.WithTransportCredentials(insecure.NewCredentials()),
		grpc.WithKeepaliveParams(keepalive.ClientParameters{
			Time:                10 * time.Second, // send pings every 10 seconds if there is no activity
			Timeout:             time.Second,      // wait 1 second for ping ack before considering the connection dead
			PermitWithoutStream: false,            // send pings even without active streams
		}),
	)
	if err != nil {
		log.Fatalf("could not connect: %v", err)
	}
	defer conn.Close()

	// Setup authentication
	apiKey := getEnv("KAIKO_API_KEY", "1234") // Put your api key here
	ctx = metadata.AppendToOutgoingContext(ctx, "authorization", "Bearer "+apiKey)

	// timeout := 30 * time.Second // demo timeout
	// ctx, cancel = context.WithTimeout(ctx, timeout)
	// defer cancel()

	// go func() {
	// 	// Create a streaming market update request with SDK
	// 	err := marketUpdateRequest(ctx, conn)
	// 	if err != nil {
	// 		log.Fatalf("could not get market updates: %v", err)
	// 	}
	// }()

	// go func() {
	// 	// Create a streaming ohlcv request with SDK
	// 	err := ohlcvRequest(ctx, conn)
	// 	if err != nil {
	// 		log.Fatalf("could not get ohlcvs: %v", err)
	// 	}
	// }()

	// go func() {
	// 	// Create a streaming vwap request with SDK
	// 	err := vwapRequest(ctx, conn)
	// 	if err != nil {
	// 		log.Fatalf("could not get vwaps: %v", err)
	// 	}
	// }()

	// go func() {
	// Create a streaming index request with SDK
	err = indexRequest(ctx, conn)
	if err != nil {
		log.Printf("could not get index: %v", err)
	}
	// <-ctx.Done()
	// }()

	// go func() {
	// 	// Create a streaming multi_asset index request with SDK
	// 	err := indexMultiAssetRequest(ctx, conn)
	// 	if err != nil {
	// 		log.Printf("could not get index: %v", err)
	// 	}
	// }()

	// go func() {
	// 	// Create a streaming forex rate index request with SDK
	// 	err := indexForexRateRequest(ctx, conn)
	// 	if err != nil {
	// 		log.Printf("could not get index: %v", err)
	// 	}
	// }()

	// go func() {
	// 	// Create a streaming aggregated quote request with SDK
	// 	err := aggregatedQuoteRequest(ctx, conn)
	// 	if err != nil {
	// 		log.Printf("could not get aggregated quote: %v", err)
	// 	}
	// }()

	// go func() {
	// 	// Create a streaming aggregated spot exchange rate request with SDK
	// 	err := aggregatesSpotExchangeRateV2Request(ctx, conn)
	// 	if err != nil {
	// 		log.Printf("could not get spot exchange rate: %v", err)
	// 	}
	// }()

	// go func() {
	// 	// Create a streaming aggregated spot direct exchange rate request with SDK
	// 	err := aggregatesSpotDirectExchangeRateV2Request(ctx, conn)
	// 	if err != nil {
	// 		log.Printf("could not get spot direct exchange rate: %v", err)
	// 	}
	// }()

	// go func() {
	// 	// Create a streaming derivatives instrument metrics request with SDK
	// 	err := derivativesInstrumentMetricsRequest(ctx, conn)
	// 	if err != nil {
	// 		log.Printf("could not get derivatives instrument metrics: %v", err)
	// 	}
	// }()

	// go func() {
	// 	// Create a streaming iv svi parameters request with SDK
	// 	err := ivSviParametersRequest(ctx, conn)
	// 	if err != nil {
	// 		log.Printf("could not get iv svi parameters: %v", err)
	// 	}
	// }()

	// // Create a streaming trades request with SDK
	// err = tradesRequest(ctx, conn)
	// if err != nil {
	// 	log.Fatalf("could not get trades: %v", err)
	// }
}

func getEnv(key, fallback string) string {
	if value, ok := os.LookupEnv(key); ok {
		return value
	}
	return fallback
}

// func ohlcvRequest(
// 	ctx context.Context,
// 	conn *grpc.ClientConn,
// ) error {
// 	cli := pb.NewStreamAggregatesOHLCVServiceV1Client(conn)
// 	request := aggregates_ohlcv_v1.StreamAggregatesOHLCVRequestV1{
// 		InstrumentCriteria: &core.InstrumentCriteria{
// 			Exchange:        "cbse",
// 			InstrumentClass: "spot",
// 			Code:            "btc-usd",
// 		},
// 		Aggregate: "1s",
// 	}

// 	sub, err := cli.Subscribe(ctx, &request)
// 	if err != nil {
// 		log.Fatalf("could not subscribe: %v", err)
// 	}

// 	for {
// 		elt, err := sub.Recv()
// 		if err == io.EOF {
// 			return nil
// 		}

// 		if err != nil {
// 			return err
// 		}

// 		fmt.Printf("[OHLCV] %+v\n", elt)
// 	}
// }

// func vwapRequest(
// 	ctx context.Context,
// 	conn *grpc.ClientConn,
// ) error {
// 	cli := pb.NewStreamAggregatesVWAPServiceV1Client(conn)
// 	request := aggregates_vwap_v1.StreamAggregatesVWAPRequestV1{
// 		InstrumentCriteria: &core.InstrumentCriteria{
// 			Exchange:        "cbse",
// 			InstrumentClass: "spot",
// 			Code:            "btc-usd",
// 		},
// 		Aggregate: "1s",
// 	}

// 	sub, err := cli.Subscribe(ctx, &request)
// 	if err != nil {
// 		log.Fatalf("could not subscribe: %v", err)
// 	}

// 	for {
// 		elt, err := sub.Recv()
// 		if err == io.EOF {
// 			return nil
// 		}

// 		if err != nil {
// 			return err
// 		}

// 		fmt.Printf("[VWAP] %+v\n", elt)
// 	}
// }

// func marketUpdateRequest(
// 	ctx context.Context,
// 	conn *grpc.ClientConn,
// ) error {
// 	cli := pb.NewStreamMarketUpdateServiceV1Client(conn)
// 	// Globbing patterns are also supported on all fields. See http://sdk.kaiko.com/#instrument-selection for all supported patterns
// 	request := market_update_v1.StreamMarketUpdateRequestV1{
// 		InstrumentCriteria: &core.InstrumentCriteria{
// 			Exchange:        "cbse",
// 			InstrumentClass: "spot",
// 			Code:            "btc-usd",
// 		},
// 		Commodities: []market_update_v1.StreamMarketUpdateCommodity{market_update_v1.StreamMarketUpdateCommodity_SMUC_TRADE},
// 	}

// 	sub, err := cli.Subscribe(ctx, &request)
// 	if err != nil {
// 		log.Fatalf("could not subscribe: %v", err)
// 	}

// 	for {
// 		elt, err := sub.Recv()
// 		if err == io.EOF {
// 			return nil
// 		}

// 		if err != nil {
// 			return err
// 		}

// 		fmt.Printf("[MARKET UPDATE] %+v\n", elt)
// 	}
// }

// func tradesRequest(
// 	ctx context.Context,
// 	conn *grpc.ClientConn,
// ) error {
// 	cli := pb.NewStreamTradesServiceV1Client(conn)
// 	// Globbing patterns are also supported on all fields. See http://sdk.kaiko.com/#instrument-selection for all supported patterns
// 	request := trades_v1.StreamTradesRequestV1{
// 		InstrumentCriteria: &core.InstrumentCriteria{
// 			Exchange:        "cbse",
// 			InstrumentClass: "spot",
// 			Code:            "btc-usd",
// 		},
// 	}

// 	sub, err := cli.Subscribe(ctx, &request)
// 	if err != nil {
// 		log.Fatalf("could not subscribe: %v", err)
// 	}

// 	for {
// 		elt, err := sub.Recv()
// 		if err == io.EOF {
// 			return nil
// 		}

// 		if err != nil {
// 			return err
// 		}

// 		fmt.Printf("[TRADE] %+v\n", elt)
// 	}
// }

func indexRequest(
	ctx context.Context,
	conn *grpc.ClientConn,
) error {
	cli := pb.NewStreamIndexServiceV1Client(conn)
	request := index_v1.StreamIndexServiceRequestV1{
		IndexCode: "KK_BRR_BTCUSD",
	}

	sub, err := cli.Subscribe(ctx, &request)
	if err != nil {
		log.Fatalf("could not subscribe: %v", err)
	}

	for {
		elt, err := sub.Recv()
		if err == io.EOF {
			return nil
		}

		if err != nil {
			return err
		}

		fmt.Printf("[INDEX] %+v\n", elt)
	}
}

// func indexMultiAssetRequest(
// 	ctx context.Context,
// 	conn *grpc.ClientConn,
// ) error {
// 	cli := pb.NewStreamIndexMultiAssetsServiceV1Client(conn)
// 	request := index_multi_assets_v1.StreamIndexMultiAssetsServiceRequestV1{
// 		IndexCode: "KT15",
// 	}

// 	sub, err := cli.Subscribe(ctx, &request)
// 	if err != nil {
// 		log.Fatalf("could not subscribe: %v", err)
// 	}

// 	for {
// 		elt, err := sub.Recv()
// 		if err == io.EOF {
// 			return nil
// 		}

// 		if err != nil {
// 			return err
// 		}

// 		fmt.Printf("[INDEX_MULTI_ASSET] %+v\n", elt)
// 	}
// }

// func indexForexRateRequest(
// 	ctx context.Context,
// 	conn *grpc.ClientConn,
// ) error {
// 	cli := pb.NewStreamIndexForexRateServiceV1Client(conn)
// 	request := index_forex_rate_v1.StreamIndexForexRateServiceRequestV1{
// 		IndexCode: "KK_BRR_BTCUSD_EUR",
// 	}

// 	sub, err := cli.Subscribe(ctx, &request)
// 	if err != nil {
// 		log.Fatalf("could not subscribe: %v", err)
// 	}

// 	for {
// 		elt, err := sub.Recv()
// 		if err == io.EOF {
// 			return nil
// 		}

// 		if err != nil {
// 			return err
// 		}

// 		fmt.Printf("[INDEX_FOREX_RATE] %+v\n", elt)
// 	}
// }

// func aggregatedQuoteRequest(
// 	ctx context.Context,
// 	conn *grpc.ClientConn,
// ) error {
// 	cli := pb.NewStreamAggregatedQuoteServiceV2Client(conn)
// 	// Globbing patterns are also supported on all fields. See http://sdk.kaiko.com/#instrument-selection for all supported patterns
// 	request := aggregated_quote_v2.StreamAggregatedQuoteRequestV2{
// 		InstrumentClass: "spot",
// 		Code:            "btc-usd",
// 	}

// 	sub, err := cli.Subscribe(ctx, &request)
// 	if err != nil {
// 		log.Fatalf("could not subscribe: %v", err)
// 	}

// 	for {
// 		elt, err := sub.Recv()
// 		if err == io.EOF {
// 			return nil
// 		}

// 		if err != nil {
// 			return err
// 		}

// 		fmt.Printf("[AGGREGATED QUOTE] %+v\n", elt)
// 	}
// }

// func aggregatesSpotExchangeRateV2Request(
// 	ctx context.Context,
// 	conn *grpc.ClientConn,
// ) error {
// 	cli := pb.NewStreamAggregatesSpotExchangeRateV2ServiceV1Client(conn)
// 	request := aggregates_spot_exchange_rate_v2.StreamAggregatesSpotExchangeRateV2RequestV1{
// 		Assets: &core.Assets{
// 			Base:  "btc",
// 			Quote: "usd",
// 		},
// 		Window:          durationpb.New(10 * time.Second),
// 		UpdateFrequency: durationpb.New(2 * time.Second),
// 	}

// 	sub, err := cli.Subscribe(ctx, &request)
// 	if err != nil {
// 		log.Fatalf("could not subscribe: %v", err)
// 	}

// 	for {
// 		elt, err := sub.Recv()
// 		if err == io.EOF {
// 			return nil
// 		}

// 		if err != nil {
// 			return err
// 		}

// 		fmt.Printf("[SPOT EXCHANGE RATE] %+v\n", elt)
// 	}
// }

// func aggregatesSpotDirectExchangeRateV2Request(
// 	ctx context.Context,
// 	conn *grpc.ClientConn,
// ) error {
// 	cli := pb.NewStreamAggregatesSpotDirectExchangeRateV2ServiceV1Client(conn)
// 	request := aggregates_direct_exchange_rate_v2.StreamAggregatesDirectExchangeRateV2RequestV1{
// 		Assets: &core.Assets{
// 			Base:  "btc",
// 			Quote: "usd",
// 		},
// 		Window:          durationpb.New(10 * time.Second),
// 		UpdateFrequency: durationpb.New(2 * time.Second),
// 	}

// 	sub, err := cli.Subscribe(ctx, &request)
// 	if err != nil {
// 		log.Fatalf("could not subscribe: %v", err)
// 	}

// 	for {
// 		elt, err := sub.Recv()
// 		if err == io.EOF {
// 			return nil
// 		}

// 		if err != nil {
// 			return err
// 		}

// 		fmt.Printf("[SPOT DIRECT EXCHANGE RATE] %+v\n", elt)
// 	}
// }

// func derivativesInstrumentMetricsRequest(
// 	ctx context.Context,
// 	conn *grpc.ClientConn,
// ) error {
// 	cli := pb.NewStreamDerivativesInstrumentMetricsServiceV1Client(conn)
// 	request := derivatives_instrument_metrics_v1.StreamDerivativesInstrumentMetricsRequestV1{
// 		InstrumentCriteria: &core.InstrumentCriteria{
// 			Exchange:        "*",
// 			InstrumentClass: "perpetual-future",
// 			Code:            "btc-usd",
// 		},
// 	}

// 	sub, err := cli.Subscribe(ctx, &request)
// 	if err != nil {
// 		log.Fatalf("could not subscribe: %v", err)
// 	}

// 	for {
// 		elt, err := sub.Recv()
// 		if err == io.EOF {
// 			return nil
// 		}

// 		if err != nil {
// 			return err
// 		}

// 		fmt.Printf("[DERIVATIVES INSTRUMENT METRICS] %+v\n", elt)
// 	}
// }

// func ivSviParametersRequest(
// 	ctx context.Context,
// 	conn *grpc.ClientConn,
// ) error {
// 	cli := pb.NewStreamIvSviParametersServiceV1Client(conn)
// 	request := iv_svi_parameters_v1.StreamIvSviParametersRequestV1{
// 		Assets: &core.Assets{
// 			Base:  "btc",
// 			Quote: "usd",
// 		},
// 		Exchanges: "drbt",
// 	}

// 	sub, err := cli.Subscribe(ctx, &request)
// 	if err != nil {
// 		log.Fatalf("could not subscribe: %v", err)
// 	}

// 	for {
// 		elt, err := sub.Recv()
// 		if err == io.EOF {
// 			return nil
// 		}

// 		if err != nil {
// 			return err
// 		}

// 		fmt.Printf("[IV SVI PARAMETERS] %+v\n", elt)
// 	}
// }
