use kaikosdk::{
    StreamAggregatesOhlcvRequestV1, StreamTradesRequestV1,
    stream_aggregates_ohlcv_service_v1_client::StreamAggregatesOhlcvServiceV1Client,
    stream_trades_service_v1_client::StreamTradesServiceV1Client,
};
use tokio_stream::StreamExt;
use tonic::{Request, metadata::Ascii, metadata::MetadataValue, transport::Channel};

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    let (channel, token) = example::new_channel().await?;
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
