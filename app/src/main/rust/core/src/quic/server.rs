use std::net::IpAddr;
use std::net::SocketAddr;
use std::str::FromStr;
use std::time::Duration;

use anyhow::Result;
use log::info;
use quinn::ConnectionError;

use crate::ENDPOINT;
use crate::EVENT_BUS;
use crate::data::relay::Relay;
use crate::events::InternalEvent;
use crate::events::connection::ConnectionState;

impl Relay {
    pub async fn connect(&self) -> Result<()> {
        let addr = SocketAddr::new(IpAddr::from_str(&self.host.clone())?, self.port);

        info!("RELAY({}): CONNECTING AT {}", self.id, addr);

        _ = EVENT_BUS.0.send(InternalEvent::Connection { state: ConnectionState::Connecting });

        match ENDPOINT.get().unwrap().connect(addr, &self.id)?.await {
            Ok(conn) => {
                _ = EVENT_BUS
                    .0
                    .send(InternalEvent::Connection { state: ConnectionState::Handshaking });

                let (mut send, mut recv) = conn.open_bi().await?;

                // STUCK

                /////////////////////////////////////////////
                //==============|| IMITATING ||============//
                /////////////////////////////////////////////

                tokio::time::sleep(Duration::from_millis(650)).await;

                _ = EVENT_BUS
                    .0
                    .send(InternalEvent::Connection { state: ConnectionState::Connected });

                info!("RELAY({}): CONNECTED", self.id);
            },
            Err(ConnectionError::TimedOut) => {
              // very probable that relay is not available now
              // tasks?
              // consider this relay "bad", basically downvote
              // return something like "Continue"
            },
            Err(err) => return Err(err.into())
        };

        Ok(())
    }
}
