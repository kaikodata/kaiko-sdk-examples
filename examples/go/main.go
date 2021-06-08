// Example use of Kaiko SDK.
package main

import (
	"context"
	"crypto/tls"
	"fmt"
	"io"
	"log"
	"os"

	pb "github.com/challengerdeep/kaiko-go-sdk"
	core "github.com/challengerdeep/kaiko-go-sdk/core"
	trades_v1 "github.com/challengerdeep/kaiko-go-sdk/stream/trades_v1"
	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials"
	"google.golang.org/grpc/metadata"
)

func main() {
	ctx, cancel := context.WithCancel(context.Background())
	defer cancel()

	// Set up a connection to the server.
	conn, err := grpc.Dial("gateway-v0-grpc.kaiko.ovh:443", grpc.WithTransportCredentials(credentials.NewTLS(&tls.Config{})))
	if err != nil {
		log.Fatalf("could not connect: %v", err)
	}
	defer conn.Close()

	// Setup authentication
	apiKey := getEnv("KAIKO_API_KEY", "1234") // Put your api key here
	ctx = metadata.AppendToOutgoingContext(ctx, "authorization", "Bearer "+apiKey)

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

func tradesRequest(
	ctx context.Context,
	conn *grpc.ClientConn,
) error {
	cli := pb.NewStreamTradesServiceV1Client(conn)
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
		trade, err := sub.Recv()
		if err == io.EOF {
			return nil
		}

		if err != nil {
			return err
		}

		fmt.Printf("%+v\n", trade)
	}
}
