package main

import (
	"context"
	"crypto/tls"
	"errors"
	"fmt"
	"io"
	"log"
	"os"
	"time"

	"github.com/cenkalti/backoff/v4"
	pb "github.com/kaikodata/kaiko-go-sdk"
	"github.com/kaikodata/kaiko-go-sdk/core"
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

	// Create a streaming trades request with SDK
	err = tradesRequest(ctx, conn)
	if err != nil {
		log.Fatalf("could not get trades: %v", err)
	}
}

func tradesRequest(
	ctx context.Context,
	conn *grpc.ClientConn,
) error {
	cli := pb.NewStreamTradesServiceV1Client(conn)
	// Globbing patterns are also supported on all fields. See http://sdk.kaiko.com/#instrument-selection for all supported patterns
	request := &trades_v1.StreamTradesRequestV1{
		InstrumentCriteria: &core.InstrumentCriteria{
			Exchange:        "cbse",
			InstrumentClass: "spot",
			Code:            "btc-usd",
		},
	}

	receive := func() error {
		sub := subscribe(ctx, cli, request)

		for {
			elt, err := sub.Recv()
			if err != nil {
				if errors.Is(err, io.EOF) {
					sub = subscribe(ctx, cli, request)

					continue
				}

				return err
			}

			fmt.Printf("[TRADE] %+v\n", elt)
		}
	}

	return receive()
}

func getEnv(key, fallback string) string {
	if value, ok := os.LookupEnv(key); ok {
		return value
	}
	return fallback
}

func subscribe(
	ctx context.Context,
	cli pb.StreamTradesServiceV1Client,
	request *trades_v1.StreamTradesRequestV1,
) pb.StreamTradesServiceV1_SubscribeClient {
	var subscription pb.StreamTradesServiceV1_SubscribeClient

	fmt.Println("subscribing to trades: START")

	b := backoff.NewExponentialBackOff()
	b.InitialInterval = retryInitialInterval // set min time when doing retry loops

	err := backoff.Retry(func() error {
		time.Sleep(retryWaitMin) // give a bit of time before retry

		sub, err := cli.Subscribe(ctx, request)
		if err != nil {
			return err
		}

		fmt.Println("resub done")

		subscription = sub

		return nil
	}, backoff.WithContext(b, ctx))
	if err != nil {
		log.Fatalf("could not subscribe: %v", err)
	}

	fmt.Println("subscribe to trades : DONE")

	return subscription
}

const (
	retryInitialInterval = 2 * time.Second
	retryWaitMin         = 1 * time.Second
)
