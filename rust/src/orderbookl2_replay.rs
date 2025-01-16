use kaikosdk::{
    stream_orderbook_l2_replay_service_v1_client::StreamOrderbookL2ReplayServiceV1Client,
    DataInterval, StreamOrderBookL2ReplayRequestV1,
};
use pbjson_types::Timestamp;
use std::time::SystemTime;
use tokio_stream::StreamExt;
use tonic::Request;
use tonic::{
    metadata::{Ascii, MetadataValue},
    transport::Channel,
};

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    let (channel, token) = example::new_channel().await?;

    orderbookl2_replay(channel, &token).await?;

    Ok(())
}

async fn orderbookl2_replay(
    channel: Channel,
    token: &MetadataValue<Ascii>,
) -> Result<(), Box<dyn std::error::Error>> {
    let mut client = StreamOrderbookL2ReplayServiceV1Client::with_interceptor(
        channel,
        move |mut req: Request<()>| {
            req.metadata_mut().insert("authorization", token.clone());
            Ok(req)
        },
    );

    let start = SystemTime::now()
        .duration_since(SystemTime::UNIX_EPOCH)
        .unwrap()
        .as_secs()
        - 120;
    let end = SystemTime::now()
        .duration_since(SystemTime::UNIX_EPOCH)
        .unwrap()
        .as_secs()
        - 60;

    #[allow(deprecated)]
    let payload = StreamOrderBookL2ReplayRequestV1 {
        instrument_criteria: Some(kaikosdk::InstrumentCriteria {
            exchange: "cbse".into(),
            instrument_class: "spot".into(),
            code: "btc-usd".into(),
        }),
        interval: Some(DataInterval {
            start_time: Some(Timestamp {
                seconds: start as i64,
                nanos: 0,
            }),
            end_time: Some(Timestamp {
                seconds: end as i64,
                nanos: 0,
            }),
        }),
    };

    let request = Request::new(payload);
    let mut stream = client.subscribe(request).await?.into_inner();

    while let Some(item) = stream.next().await {
        println!("{:?}", item?);
    }

    Ok(())
}
