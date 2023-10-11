use kaikosdk::StreamMarketUpdateCommodity;
use kaikosdk::{
    stream_market_update_service_v1_client::StreamMarketUpdateServiceV1Client,
    StreamMarketUpdateRequestV1,
};
use tokio_stream::StreamExt;
use tonic::Request;
use tonic::{
    metadata::{Ascii, MetadataValue},
    transport::Channel,
};

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    let channel = new_channel().await?;
    let api_key =
        std::env::var("KAIKO_API_KEY").map_err(|_| "KAIKO_API_KEY environment variable not set")?;

    let token: MetadataValue<_> = format!("Bearer {}", api_key).parse()?;

    market_update_trades(channel, &token).await?;

    Ok(())
}

async fn market_update_trades(
    channel: Channel,
    token: &MetadataValue<Ascii>,
) -> Result<(), Box<dyn std::error::Error>> {
    let mut client =
        StreamMarketUpdateServiceV1Client::with_interceptor(channel, move |mut req: Request<()>| {
            req.metadata_mut().insert("authorization", token.clone());
            Ok(req)
        });

    let mut payload = StreamMarketUpdateRequestV1::default();
    payload.instrument_criteria = Some(kaikosdk::InstrumentCriteria {
        exchange: "cbse".into(),
        instrument_class: "spot".into(),
        code: "btc-usd".into(),
    });

    payload.commodities = vec![StreamMarketUpdateCommodity::SmucTrade.into()];
    payload.interval = None;

    let request = Request::new(payload);
    let mut stream = client.subscribe(request).await?.into_inner();

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
