use kaikosdk::stream_aggregates_spot_direct_exchange_rate_v2_service_v1_client::StreamAggregatesSpotDirectExchangeRateV2ServiceV1Client;
use kaikosdk::stream_aggregates_spot_exchange_rate_v2_service_v1_client::StreamAggregatesSpotExchangeRateV2ServiceV1Client;
use kaikosdk::{
    Assets, StreamAggregatesDirectExchangeRateV2RequestV1,
    StreamAggregatesSpotExchangeRateV2RequestV1,
};
use pbjson_types::Duration;
use tokio_stream::StreamExt;
use tonic::Request;
use tonic::metadata::{Ascii, MetadataValue};
use tonic::transport::Channel;

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    let (channel, token) = example::new_channel().await?;

    tokio::try_join!(
        aggregates_spot_exchange_rate(channel.clone(), &token),
        aggregates_spot_direct_exchange_rate(channel.clone(), &token),
    )?;

    Ok(())
}

async fn aggregates_spot_exchange_rate(
    channel: Channel,
    token: &MetadataValue<Ascii>,
) -> Result<(), Box<dyn std::error::Error>> {
    let mut client = StreamAggregatesSpotExchangeRateV2ServiceV1Client::with_interceptor(
        channel,
        move |mut req: Request<()>| {
            req.metadata_mut().insert("authorization", token.clone());
            Ok(req)
        },
    );

    let request = Request::new(StreamAggregatesSpotExchangeRateV2RequestV1 {
        assets: Some(Assets {
            base: "btc".into(),
            quote: "usd".into(),
        }),
        window: Some(Duration {
            seconds: 10,
            nanos: 0,
        }),
        update_frequency: Some(Duration {
            seconds: 2,
            nanos: 0,
        }),
        ..Default::default()
    });

    let mut stream = client.subscribe(request).await?.into_inner();

    while let Some(item) = stream.next().await {
        println!("{:?}", item?);
    }

    Ok(())
}

async fn aggregates_spot_direct_exchange_rate(
    channel: Channel,
    token: &MetadataValue<Ascii>,
) -> Result<(), Box<dyn std::error::Error>> {
    let mut client = StreamAggregatesSpotDirectExchangeRateV2ServiceV1Client::with_interceptor(
        channel,
        move |mut req: Request<()>| {
            req.metadata_mut().insert("authorization", token.clone());
            Ok(req)
        },
    );

    let request = Request::new(StreamAggregatesDirectExchangeRateV2RequestV1 {
        assets: Some(Assets {
            base: "btc".into(),
            quote: "usd".into(),
        }),
        window: Some(Duration {
            seconds: 10,
            nanos: 0,
        }),
        update_frequency: Some(Duration {
            seconds: 2,
            nanos: 0,
        }),
        ..Default::default()
    });

    let mut stream = client.subscribe(request).await?.into_inner();

    while let Some(item) = stream.next().await {
        println!("{:?}", item?);
    }

    Ok(())
}
