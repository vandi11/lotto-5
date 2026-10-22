# Lotto REST API

Laravel API for the `Gamers` and `Data` tables from the assignment. Laravel migrations own the schema; the Java importer can insert into those same tables.

## Requirements and startup

- PHP 8.2+ with PDO SQLite (or PDO MySQL)
- Composer

From this directory, install dependencies and initialize the SQLite database:

```sh
composer install
cp .env.example .env
php artisan key:generate
mkdir -p database
touch database/database.sqlite
php artisan migrate
php artisan serve
```

The API then listens on `http://127.0.0.1:8000/api`. For MySQL/MariaDB, change the `DB_*` values in `.env` and set `DB_CONNECTION=mysql` before running the migration.

## Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| GET | `/api/Data` | List tips |
| POST | `/api/Data` | Create a tip |
| GET | `/api/Data/{id}` | Get one tip |
| PUT | `/api/Data/{id}` | Replace a tip |
| DELETE | `/api/Data/{id}` | Delete a tip |
| GET | `/api/Gamers` | List gamers, each with their tips |
| GET | `/api/Gamers/{id}` | Get one gamer with their tips |

Create/update request body (JSON):

```json
{
  "gamersId": 1,
  "sz1": 4,
  "sz2": 18,
  "sz3": 27,
  "sz4": 52,
  "sz5": 89
}
```

Numbers must be unique and between 1 and 90, and `gamersId` must refer to an existing gamer. Responses use the assignment's camelCase `gamersId` field. Validation errors return HTTP 422 JSON; missing records return 404. CORS allows all origins as requested by the brief.

## Shared database with Java

Default database is `database/database.sqlite`. Set `DB_DATABASE` to an absolute path if another process (such as the Java console importer) needs to connect reliably. The tables are `gamers(id, name, created_at, updated_at)` and `data(id, gamers_id, sz1, sz2, sz3, sz4, sz5, created_at, updated_at)`. `data.gamers_id` references `gamers.id`. Import the gamer rows first, then the tips, preserving IDs if the CSV data expects them.

The companion Java importer is in `../java-console-importer`. Build it with Maven and follow its README to load the supplied CSV files into this database.
