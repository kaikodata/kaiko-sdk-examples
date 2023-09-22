use kaikosdk::{
    stream_aggregates_ohlcv_service_v1_client::StreamAggregatesOhlcvServiceV1Client,
    stream_trades_service_v1_client::StreamTradesServiceV1Client, StreamAggregatesOhlcvRequestV1,
    StreamTradesRequestV1,
};
use tokio_stream::StreamExt;
use tonic::{metadata::Ascii, metadata::MetadataValue, transport::Channel, Request};

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    let channel = new_channel().await?;
    let api_key = std::env::var("KAIKO_API_KEY").unwrap_or("1234".into());
    let token: MetadataValue<_> = format!("Bearer {}", api_key).parse()?;

    client_trades(channel.clone(), token.clone()).await?;
    client_ohlcv(channel.clone(), token.clone()).await?;

    Ok(())
}

async fn client_trades(
    channel: Channel,
    token: MetadataValue<Ascii>,
) -> Result<(), Box<dyn std::error::Error>> {
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

    let stream = client.subscribe(request).await?.into_inner();

    // stream is infinite - take just 10 elements and then disconnect
    let mut stream = stream.take(10);
    while let Some(item) = stream.next().await {
        println!("{:?}", item?);
    }

    Ok(())
}

async fn client_ohlcv(
    channel: Channel,
    token: MetadataValue<Ascii>,
) -> Result<(), Box<dyn std::error::Error>> {
    let mut client = StreamAggregatesOhlcvServiceV1Client::with_interceptor(
        channel,
        move |mut req: Request<()>| {
            req.metadata_mut().insert("authorization", token.clone());
            Ok(req)
        },
    );

    let request = tonic::Request::new(StreamAggregatesOhlcvRequestV1 {
        aggregate: "1m".into(),
        instrument_criteria: Some(kaikosdk::InstrumentCriteria {
            exchange: "cbse".into(),
            instrument_class: "spot".into(),
            code: "btc-usd".into(),
        }),
    });

    let stream = client.subscribe(request).await?.into_inner();

    // stream is infinite - take just 10 elements and then disconnect
    let mut stream = stream.take(10);
    while let Some(item) = stream.next().await {
        println!("{:?}", item?);
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
