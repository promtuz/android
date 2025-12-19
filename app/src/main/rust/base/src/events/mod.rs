use serde::Serialize;

use crate::events::connection::ConnectionState;
use crate::events::identity::Identity;

pub mod connection;
pub mod identity;
pub mod poll;

#[derive(Serialize, Debug, Clone, PartialEq, Eq)]
#[allow(unused)]
pub enum InternalEvent {
    Connection { state: ConnectionState },
    Identity { event: Identity },
}