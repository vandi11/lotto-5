# Java CSV importer

This Java 17 console application imports the supplied semicolon-separated UTF-8 CSV files into the SQLite database created by the Laravel API migrations.

## Build

From this directory:

```sh
mvn package
```

## Import the supplied data

Run from `java-console-importer`:

```sh
java -jar target/lotto-console-importer-1.0.0.jar \
  ./csv/gamers.csv \
  ./csv/data5.csv \
  ../lotto-api/database/database.sqlite
```

First create the database and tables by following the setup instructions in `../lotto-api/README.md`. The importer checks for those tables and refuses to create a blank SQLite file in their place. It imports gamer IDs in CSV order (the data file refers to those IDs), and writes the Laravel timestamp columns as well.

The default database path is `../lotto-api/database/database.sqlite`. You can override it with the optional third argument or the `LOTTO_DB_PATH` environment variable.

The importer refuses to add duplicate sample rows when either table is non-empty. To replace both tables with the supplied CSV contents, add `--replace` to the command. That option deletes existing tips and gamers inside a transaction before importing. The source CSVs contain 6 gamers and 99 tip rows.
