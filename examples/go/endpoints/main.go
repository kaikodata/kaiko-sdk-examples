// Example use of Kaiko SDK.
package main

import (
	"context"
	"crypto/tls"
	"fmt"
	"io"
	"log"
	"os"
	"time"

	pb "github.com/kaikodata/kaiko-go-sdk"
	"github.com/kaikodata/kaiko-go-sdk/core"
	"github.com/kaikodata/kaiko-go-sdk/stream/aggregated_price_v1"
	"github.com/kaikodata/kaiko-go-sdk/stream/aggregates_direct_exchange_rate_v1"
	"github.com/kaikodata/kaiko-go-sdk/stream/aggregates_ohlcv_v1"
	"github.com/kaikodata/kaiko-go-sdk/stream/aggregates_spot_exchange_rate_v1"
	"github.com/kaikodata/kaiko-go-sdk/stream/aggregates_vwap_v1"
	"github.com/kaikodata/kaiko-go-sdk/stream/derivatives_price_v2"
	"github.com/kaikodata/kaiko-go-sdk/stream/index_v1"
	"github.com/kaikodata/kaiko-go-sdk/stream/market_update_v1"
	"github.com/kaikodata/kaiko-go-sdk/stream/trades_v1"
	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials"
	"google.golang.org/grpc/metadata"
)

func main() {
	ctx, cancel := context.WithCancel(context.Background())
	defer cancel()

	// Set up a connection to the server.
	// WARNING: reconnection is automatically handled by client, you should never have
	// 2 connections at the same time otherwise you're likely to leak connections.
	conn, err := grpc.Dial("gateway-v0-grpc.kaiko.ovh:443", grpc.WithTransportCredentials(credentials.NewTLS(&tls.Config{})))
	if err != nil {
		log.Fatalf("could not connect: %v", err)
	}
	defer conn.Close()

	// Setup authentication
	apiKey := getEnv("KAIKO_API_KEY", "1234") // Put your api key here
	ctx = metadata.AppendToOutgoingContext(ctx, "authorization", "Bearer "+apiKey)

	timeout := 15 * time.Second // demo timeout
	ctx, cancel = context.WithTimeout(ctx, timeout)
	defer cancel()

	go func() {
		// Create a streaming spot exchange rate request with SDK
		err = spotExchangeRateRequest(ctx, conn)
		if err != nil {
			log.Fatalf("could not get spot exchange rates: %v", err)
		}
	}()

	go func() {
		// Create a streaming market update request with SDK
		err := marketUpdateRequest(ctx, conn)
		if err != nil {
			log.Fatalf("could not get market updates: %v", err)
		}
	}()

	go func() {
		// Create a streaming ohlcv request with SDK
		err := ohlcvRequest(ctx, conn)
		if err != nil {
			log.Fatalf("could not get ohlcvs: %v", err)
		}
	}()

	go func() {
		// Create a streaming vwap request with SDK
		err := vwapRequest(ctx, conn)
		if err != nil {
			log.Fatalf("could not get vwaps: %v", err)
		}
	}()

	go func() {
		// Create a streaming direct exchange rate request with SDK
		err := directExchangeRateRequest(ctx, conn)
		if err != nil {
			log.Fatalf("could not get direct exchange rates: %v", err)
		}
	}()

	go func() {
		// Create a streaming index request with SDK
		err := indexRequest(ctx, conn)
		if err != nil {
			log.Printf("could not get index: %v", err)
		}
	}()

	go func() {
		// Create a streaming index request with SDK
		err := derivativesPriceRequest(ctx, conn)
		if err != nil {
			log.Printf("could not get derivatives price: %v", err)
		}
	}()

	go func() {
		// Create a streaming aggregated quote request with SDK
		err := aggregatedQuoteRequest(ctx, conn)
		if err != nil {
			log.Printf("could not get aggregated quote: %v", err)
		}
	}()

	// Create a streaming trades request with SDK
	err = tradesRequest(ctx, conn)
	if err != nil {
		log.Fatalf("could not get trades: %v", err)
	}
}

func getEnv(key, fallback string) string {
	if value, ok := os.LookupEnv(key); ok {
		return value
	}
	return fallback
}

func ohlcvRequest(
	ctx context.Context,
	conn *grpc.ClientConn,
) error {
	cli := pb.NewStreamAggregatesOHLCVServiceV1Client(conn)
	request := aggregates_ohlcv_v1.StreamAggregatesOHLCVRequestV1{
		InstrumentCriteria: &core.InstrumentCriteria{
			Exchange:        "cbse",
			InstrumentClass: "spot",
			Code:            "btc-usd",
		},
		Aggregate: "1s",
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

		fmt.Printf("[OHLCV] %+v\n", elt)
	}
}

