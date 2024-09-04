use tonic::metadata::{Ascii, MetadataValue};
use tonic::transport::{Channel, ClientTlsConfig};

pub async fn new_channel() -> Result<(Channel, MetadataValue<Ascii>), Box<dyn std::error::Error>> {
    let tls = ClientTlsConfig::new().with_native_roots();

    let channel = Channel::from_static("https://gateway-v0-grpc.kaiko.ovh:443")
        .tls_config(tls)?
        .connect()
        .await?;

    let api_key =
        std::env::var("KAIKO_API_KEY").map_err(|_| "KAIKO_API_KEY environment variable not set")?;

    let token: MetadataValue<_> = format!("Bearer {}", api_key).parse()?;

    Ok((channel, token))
}
