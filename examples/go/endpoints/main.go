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
	"github.com/kaikodata/kaiko-go-sdk/stream/trades_v1"
	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials"
	"google.golang.org/grpc/metadata"
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

	start := time.Now()

	// Create a streaming trades request with SDK
	i, err := tradesRequest(ctx, conn)
	if err != nil && errors.Is(errors.Unwrap(err), context.Canceled) {
		log.Fatalf("could not get trades: %v", err)
	}

	end := time.Now()

	fmt.Printf("%d trades between %s and %s\n", i, start.UTC().String(), end.UTC().String())
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
