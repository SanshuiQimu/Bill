mod cli;
mod commands;
mod models;
mod storage;

use clap::Parser;

fn main() {
    let args = cli::Cli::parse();
    commands::handle_command(args.command);
}
