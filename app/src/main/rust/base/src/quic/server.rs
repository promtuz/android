use std::net::IpAddr;
use std::net::SocketAddr;
use std::str::FromStr;
use std::sync::Arc;
// use std::time::Duration;

use anyhow::anyhow;
use common::crypto::PublicKey;
use common::crypto::encrypt::Encrypted;
use common::crypto::get_shared_key;
use common::msg::cbor::FromCbor;
use common::msg::cbor::ToCbor;
// use common::msg::postcard::FromPCard;
// use common::msg::postcard::ToPCard;
use common::msg::relay::HandshakePacket;
use log::info;
use quinn::ConnectionError;
use tokio::io::AsyncReadExt;

use crate::CONNECTION;
use crate::ENDPOINT;
use crate::EVENT_BUS;
use crate::data::relay::Relay;
use crate::events::InternalEvent;
use crate::events::connection::ConnectionState;

pub enum RelayConnError {
    Continue,
    Error(anyhow::Error),
}

impl<E> From<E> for RelayConnError
where
    E: std::error::Error + Send + Sync + 'static,
{
    fn from(err: E) -> Self {
        RelayConnError::Error(err.into())
    }
}

pub struct KeyPair {
    pub public: common::crypto::PublicKey,
    pub secret: common::crypto::StaticSecret,
}

impl Relay {
    pub async fn connect(&self, keypair: &KeyPair) -> Result<(), RelayConnError> {
        let addr = SocketAddr::new(IpAddr::from_str(&self.host.clone())?, self.port);

        info!("RELAY({}): CONNECTING AT {}", self.id, addr);

        _ = EVENT_BUS.0.send(InternalEvent::Connection { state: ConnectionState::Connecting });

        // TODO: verifying if the host exists before trying udp based handshake
        // ping?

        match ENDPOINT.get().unwrap().connect(addr, &self.id)?.await {
            Ok(conn) => {
                _ = EVENT_BUS
                    .0
                    .send(InternalEvent::Connection { state: ConnectionState::Handshaking });

                let (mut send, mut recv) = conn.open_bi().await?;

                use HandshakePacket::*;

                let client_hello = ClientHello { ipk: keypair.public.to_bytes() }.pack().unwrap();
                send.write_all(&client_hello).await?;

                let conn = Arc::new(conn);
                loop {
                    let conn = conn.clone();

                    let mut packet = vec![0u8; recv.read_u32().await? as usize];
                    recv.read_exact(&mut packet).await?;

                    let msg = HandshakePacket::from_cbor(&packet)?;

                    if let ServerChallenge { ct, epk } = msg {
                        let secret = keypair.secret.diffie_hellman(&PublicKey::from(epk));

                        let key = get_shared_key(
                            secret.as_bytes(),
                            &[0u8; 32],
                            "handshake.challenge.key",
                        );

                        // Authenticated Data
                        let ad = &[&keypair.public.as_bytes()[..], &epk[..]].concat();

                        let encrypted = Encrypted { cipher: ct.to_vec(), nonce: vec![0u8; 12] };

                        let proof = encrypted.decrypt(&key, ad)?.try_into().map_err(|_| {
                            RelayConnError::Error(anyhow!("server proof is invalid"))
                        })?;

                        let client_proof = ClientProof { proof }.pack()?;

                        send.write_all(&client_proof).await?;
                    } else if let ServerAccept { timestamp } = msg {
                        info!("RELAY({}): Connected at {timestamp}", self.id);

                        _ = EVENT_BUS
                            .0
                            .send(InternalEvent::Connection { state: ConnectionState::Connected });

                        *CONNECTION.write() = Some(conn)
                    } else if let ServerReject { reason } = msg {
                        info!("RELAY({}): Rejected because {reason}", self.id);

                        // or something else maybe
                        return Err(RelayConnError::Continue);
                    }
                }
            },
            Err(ConnectionError::TimedOut) => {
                _ = EVENT_BUS.0.send(InternalEvent::Connection { state: ConnectionState::Failed });

                _ = self.downvote().await;

                Err(RelayConnError::Continue)
            },
            Err(err) => Err(err.into()),
        }
    }
}
