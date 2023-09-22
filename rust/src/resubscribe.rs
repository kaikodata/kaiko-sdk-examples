use kaikosdk::{
    stream_trades_service_v1_client::StreamTradesServiceV1Client, StreamTradesRequestV1,
    StreamTradesResponseV1,
};
use tokio::time::{sleep, Duration};
use tokio_stream::StreamExt;
use tonic::metadata::Ascii;
use tonic::{metadata::MetadataValue, transport::Channel, Request, Response, Status, Streaming};

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    let channel = new_channel().await?;
    let api_key = std::env::var("KAIKO_API_KEY").unwrap_or("1234".into());
    let token: MetadataValue<_> = format!("Bearer {}", api_key).parse()?;

    let mut stream = new_sub(channel.clone(), token.clone())
        .await?
        .into_inner()
        .take(10);

    loop {
        // stream is infinite - take just 10 elements and then disconnect
        while let Some(item) = stream.next().await {
            println!("{:?}", item?);
        }

        println!("Resubscribe");
        sleep(Duration::from_secs(1)).await;

        stream = new_sub(channel.clone(), token.clone())
            .await?
            .into_inner()
            .take(10);
    }
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

async fn new_sub(
    channel: Channel,
    token: MetadataValue<Ascii>,
) -> Result<Response<Streaming<StreamTradesResponseV1>>, Status> {
    let mut client =
        StreamTradesServiceV1Client::with_interceptor(channel, move |mut req: Request<()>| {
            req.metadata_mut().insert("authorization", token.clone());
            Ok(req)
        });

    let request = tonic::Request::new(StreamTradesRequestV1 {
        instrument_criteria: Some(kaikosdk::InstrumentCriteria {
            exchange: "cbse".into(),
            instrument_class: "spot".into(),
            code: "btc-usd".into(),
        }),
    });

    client.subscribe(request).await
}
