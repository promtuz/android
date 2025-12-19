use anyhow::Result;
use anyhow::anyhow;
use common::PROTOCOL_VERSION;
use common::msg::cbor::FromCbor;
use common::msg::cbor::ToCbor;
use common::msg::client::ClientRequest;
use common::msg::client::ClientResponse;
use common::msg::client::RelayDescriptor;
use log::error;
use log::info;
use quinn::VarInt;
use rusqlite::Row;
use rusqlite::params;
use tokio::io::AsyncReadExt;
use tokio::io::AsyncWriteExt;

use crate::ENDPOINT;
use crate::EVENT_BUS;
use crate::data::ResolverSeed;
use crate::db::NETWORK_DB;
use crate::events::InternalEvent;
use crate::events::connection::ConnectionState;
use crate::quic::dialer::connect_to_any_seed;
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

    reputation: i16,
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
                  WHERE 
                    last_version = ?1 AND
                    reputation >= 0
                  ORDER BY 
                      reputation DESC,
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
            reputation: row.get("reputation")?,
        })
    }

    pub fn refresh(relays: &[RelayDescriptor]) -> Result<u8> {
        let conn = NETWORK_DB.lock();

        // Increase reputation as resolver says so
        let mut stmt = conn.prepare(
            "INSERT INTO relays (
                    id, host, port, last_seen, last_version
                 )
                 VALUES (?1, ?2, ?3, ?4, ?5)
                 ON CONFLICT(id) DO UPDATE SET
                    host         = excluded.host,
                    port         = excluded.port,
                    last_seen    = excluded.last_seen,
                    last_version = excluded.last_version,
                    reputation   = reputation + 1",
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
    ///
    /// Any type of failure except ui related is not tolerated and will return an error
    pub async fn resolve(seeds: &[ResolverSeed]) -> anyhow::Result<()> {
        _ = EVENT_BUS.0.send(InternalEvent::Connection { state: ConnectionState::Resolving });

        let conn =
            match connect_to_any_seed(ENDPOINT.get().ok_or(anyhow!("API not initialized"))?, seeds)
                .await
            {
                Ok(conn) => conn,
                Err(err) => {
                    _ = EVENT_BUS
                        .0
                        .send(InternalEvent::Connection { state: ConnectionState::Failed });
                    return Err(err);
                },
            };

        let req = ClientRequest::GetRelays().pack().unwrap();

        let (mut send, mut recv) = conn.open_bi().await?;

        send.write_all(&req).await?;
        send.flush().await?;

        use CRes::*;
        use ClientResponse as CRes;

        let packet_size = recv.read_u32().await?;
        let mut packet = vec![0u8; packet_size as usize];

        recv.read_exact(&mut packet).await?;

        match CRes::from_cbor(&packet) {
            Ok(cres) => {
                #[allow(irrefutable_let_patterns)]
                if let GetRelays { relays } = cres {
                    Relay::refresh(&relays)?;

                    // we letting the connect function do this itself

                    // match Relay::fetch_best() {
                    //     Ok(relay) => _ = relay.connect().await,
                    //     Err(err) => {
                    //         error!("DB: FETCHING BEST RELAY FAILED {}", err);
                    //     },
                    // }

                    conn.close(VarInt::from_u32(1), &[]);
                }
            },
            Err(err) => return Err(err),
        }

        Ok(())
    }

    /// Reduces reputation of relay by 1
    ///
    /// Returns updated reputation
    pub async fn downvote(&self) -> anyhow::Result<i16> {
        info!("RELAY({}): Downvoting", self.id);
        let conn = NETWORK_DB.lock();

        Ok(conn.query_one(
            "UPDATE relays SET reputation = reputation - 1 WHERE id = ?1 RETURNING reputation;",
            params![self.id],
            |r| r.get(0),
        )?)
    }
}
