use kaikosdk::{
    StreamIndexAggregatedObRequestV1,
    stream_index_aggregated_ob_service_v1_client::StreamIndexAggregatedObServiceV1Client,
};
use tokio_stream::StreamExt;
use tonic::Request;
use tonic::{
    metadata::{Ascii, MetadataValue},
    transport::Channel,
};

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    let (channel, token) = example::new_channel().await?;

    index_aggregated_ob(channel, &token).await?;

    Ok(())
}

/// Run an example of the aggregated order book an order-book rate is computed from.
async fn index_aggregated_ob(
    channel: Channel,
    token: &MetadataValue<Ascii>,
) -> Result<(), Box<dyn std::error::Error>> {
    let mut client = StreamIndexAggregatedObServiceV1Client::with_interceptor(
        channel,
        move |mut req: Request<()>| {
            req.metadata_mut().insert("authorization", token.clone());
            Ok(req)
        },
    );

    let request = Request::new(StreamIndexAggregatedObRequestV1 {
        feed_names: vec!["<YOUR_FEED_NAME>".into()],
    });

    let stream = client.subscribe(request).await?.into_inner();

    // stream is infinite - take just 10 elements and then disconnect
    let mut stream = stream.take(10);
    while let Some(item) = stream.next().await {
        println!("{:?}", item?);
    }

    Ok(())
}
