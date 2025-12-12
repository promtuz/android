use anyhow::Result;
use common::msg::client::RelayDescriptor;
use rusqlite::Row;
use rusqlite::params_from_iter;

use crate::db::NETWORK_DB;

/// Local Database Representation of Relay
#[derive(Debug)]
pub struct Relay {
    pub id: String,
    pub host: String,
    pub port: u16,
    pub last_avg_latency: Option<u64>,
    pub last_seen: u64,
    pub last_connect: Option<u64>,
    pub last_version: u16,
}

impl Relay {
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

        let q_marks = std::iter::repeat_n("?", relays.len()).collect::<Vec<_>>().join(",");

        let sql = format!("SELECT * FROM relays WHERE id IN ({})", q_marks);

        let mut stmt = conn.prepare(&sql)?;

        let db_relays = stmt
            .query_map(params_from_iter(relays.iter().map(|r| r.id.to_string())), Self::from_row)?
            .collect::<Vec<rusqlite::Result<Relay>>>();

        Ok(db_relays.len() as u8)
    }
}
