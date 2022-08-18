// Example use of Kaiko SDK.
package main

import (
	"context"
	"crypto/tls"
	"io"
	"log"
	"os"
	"time"

	pb "github.com/kaikodata/kaiko-go-sdk"
	"github.com/kaikodata/kaiko-go-sdk/core"
	"github.com/kaikodata/kaiko-go-sdk/stream/market_update_v1"
	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials"
	"google.golang.org/grpc/metadata"
	"google.golang.org/protobuf/types/known/timestamppb"
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

	// Create a streaming market update request with SDK
	err = marketUpdateRequest(ctx, conn)
	if err != nil {
		log.Fatalf("could not get market updates: %v", err)
	}
}

func getEnv(key, fallback string) string {
	if value, ok := os.LookupEnv(key); ok {
		return value
	}
	return fallback
}

func marketUpdateRequest(
	ctx context.Context,
	conn *grpc.ClientConn,
) error {
	cli := pb.NewStreamMarketUpdateServiceV1Client(conn)
	// Globbing patterns are also supported on all fields. See http://sdk.kaiko.com/#instrument-selection for all supported patterns
	request := market_update_v1.StreamMarketUpdateRequestV1{
		InstrumentCriteria: &core.InstrumentCriteria{
			Exchange:        "*",
			InstrumentClass: "*",
			Code:            "*",
		},
		Commodities: []market_update_v1.StreamMarketUpdateCommodity{market_update_v1.StreamMarketUpdateCommodity_SMUC_FULL_ORDER_BOOK},
		Interval: &core.DataInterval{
			StartTime: timestamppb.New(time.Unix(1660608000, 0)),
			EndTime:   timestamppb.New(time.Unix(1660694400, 0)),
		},
	}

	sub, err := cli.Subscribe(ctx, &request)
	if err != nil {
		log.Fatalf("could not subscribe: %v", err)
	}

	for {
		_, err := sub.Recv()
		if err == io.EOF {
			return nil
		}

		if err != nil {
			return err
		}

		// fmt.Printf("[MARKET UPDATE] %+v\n", elt)
	}
}
