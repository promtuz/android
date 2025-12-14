use anyhow::Result;
use common::PROTOCOL_VERSION;
use common::msg::cbor::{FromCbor, ToCbor};
use common::msg::client::{ClientRequest, ClientResponse, RelayDescriptor};
use log::error;
use quinn::VarInt;
use rusqlite::Row;
use tokio::io::{AsyncReadExt, AsyncWriteExt};

use crate::events::InternalEvent;
use crate::events::connection::ConnectionState;
use crate::quic::dialer::connect_to_any_seed;
use crate::{EVENT_BUS, endpoint};
use crate::data::ResolverSeed;
use crate::db::NETWORK_DB;
use crate::utils::systime;

/// Local Database Representation of Relay
#[derive(Debug)]
pub struct Relay {
    pub id: String,
    pub host: String,
    pub port: u16,
    last_avg_latency: Option<u64>,
    last_seen: u64,
    last_connect: Option<u64>,
    last_version: u16,
}

/// TODO: Create unit testing for this
impl Relay {
    /// "Best" how?
    ///
    /// - Must match current version
    /// - Lowest last avg latency if exists
    /// - Lowest last seen
    /// - Lowest last connect if exists
    pub fn fetch_best() -> rusqlite::Result<Self> {
        let conn = NETWORK_DB.lock();

        conn.query_row(
            "SELECT * FROM relays 
                  WHERE last_version = ?1 
                  ORDER BY 
                      last_seen DESC, 
                      last_connect DESC, 
                      last_avg_latency ASC 
                  LIMIT 1",
            [PROTOCOL_VERSION],
            Self::from_row,
        )
    }

    fn from_row(row: &Row) -> rusqlite::Result<Self> {
        Ok(Self {
            id: row.get("id")?,
            host: row.get("host")?,
            port: row.get("port")?,
            last_avg_latency: row.get("last_avg_latency")?,
            last_seen: row.get("last_seen")?,
            last_connect: row.get("last_connect")?,
            last_version: row.get("last_version")?,
        })
    }

    pub fn refresh(relays: &[RelayDescriptor]) -> Result<u8> {
        let conn = NETWORK_DB.lock();

        let mut stmt = conn.prepare(
            "INSERT INTO relays (
                    id, host, port, last_seen, last_version
                 )
                 VALUES (?1, ?2, ?3, ?4, ?5)
                 ON CONFLICT(id) DO UPDATE SET
                    host         = excluded.host,
                    port         = excluded.port,
                    last_seen    = excluded.last_seen,
                    last_version = excluded.last_version",
        )?;

        relays.iter().for_each(|r| {
            _ = stmt.execute((
                r.id.to_string(),
                r.addr.ip().to_string(),
                r.addr.port(),
                systime().as_millis() as u64,
                PROTOCOL_VERSION,
            ));
        });

        Ok(0)
    }

    /// Resolves relays by connected to one of the resolver seed provided
    pub async fn resolve(seeds: &[ResolverSeed]) {
        _ = EVENT_BUS.0.send(InternalEvent::Connection { state: ConnectionState::Resolving });

        let conn = match connect_to_any_seed(endpoint!(), seeds).await {
            Ok(conn) => conn,
            Err(_) => {
                _ = EVENT_BUS.0.send(InternalEvent::Connection { state: ConnectionState::Failed });
                return;
            },
        };

        let req = ClientRequest::GetRelays().pack().unwrap();

        if let Ok((mut send, mut recv)) = conn.open_bi().await {
            _ = send.write_all(&req).await;
            _ = send.flush().await;

            use CRes::*;
            use ClientResponse as CRes;

            if let Ok(packet_size) = recv.read_u32().await {
                let mut packet = vec![0u8; packet_size as usize];

                if recv.read_exact(&mut packet).await.is_err() {
                    return;
                }

                match CRes::from_cbor(&packet) {
                    Ok(cres) =>
                    {
                        #[allow(irrefutable_let_patterns)]
                        if let GetRelays { relays } = cres {
                            _ = Relay::refresh(&relays);

                            match Relay::fetch_best() {
                                Ok(relay) => _ = relay.connect().await,
                                Err(err) => {
                                    error!("DB: FETCHING BEST RELAY FAILED {}", err);
                                },
                            }

                            conn.close(VarInt::from_u32(1), &[]);
                        }
                    },
                    Err(err) => {
                        error!("API: CLIENT RES DECODE ERR : {}", err);
                    },
                }
            }
        }
    }
}
