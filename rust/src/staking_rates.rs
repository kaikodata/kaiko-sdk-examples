use kaikosdk::stream_staking_rates_service_v1_client::StreamStakingRatesServiceV1Client;
use kaikosdk::{DataInterval, StreamIndexCommodity, StreamStakingRatesServiceRequestV1};
use pbjson_types::Timestamp;
use std::time::{Duration, SystemTime, UNIX_EPOCH};
use tokio_stream::StreamExt;
use tonic::Request;
use tonic::metadata::{Ascii, MetadataValue};
use tonic::transport::Channel;

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    let (channel, token) = example::new_channel().await?;

    let index_code = std::env::var("KAIKO_INDEX_CODE")
        .map_err(|_| "KAIKO_INDEX_CODE environment variable not set")?;

    staking_rates_fixing(&index_code, channel, &token).await?;

    Ok(())
}

/// Run an example of staking rates fixing.
///
/// Staking rates are published once a day, so this endpoint must be queried
/// with the `SicDailyFixing` commodity and an interval covering the desired
/// publication window.
async fn staking_rates_fixing(
    index_code: &str,
    channel: Channel,
    token: &MetadataValue<Ascii>,
) -> Result<(), Box<dyn std::error::Error>> {
    let mut client = StreamStakingRatesServiceV1Client::with_interceptor(
        channel,
        move |mut req: Request<()>| {
            req.metadata_mut().insert("authorization", token.clone());
            Ok(req)
        },
    );

    let end_time = SystemTime::now().duration_since(UNIX_EPOCH)?;
    let start_time = end_time - Duration::from_secs(48 * 3600);

    let request = Request::new(StreamStakingRatesServiceRequestV1 {
        index_code: index_code.to_string(),
        commodities: vec![StreamIndexCommodity::SicDailyFixing.into()],
        interval: Some(DataInterval {
            start_time: Some(Timestamp {
                seconds: start_time.as_secs() as i64,
                nanos: 0,
            }),
            end_time: Some(Timestamp {
                seconds: end_time.as_secs() as i64,
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
