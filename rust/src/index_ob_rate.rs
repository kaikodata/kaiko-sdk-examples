use kaikosdk::{
    StreamIndexObRateRequestV1,
    stream_index_ob_rate_service_v1_client::StreamIndexObRateServiceV1Client,
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

    index_ob_rate(channel, &token).await?;

    Ok(())
}

/// Run an example of order-book rates.
///
/// Order-book rates are computed from a consolidated order book (the "feed").
/// Request a rate by its code, and the book it is computed from by its feed
/// name: the feed name is in each rate message (`source_feed`).
async fn index_ob_rate(
    channel: Channel,
    token: &MetadataValue<Ascii>,
) -> Result<(), Box<dyn std::error::Error>> {
    let mut client =
        StreamIndexObRateServiceV1Client::with_interceptor(channel, move |mut req: Request<()>| {
            req.metadata_mut().insert("authorization", token.clone());
            Ok(req)
        });

    let request = Request::new(StreamIndexObRateRequestV1 {
        index_codes: vec!["<YOUR_OB_RATE_CODE>".into()],
    });

    let stream = client.subscribe(request).await?.into_inner();

    // stream is infinite - take just 10 elements and then disconnect
    let mut stream = stream.take(10);
    while let Some(item) = stream.next().await {
        println!("{:?}", item?);
    }

    Ok(())
}
