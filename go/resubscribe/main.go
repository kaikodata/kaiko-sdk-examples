package main

import (
	"context"
	"crypto/tls"
	"errors"
	"io"
	"log"
	"os"
	"sync"
	"time"

	"github.com/bobg/go-generics/v3/maps"
	"github.com/bobg/go-generics/v3/slices"
	"github.com/cenkalti/backoff/v4"
	pb "github.com/kaikodata/kaiko-go-sdk"
	"github.com/kaikodata/kaiko-go-sdk/stream/index_v1"
	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials"
	"google.golang.org/grpc/metadata"
)

type countMap struct {
	tickers map[string]time.Time

	*sync.RWMutex
}

func (c *countMap) Update(value *index_v1.StreamIndexServiceResponseV1) {
	c.Lock()
	defer c.Unlock()

	c.tickers[value.IndexCode] = value.TsEvent.AsTime()
}

func (c *countMap) Debug() {
	values := func() []time.Time {
		c.RLock()
		defer c.RUnlock()

		return maps.Values(c.tickers)
	}()

	minTs := slices.Min(slices.Map(values, func(a time.Time) int64 { return a.UnixNano() }))

	log.Printf("%d tickers\n", len(c.tickers))
	log.Printf("earliest timestamp : %s\n", time.Unix(0, minTs))
}

// KAIKO_API_KEY="" KAIKO_STREAM_ENDPOINT="gateway-v0-grpc.kaiko.ovh:443" KAIKO_RATE_PATTERN="" go run resubscribe/main.go
func main() {
	ctx, cancel := context.WithCancel(context.Background())
	defer cancel()

	endpoint := getEnv("KAIKO_STREAM_ENDPOINT", "")
	if len(endpoint) == 0 {
		panic("Please provide 'KAIKO_STREAM_ENDPOINT' env var")
	}

	// Set up a connection to the server.
	// WARNING: reconnection is automatically handled by client, you should never have
	// 2 connections at the same time otherwise you're likely to leak connections.
	conn, err := grpc.Dial(endpoint, grpc.WithTransportCredentials(credentials.NewTLS(&tls.Config{})))
	if err != nil {
		log.Fatalf("could not connect: %v", err)
	}
	defer conn.Close()

	// Setup authentication
	apiKey := getEnv("KAIKO_API_KEY", "1234") // Put your api key here
	ctx = metadata.AppendToOutgoingContext(ctx, "authorization", "Bearer "+apiKey)

	pattern := getEnv("KAIKO_RATE_PATTERN", "")
	if len(pattern) == 0 {
		panic("Please provide 'KAIKO_RATE_PATTERN' env var")
	}

	m := &countMap{
		tickers: make(map[string]time.Time),
		RWMutex: &sync.RWMutex{},
	}

	t := time.NewTicker(5 * time.Second)
	defer t.Stop()

	ch := make(chan *index_v1.StreamIndexServiceResponseV1)

	go func() {
		for {
			select {
			case <-ctx.Done():
				log.Println("done")
				return
			case <-t.C:
				// log.Println("debug")
				m.Debug()
			case e := <-ch:
				// log.Println("update")
				m.Update(e)
			}
		}
	}()

	// Create a streaming index request with SDK
	err = indexRequest(ctx, conn, pattern, ch)
	if err != nil {
		log.Printf("could not get index: %v", err)
	}
}

func getEnv(key, fallback string) string {
	if value, ok := os.LookupEnv(key); ok {
		return value
	}
	return fallback
}

func subscribe(
	ctx context.Context,
	cli pb.StreamIndexServiceV1Client,
	request *index_v1.StreamIndexServiceRequestV1,
) pb.StreamIndexServiceV1_SubscribeClient {
	var subscription pb.StreamIndexServiceV1_SubscribeClient

	log.Println("subscribing to rates: START")

	b := backoff.NewExponentialBackOff()
	b.InitialInterval = retryInitialInterval // set min time when doing retry loops

	err := backoff.Retry(func() error {
		time.Sleep(retryWaitMin) // give a bit of time before retry

		sub, err := cli.Subscribe(ctx, request)
		if err != nil {
			return err
		}

		log.Println("resub done")

		subscription = sub

		return nil
	}, backoff.WithContext(b, ctx))
	if err != nil {
		log.Fatalf("could not subscribe: %v", err)
	}

	log.Println("subscribe to rates : DONE")

	return subscription
}

func indexRequest(
	ctx context.Context,
	conn *grpc.ClientConn,
	pattern string,
	ch chan *index_v1.StreamIndexServiceResponseV1,
) error {
	cli := pb.NewStreamIndexServiceV1Client(conn)
	request := &index_v1.StreamIndexServiceRequestV1{
		IndexCode: pattern,
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

			ch <- elt
			// m.Update(elt)
			// log.Println("update done", time.Now())
			// log.Printf("[RATE] %+v\n", elt)
		}
	}

	return receive()
}

const (
	retryInitialInterval = 2 * time.Second
	retryWaitMin         = 1 * time.Second
)
