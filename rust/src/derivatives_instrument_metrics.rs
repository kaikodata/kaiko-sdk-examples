use kaikosdk::StreamDerivativesInstrumentMetricsRequestV1;
use kaikosdk::stream_derivatives_instrument_metrics_service_v1_client::StreamDerivativesInstrumentMetricsServiceV1Client;
use tokio_stream::StreamExt;
use tonic::Request;
use tonic::metadata::{Ascii, MetadataValue};
use tonic::transport::Channel;

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    let (channel, token) = example::new_channel().await?;
    derivatives_instrument_metrics(channel, &token).await?;

    Ok(())
}

async fn derivatives_instrument_metrics(
    channel: Channel,
    token: &MetadataValue<Ascii>,
) -> Result<(), Box<dyn std::error::Error>> {
    let mut client = StreamDerivativesInstrumentMetricsServiceV1Client::with_interceptor(
        channel,
        move |mut req: Request<()>| {
            req.metadata_mut().insert("authorization", token.clone());
            Ok(req)
        },
    );

    let request = Request::new(StreamDerivativesInstrumentMetricsRequestV1 {
        instrument_criteria: Some(kaikosdk::InstrumentCriteria {
            exchange: "*".into(),
            instrument_class: "perpetual-future".into(),
            code: "btc-usd".into(),
        }),
        ..Default::default()
    });

    let mut stream = client.subscribe(request).await?.into_inner();

    while let Some(item) = stream.next().await {
        println!("{:?}", item?);
    }

    Ok(())
}
