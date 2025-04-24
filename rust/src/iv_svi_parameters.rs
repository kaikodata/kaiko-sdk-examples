use kaikosdk::stream_iv_svi_parameters_service_v1_client::StreamIvSviParametersServiceV1Client;
use kaikosdk::{Assets, StreamIvSviParametersRequestV1};
use tokio_stream::StreamExt;
use tonic::Request;
use tonic::metadata::{Ascii, MetadataValue};
use tonic::transport::Channel;

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    let (channel, token) = example::new_channel().await?;

    iv_svi_parameters(channel, &token).await?;

    Ok(())
}

async fn iv_svi_parameters(
    channel: Channel,
    token: &MetadataValue<Ascii>,
) -> Result<(), Box<dyn std::error::Error>> {
    let mut client = StreamIvSviParametersServiceV1Client::with_interceptor(
        channel,
        move |mut req: Request<()>| {
            req.metadata_mut().insert("authorization", token.clone());
            Ok(req)
        },
    );

    let request = Request::new(StreamIvSviParametersRequestV1 {
        assets: Some(Assets {
            base: "btc".into(),
            quote: "usd".into(),
        }),
        exchanges: "drbt".into(),
        ..Default::default()
    });

    let mut stream = client.subscribe(request).await?.into_inner();

    while let Some(item) = stream.next().await {
        println!("{:?}", item?);
    }

    Ok(())
}
