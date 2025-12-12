use crate::db::NETWORK_DB;

const RELAYS_SQL: &str = include_str!("../db/relays.sql");

pub fn initial_execute() -> anyhow::Result<()> {
    ////////////////////////
    //  NETWORK DATABASE  //
    ////////////////////////
    NETWORK_DB.lock().execute(RELAYS_SQL, ())?;

    Ok(())
}
