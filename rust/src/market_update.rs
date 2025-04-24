use kaikosdk::StreamMarketUpdateCommodity;
use kaikosdk::{
    StreamMarketUpdateRequestV1,
    stream_market_update_service_v1_client::StreamMarketUpdateServiceV1Client,
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

    #[allow(deprecated)]
    let payload = StreamMarketUpdateRequestV1 {
        instrument_criteria: Some(kaikosdk::InstrumentCriteria {
            exchange: "cbse".into(),
            instrument_class: "spot".into(),
            code: "btc-usd".into(),
        }),
        commodities: vec![StreamMarketUpdateCommodity::SmucTrade.into()],
        interval: None,
        snapshot_type: 0,
    };

    let request = Request::new(payload);
    let mut stream = client.subscribe(request).await?.into_inner();

    while let Some(item) = stream.next().await {
        println!("{:?}", item?);
    }

    Ok(())
}
