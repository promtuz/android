//! TODO: Minify SQL before executing

use log::info;
use once_cell::sync::Lazy;
use parking_lot::Mutex;
use rusqlite::Connection;

use crate::PACKAGE_NAME;

// static DB_PATH: &str = &format!("/data/data/{PACKAGE_NAME}/databases/");

fn db(file_name: &'static str) -> String {
    format!("/data/data/{PACKAGE_NAME}/databases/{file_name}.db")
}

/// Connection to any sqlite network db
pub static NETWORK_DB: Lazy<Mutex<Connection>> = Lazy::new(|| {
    let db = Mutex::new(Connection::open(db("network")).expect("db open failed"));
    info!("DB: Network Database Connected");
    db
});
