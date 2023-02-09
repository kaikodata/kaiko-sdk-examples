// Example use of Kaiko SDK.
package main

import (
	"context"
	"crypto/tls"
	"errors"
	"fmt"
	"io"
	"log"
	"os"
	"os/signal"
	"syscall"
	"time"

	pb "github.com/kaikodata/kaiko-go-sdk"
	"github.com/kaikodata/kaiko-go-sdk/core"
	"github.com/kaikodata/kaiko-go-sdk/stream/market_update_v1"
	"github.com/kaikodata/kaiko-go-sdk/stream/trades_v1"
	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials"
	"google.golang.org/grpc/metadata"
	"google.golang.org/protobuf/types/known/timestamppb"
)

func main() {
	ctx, cancel := context.WithCancel(context.Background())
	sigc := make(chan os.Signal, 1)
	signal.Notify(sigc, syscall.SIGINT, syscall.SIGHUP, syscall.SIGTERM)
	defer cancel()

	// cancel context when either termination signals are received
	go func() {
		<-sigc
		signal.Stop(sigc)
		cancel()
	}()

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

	// start := time.Now()
	// Create a streaming trades request with SDK
	// i, err := tradesRequest(ctx, conn)
	// if err != nil && errors.Is(errors.Unwrap(err), context.Canceled) {
	// 	log.Fatalf("could not get trades: %v", err)
	// }
	// end := time.Now()
	// fmt.Printf("%d trades between %s and %s\n", i, start.UTC().String(), end.UTC().String())
	// OUTPUT: 593545 trades between 2023-02-09 15:07:59.254071269 +0000 UTC and 2023-02-09 15:14:26.374716132 +0000 UTC

	i, err := tradesRequestHistorical(ctx, conn)
	if err != nil && errors.Is(errors.Unwrap(err), context.Canceled) {
		log.Fatalf("could not get historical trades: %v", err)
	}

	fmt.Printf("%d trades for interval", i)
	// OUTPUT: 596803 trades trades for interval

}

func getEnv(key, fallback string) string {
	if value, ok := os.LookupEnv(key); ok {
		return value
	}
	return fallback
}

func tradesRequest(
	ctx context.Context,
	conn *grpc.ClientConn,
) (int, error) {
	cli := pb.NewStreamTradesServiceV1Client(conn)
	// Globbing patterns are also supported on all fields. See http://sdk.kaiko.com/#instrument-selection for all supported patterns
	request := trades_v1.StreamTradesRequestV1{
		InstrumentCriteria: &core.InstrumentCriteria{
			Exchange:        "*",
			InstrumentClass: "spot",
			Code:            "*",
		},
	}

	sub, err := cli.Subscribe(ctx, &request)
	if err != nil {
		log.Fatalf("could not subscribe: %v", err)
	}

	i := 0

	for {
		_, err := sub.Recv()
		if err == io.EOF {
			return i, nil
		}

		if err != nil {
			return i, err
		}

		i++
	}
}

func tradesRequestHistorical(
	ctx context.Context,
	conn *grpc.ClientConn,
) (int, error) {
	cli := pb.NewStreamMarketUpdateServiceV1Client(conn)
	// Globbing patterns are also supported on all fields. See http://sdk.kaiko.com/#instrument-selection for all supported patterns
	request := market_update_v1.StreamMarketUpdateRequestV1{
		InstrumentCriteria: &core.InstrumentCriteria{
			Exchange:        "*",
			InstrumentClass: "spot",
			Code:            "*",
		},
		Commodities: []market_update_v1.StreamMarketUpdateCommodity{market_update_v1.StreamMarketUpdateCommodity_SMUC_TRADE},
		Interval: &core.DataInterval{
			StartTime: timestamppb.New(time.Date(2023, 02, 9, 15, 07, 59, 0, time.UTC)),
			EndTime:   timestamppb.New(time.Date(2023, 02, 9, 15, 14, 26, 0, time.UTC)),
		},
	}

	sub, err := cli.Subscribe(ctx, &request)
	if err != nil {
		log.Fatalf("could not subscribe: %v", err)
	}

	i := 0

	for {
		_, err := sub.Recv()
		if err == io.EOF {
			return i, nil
		}

		if err != nil {
			return i, err
		}

		i++
	}
}
