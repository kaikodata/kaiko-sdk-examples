use kaikosdk::stream_index_forex_rate_service_v1_client::StreamIndexForexRateServiceV1Client;
use kaikosdk::stream_index_multi_assets_service_v1_client::StreamIndexMultiAssetsServiceV1Client;
use kaikosdk::stream_index_service_v1_client::StreamIndexServiceV1Client;
use kaikosdk::{
    StreamIndexCommodity, StreamIndexForexRateServiceRequestV1,
    StreamIndexMultiAssetsServiceRequestV1, StreamIndexServiceRequestV1,
};
use tokio_stream::StreamExt;
use tonic::metadata::{Ascii, MetadataValue};
use tonic::transport::Channel;
use tonic::{Request, Streaming};

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    let channel = new_channel().await?;
    let api_key =
        std::env::var("KAIKO_API_KEY").map_err(|_| "KAIKO_API_KEY environment variable not set")?;

    let token: MetadataValue<_> = format!("Bearer {}", api_key).parse()?;

    tokio::try_join!(
        blue_chip_indices(channel.clone(), &token),
        digital_assets_rates(channel.clone(), &token),
        index_forex_rate(channel.clone(), &token)
    )?;

    Ok(())
}

/// Run an example of blue chip indices
/// /!\ This example requires a Kaiko API key with access to the Kaiko Indices API
///     This example requires that the Kaiko API Key has the proper permissions set with the KT10 index code configured
///
/// # Aguments
///
/// - `channel` - The channel to use to connect to the Kaiko Gateway
/// - `token` - The token to use to authenticate to the Kaiko Gateway
async fn blue_chip_indices(
    channel: Channel,
    token: &MetadataValue<Ascii>,
) -> Result<(), Box<dyn std::error::Error>> {
    let mut client = StreamIndexMultiAssetsServiceV1Client::with_interceptor(
        channel,
        move |mut req: Request<()>| {
            req.metadata_mut().insert("authorization", token.clone());
            Ok(req)
        },
    );

    let request = Request::new(StreamIndexMultiAssetsServiceRequestV1 {
        commodities: vec![StreamIndexCommodity::SicRealTime.into()],
        index_code: "KT10".to_string(),
        interval: None,
    });

    let stream = client.subscribe(request).await?.into_inner();

    process_stream_channel(stream, |item| {
        println!("{:?}", item);
    })
    .await?;

    Ok(())
}

/// Run an example of digital assets rates
/// /!\ This example requires a Kaiko API key with access to the Kaiko Indices API
///     This example requires that the Kaiko API Key has the proper permissions set with the KK_PR_BTCUSD index code configured
///
/// # Arguments
///
/// - `channel` - The channel to use to connect to the Kaiko Gateway
/// - `token` - The token to use to authenticate to the Kaiko Gateway
async fn digital_assets_rates(
    channel: Channel,
    token: &MetadataValue<Ascii>,
) -> Result<(), Box<dyn std::error::Error>> {
    let mut client =
        StreamIndexServiceV1Client::with_interceptor(channel, move |mut req: Request<()>| {
            req.metadata_mut().insert("authorization", token.clone());
            Ok(req)
        });

    let request = Request::new(StreamIndexServiceRequestV1 {
        commodities: vec![StreamIndexCommodity::SicRealTime.into()],
        index_code: "KK_PR_BTCUSD".to_string(),
        interval: None,
    });

    let stream = client.subscribe(request).await?.into_inner();

    process_stream_channel(stream, |item| {
        println!("{:?}", item);
    })
    .await?;

    Ok(())
}

/// Run an example of digital assets rates
/// /!\ This example requires a Kaiko API key with access to the Kaiko Indices API
///     This example requires that the Kaiko API Key has the proper permissions set with the KK_PR_BTCUSD index code configured
///
/// # Arguments
///
/// - `channel` - The channel to use to connect to the Kaiko Gateway
/// - `token` - The token to use to authenticate to the Kaiko Gateway
async fn index_forex_rate(
    channel: Channel,
    token: &MetadataValue<Ascii>,
) -> Result<(), Box<dyn std::error::Error>> {
    let mut client = StreamIndexForexRateServiceV1Client::with_interceptor(
        channel,
        move |mut req: Request<()>| {
            req.metadata_mut().insert("authorization", token.clone());
            Ok(req)
        },
    );

    let request = Request::new(StreamIndexForexRateServiceRequestV1 {
        index_code: "KK_PR_BTCUSD_EUR".to_string(),
        interval: None,
    });

    let stream = client.subscribe(request).await?.into_inner();

    process_stream_channel(stream, |item| {
        println!("{:?}", item);
    })
    .await?;

    Ok(())
}

async fn process_stream_channel<S, F>(
    mut stream: Streaming<S>,
    callback: F,
) -> Result<(), Box<dyn std::error::Error>>
where
    S: std::fmt::Debug,
    F: Fn(S),
{
    while let Some(item) = stream.next().await {
        let item = item?;
        callback(item);
    }

    Ok(())
}

async fn new_channel() -> Result<Channel, Box<dyn std::error::Error>> {
    let connector = hyper_rustls::HttpsConnectorBuilder::new()
        .with_native_roots()
        .https_only()
        .enable_http2()
        .build();

    let channel = Channel::from_static("https://gateway-v0-grpc.kaiko.ovh:443")
        .connect_with_connector(connector)
        .await?;

    Ok(channel)
}
