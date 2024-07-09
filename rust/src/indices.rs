use kaikosdk::stream_index_service_v1_client::StreamIndexServiceV1Client;
use kaikosdk::{
    StreamIndexCommodity, StreamIndexServiceRequestV1,
};
use tokio_stream::StreamExt;
use tonic::metadata::{Ascii, MetadataValue};
use tonic::transport::Channel;
use tonic::{Request, Streaming};

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    let channel = example::new_channel().await?;
    let api_key =
        std::env::var("KAIKO_API_KEY").map_err(|_| "KAIKO_API_KEY environment variable not set")?;

    let token: MetadataValue<_> = format!("Bearer {}", api_key).parse()?;

    tokio::try_join!(
        digital_assets_rates(channel.clone(), &token),
    )?;

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
        index_code: "D2X-KAIKO_BTCEUR,D2X-KAIKO_ETHEUR".to_string(),
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
