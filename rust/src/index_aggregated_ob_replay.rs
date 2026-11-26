use kaikosdk::{
    DataInterval, StreamIndexAggregatedObReplayRequestV1,
    stream_index_aggregated_ob_replay_service_v1_client::StreamIndexAggregatedObReplayServiceV1Client,
};
use pbjson_types::Timestamp;
use std::time::{Duration, SystemTime, UNIX_EPOCH};
use tokio_stream::StreamExt;
use tonic::Request;
use tonic::{
    metadata::{Ascii, MetadataValue},
    transport::Channel,
};

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    let (channel, token) = example::new_channel().await?;

    index_aggregated_ob_replay(channel, &token).await?;

    Ok(())
}

/// Run an example of aggregated order book replay.
async fn index_aggregated_ob_replay(
    channel: Channel,
    token: &MetadataValue<Ascii>,
) -> Result<(), Box<dyn std::error::Error>> {
    let mut client = StreamIndexAggregatedObReplayServiceV1Client::with_interceptor(
        channel,
        move |mut req: Request<()>| {
            req.metadata_mut().insert("authorization", token.clone());
            Ok(req)
        },
    );

    // Replays cover the last 48 hours: replay one minute, one hour ago.
    let end_time = SystemTime::now().duration_since(UNIX_EPOCH)? - Duration::from_secs(3600);
    let start_time = end_time - Duration::from_secs(60);

    let request = Request::new(StreamIndexAggregatedObReplayRequestV1 {
        feed_names: vec!["<YOUR_FEED_NAME>".into()],
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