func vwapRequest(
	ctx context.Context,
	conn *grpc.ClientConn,
) error {
	cli := pb.NewStreamAggregatesVWAPServiceV1Client(conn)
	request := aggregates_vwap_v1.StreamAggregatesVWAPRequestV1{
		InstrumentCriteria: &core.InstrumentCriteria{
			Exchange:        "cbse",
			InstrumentClass: "spot",
			Code:            "btc-usd",
		},
		Aggregate: "1s",
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

		fmt.Printf("[VWAP] %+v\n", elt)
	}
}

func directExchangeRateRequest(
	ctx context.Context,
	conn *grpc.ClientConn,
) error {
	cli := pb.NewStreamAggregatesDirectExchangeRateServiceV1Client(conn)
	request := aggregates_direct_exchange_rate_v1.StreamAggregatesDirectExchangeRateRequestV1{
		Code:      "btc-usd",
		Aggregate: "1s",
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

		fmt.Printf("[DIRECT EXCHANGE RATE] %+v\n", elt)
	}
}

func spotExchangeRateRequest(
	ctx context.Context,
	conn *grpc.ClientConn,
) error {
	cli := pb.NewStreamAggregatesSpotExchangeRateServiceV1Client(conn)
	request := aggregates_spot_exchange_rate_v1.StreamAggregatesSpotExchangeRateRequestV1{
		Code:      "btc-usd",
		Aggregate: "1s",
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

		fmt.Printf("[SPOT EXCHANGE RATE] %+v\n", elt)
	}
}

func marketUpdateRequest(
	ctx context.Context,
	conn *grpc.ClientConn,
) error {
	cli := pb.NewStreamMarketUpdateServiceV1Client(conn)
	// Globbing patterns are also supported on all fields. See http://sdk.kaiko.com/#instrument-selection for all supported patterns
	request := market_update_v1.StreamMarketUpdateRequestV1{
		InstrumentCriteria: &core.InstrumentCriteria{
			Exchange:        "cbse",
			InstrumentClass: "spot",
			Code:            "btc-usd",
		},
		Commodities: []market_update_v1.StreamMarketUpdateCommodity{market_update_v1.StreamMarketUpdateCommodity_SMUC_TRADE},
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

		fmt.Printf("[MARKET UPDATE] %+v\n", elt)
	}
}

func tradesRequest(
	ctx context.Context,
	conn *grpc.ClientConn,
) error {
	cli := pb.NewStreamTradesServiceV1Client(conn)
	// Globbing patterns are also supported on all fields. See http://sdk.kaiko.com/#instrument-selection for all supported patterns
	request := trades_v1.StreamTradesRequestV1{
		InstrumentCriteria: &core.InstrumentCriteria{
			Exchange:        "cbse",
			InstrumentClass: "spot",
			Code:            "btc-usd",
		},
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

		fmt.Printf("[TRADE] %+v\n", elt)
	}
}

func indexRequest(
	ctx context.Context,
	conn *grpc.ClientConn,
) error {
	cli := pb.NewStreamIndexServiceV1Client(conn)
	request := index_v1.StreamIndexServiceRequestV1{
		IndexCode: "indexCode", // fill it with actual value
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

func derivativesPriceRequest(
	ctx context.Context,
	conn *grpc.ClientConn,
) error {
	cli := pb.NewStreamDerivativesPriceServiceV2Client(conn)
	request := derivatives_price_v2.StreamDerivativesPriceRequestV2{
		InstrumentCriteria: &core.InstrumentCriteria{
			Exchange:        "drbt",
			InstrumentClass: "future",
			Code:            "btc31dec21",
		},
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

		fmt.Printf("[DERIVATIVES PRICE] %+v\n", elt)
	}
}

func aggregatedQuoteRequest(
	ctx context.Context,
	conn *grpc.ClientConn,
) error {
	cli := pb.NewStreamAggregatedPriceServiceV1Client(conn)
	// Globbing patterns are also supported on all fields. See http://sdk.kaiko.com/#instrument-selection for all supported patterns
	request := aggregated_price_v1.StreamAggregatedPriceRequestV1{
		InstrumentClass: "spot",
		Code:            "btc-usd",
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

		fmt.Printf("[AGGREGATED QUOTE] %+v\n", elt)
	}
}
