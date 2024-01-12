use tonic::transport::{Channel, ClientTlsConfig};

pub async fn new_channel() -> Result<Channel, Box<dyn std::error::Error>> {
    let tls = ClientTlsConfig::new();

    let channel = Channel::from_static("https://gateway-v0-grpc.kaiko.ovh:443")
        .tls_config(tls)?
        .connect()
        .await?;

    Ok(channel)
}
