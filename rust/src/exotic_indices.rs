use kaikosdk::stream_exotic_indices_service_v1_client::StreamExoticIndicesServiceV1Client;
use kaikosdk::{DataInterval, StreamExoticIndicesServiceRequestV1, StreamIndexCommodity};
use pbjson_types::Timestamp;
use tokio_stream::StreamExt;
use tonic::metadata::{Ascii, MetadataValue};
use tonic::transport::Channel;
use tonic::Request;

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    let (channel, token) = example::new_channel().await?;

    // If you wish to query multiple request. You can use tokio::join! macro by cloning the channel and token.
    //exotic_indices(channel, &token).await?;

    tokio::try_join!(
        exotic_indices(channel.clone(), &token),
        exotic_indices_replay(channel, &token)
    )?;

    Ok(())
}

async fn exotic_indices(
    channel: Channel,
    token: &MetadataValue<Ascii>,
) -> Result<(), Box<dyn std::error::Error>> {
    let mut client = StreamExoticIndicesServiceV1Client::with_interceptor(
        channel,
        move |mut req: Request<()>| {
            req.metadata_mut().insert("authorization", token.clone());
            Ok(req)
        },
    );

    let request = Request::new(StreamExoticIndicesServiceRequestV1 {
        index_code: "KT10TCUSD".into(),
        commodities: vec![StreamIndexCommodity::SicRealTime.into()],
        interval: None,
    });

    let mut stream = client.subscribe(request).await?.into_inner();

    while let Some(item) = stream.next().await {
        println!("{:?}", item?);
    }

    Ok(())
}

async fn exotic_indices_replay(
    channel: Channel,
    token: &MetadataValue<Ascii>,
) -> Result<(), Box<dyn std::error::Error>> {
    let mut client = StreamExoticIndicesServiceV1Client::with_interceptor(
        channel,
        move |mut req: Request<()>| {
            req.metadata_mut().insert("authorization", token.clone());
            Ok(req)
        },
    );

    let request = Request::new(StreamExoticIndicesServiceRequestV1 {
        index_code: "KT10TCUSD".into(),
        commodities: vec![StreamIndexCommodity::SicRealTime.into()],
        interval: Some(DataInterval {
            start_time: Some(Timestamp {
                seconds: 1725235200,
                nanos: 0,
            }),
            end_time: Some(Timestamp {
                seconds: 1725235500,
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
