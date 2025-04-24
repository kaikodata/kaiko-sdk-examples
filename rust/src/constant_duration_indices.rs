use std::time::{Duration, SystemTime, UNIX_EPOCH};

use kaikosdk::stream_constant_duration_indices_service_v1_client::StreamConstantDurationIndicesServiceV1Client;
use kaikosdk::{DataInterval, StreamConstantDurationIndicesServiceRequestV1, StreamIndexCommodity};
use pbjson_types::Timestamp;
use tokio_stream::StreamExt;
use tonic::Request;
use tonic::metadata::{Ascii, MetadataValue};
use tonic::transport::Channel;

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    let (channel, token) = example::new_channel().await?;

    // If you wish to query multiple request. You can use tokio::join! macro by cloning the channel and token.
    let index_code = std::env::var("KAIKO_INDEX_CODE")
        .map_err(|_| "KAIKO_INDEX_CODE environment variable not set")?;

    tokio::try_join!(
        constant_duration_indices(&index_code, channel.clone(), &token),
        constant_duration_indices_replay(&index_code, channel, &token)
    )?;

    Ok(())
}

async fn constant_duration_indices(
    index_code: &str,
    channel: Channel,
    token: &MetadataValue<Ascii>,
) -> Result<(), Box<dyn std::error::Error>> {
    let mut client = StreamConstantDurationIndicesServiceV1Client::with_interceptor(
        channel,
        move |mut req: Request<()>| {
            req.metadata_mut().insert("authorization", token.clone());
            Ok(req)
        },
    );

    let request = Request::new(StreamConstantDurationIndicesServiceRequestV1 {
        index_code: index_code.into(),
        commodities: vec![],
        interval: None,
    });

    let mut stream = client.subscribe(request).await?.into_inner();

    while let Some(item) = stream.next().await {
        println!("{:?}", item?);
    }

    Ok(())
}

async fn constant_duration_indices_replay(
    index_code: &str,
    channel: Channel,
    token: &MetadataValue<Ascii>,
) -> Result<(), Box<dyn std::error::Error>> {
    let mut client = StreamConstantDurationIndicesServiceV1Client::with_interceptor(
        channel,
        move |mut req: Request<()>| {
            req.metadata_mut().insert("authorization", token.clone());
            Ok(req)
        },
    );

    let now = SystemTime::now();
    let end = now.duration_since(UNIX_EPOCH)?.as_secs() as i64;
    let start = now
        .checked_sub(Duration::from_secs(3600))
        .unwrap()
        .duration_since(UNIX_EPOCH)?
        .as_secs() as i64;

    let request = Request::new(StreamConstantDurationIndicesServiceRequestV1 {
        index_code: index_code.into(),
        commodities: vec![],
        interval: Some(DataInterval {
            start_time: Some(Timestamp {
                seconds: start,
                nanos: 0,
            }),
            end_time: Some(Timestamp {
                seconds: end,
                nanos: 0,
            }),
        }),
    });

    let mut stream = client.subscribe(request).await?.into_inner();

    while let Some(item) = stream.next().await {
        println!("{:?}", item?);
    }

    Ok(())
}
