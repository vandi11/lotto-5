package hu.vandi.lotto;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Imports the supplied semicolon-separated UTF-8 files into the Laravel SQLite schema. */
public final class LottoImporter {
    private static final String USAGE = "Usage: java -jar target/lotto-console-importer-1.0.0.jar "
            + "<gamers.csv> <data5.csv> [database.sqlite] [--replace]\n"
            + "Example: java -jar target/lotto-console-importer-1.0.0.jar "
            + "./csv/gamers.csv ./csv/data5.csv "
            + "../lotto-api/database/database.sqlite";
    private static final DateTimeFormatter SQL_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private LottoImporter() {}

    public static void main(String[] args) {
        try {
            Arguments arguments = Arguments.parse(args);
            List<String> gamers = readGamers(arguments.gamersCsv());
            List<Tip> tips = readTips(arguments.dataCsv(), gamers.size());
            int[] imported = importData(arguments.database(), gamers, tips, arguments.replace());
            System.out.printf("Successfully imported %d gamer row(s) and %d tip row(s) into %s.%n",
                    imported[0], imported[1], arguments.database().toAbsolutePath());
        } catch (UsageException e) {
            System.err.println(e.getMessage());
            System.err.println(USAGE);
            System.exit(2);
        } catch (Exception e) {
            System.err.println("Import failed: " + e.getMessage());
            System.exit(1);
        }
    }

    private static List<String> readGamers(Path path) throws IOException {
        List<List<String>> rows = readCsv(path);
        requireHeader(rows, List.of("Name"), path);
        List<String> names = new ArrayList<>();
        for (int i = 1; i < rows.size(); i++) {
            List<String> row = rows.get(i);
            if (row.size() != 1 || row.get(0).isBlank()) {
                throw rowError(path, i + 1, "expected one non-empty gamer name");
            }
            String name = row.get(0).trim();
            if (name.length() > 60) {
                throw rowError(path, i + 1, "gamer name exceeds the 60-character database limit");
            }
            names.add(name);
        }
        if (names.isEmpty()) throw new IllegalArgumentException(path + " contains no gamers.");
        return names;
    }

    private static List<Tip> readTips(Path path, int gamerCount) throws IOException {
        List<List<String>> rows = readCsv(path);
        requireHeader(rows, List.of("GamersId", "Sz1", "Sz2", "Sz3", "Sz4", "Sz5"), path);
        List<Tip> tips = new ArrayList<>();
        for (int i = 1; i < rows.size(); i++) {
            List<String> row = rows.get(i);
            if (row.size() != 6) throw rowError(path, i + 1, "expected six columns");
            int gamerId = parseInt(row.get(0), path, i + 1, "GamersId");
            if (gamerId < 1 || gamerId > gamerCount) {
                throw rowError(path, i + 1, "GamersId " + gamerId + " has no matching row in gamers.csv");
            }
            int[] numbers = new int[5];
            for (int n = 0; n < numbers.length; n++) {
                numbers[n] = parseInt(row.get(n + 1), path, i + 1, "Sz" + (n + 1));
                if (numbers[n] < 1 || numbers[n] > 90) {
                    throw rowError(path, i + 1, "lottery numbers must be between 1 and 90");
                }
            }
            // Historical source rows are kept as provided, including any repeated values.
            tips.add(new Tip(gamerId, numbers));
        }
        if (tips.isEmpty()) throw new IllegalArgumentException(path + " contains no tips.");
        return tips;
    }

    private static List<List<String>> readCsv(Path path) throws IOException {
        if (!Files.isRegularFile(path) || !Files.isReadable(path)) {
            throw new IllegalArgumentException("File does not exist or cannot be read: " + path);
        }
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        List<List<String>> rows = new ArrayList<>();
        for (int lineNumber = 0; lineNumber < lines.size(); lineNumber++) {
            String line = lines.get(lineNumber);
            if (lineNumber == 0 && line.startsWith("\uFEFF")) line = line.substring(1);
            if (line.isBlank()) continue;
            rows.add(parseSemicolonRow(line, path, lineNumber + 1));
        }
        if (rows.isEmpty()) throw new IllegalArgumentException(path + " is empty.");
        return rows;
    }

    private static List<String> parseSemicolonRow(String line, Path path, int lineNumber) {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    field.append('"');
                    i++;
                } else {
                    quoted = !quoted;
                }
            } else if (c == ';' && !quoted) {
                fields.add(field.toString());
                field.setLength(0);
            } else {
                field.append(c);
            }
        }
        if (quoted) throw rowError(path, lineNumber, "unterminated quoted field");
        fields.add(field.toString());
        return fields;
    }

    private static void requireHeader(List<List<String>> rows, List<String> expected, Path path) {
        List<String> actual = rows.get(0).stream().map(String::trim).toList();
        if (!actual.equals(expected)) {
            throw new IllegalArgumentException(path + " has an unexpected header. Expected: "
                    + String.join(";", expected));
        }
    }

    private static int parseInt(String value, Path path, int line, String column) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw rowError(path, line, column + " must be an integer");
        }
    }

    private static IllegalArgumentException rowError(Path path, int line, String message) {
        return new IllegalArgumentException(path + ": line " + line + ": " + message);
    }

    private static int[] importData(Path database, List<String> gamers, List<Tip> tips, boolean replace)
            throws SQLException, IOException {
        Path absoluteDatabase = database.toAbsolutePath().normalize();
        Path parent = absoluteDatabase.getParent();
        if (parent != null) Files.createDirectories(parent);

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + absoluteDatabase)) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA foreign_keys = ON");
                ensureTablesExist(statement);
                int existingGamers = countRows(statement, "gamers");
                int existingTips = countRows(statement, "data");
                if (!replace && (existingGamers != 0 || existingTips != 0)) {
                    throw new IllegalStateException("The database already contains " + existingGamers
                            + " gamer(s) and " + existingTips + " tip(s). Use --replace to replace them.");
                }

                connection.setAutoCommit(false);
                try {
                    if (replace) {
                        statement.executeUpdate("DELETE FROM data");
                        statement.executeUpdate("DELETE FROM gamers");
                    }
                    String now = LocalDateTime.now().format(SQL_TIMESTAMP);
                    insertGamers(connection, gamers, now);
                    insertTips(connection, tips, now);
                    connection.commit();
                    return new int[]{gamers.size(), tips.size()};
                } catch (Exception e) {
                    connection.rollback();
                    if (e instanceof SQLException sqlException) throw sqlException;
                    if (e instanceof RuntimeException runtimeException) throw runtimeException;
                    throw new SQLException("Database import failed", e);
                }
            }
        }
    }

    private static void ensureTablesExist(Statement statement) throws SQLException {
        try (ResultSet result = statement.executeQuery(
                "SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name IN ('gamers', 'data')")) {
            if (!result.next() || result.getInt(1) != 2) {
                throw new IllegalStateException("Laravel tables are missing. Run `php artisan migrate` in lotto-api first.");
            }
        }
        Set<String> columns = new HashSet<>();
        try (ResultSet result = statement.executeQuery("PRAGMA table_info(data)")) {
            while (result.next()) columns.add(result.getString("name"));
        }
        if (!columns.containsAll(Set.of("id", "gamers_id", "sz1", "sz2", "sz3", "sz4", "sz5", "created_at", "updated_at"))) {
            throw new IllegalStateException("The data table does not match the Laravel migration schema.");
        }
    }

    private static int countRows(Statement statement, String table) throws SQLException {
        try (ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM " + table)) {
            result.next();
            return result.getInt(1);
        }
    }

    private static void insertGamers(Connection connection, List<String> gamers, String timestamp) throws SQLException {
        String sql = "INSERT INTO gamers (id, name, created_at, updated_at) VALUES (?, ?, ?, ?)";
        try (PreparedStatement insert = connection.prepareStatement(sql)) {
            for (int i = 0; i < gamers.size(); i++) {
                insert.setInt(1, i + 1);
                insert.setString(2, gamers.get(i));
                insert.setString(3, timestamp);
                insert.setString(4, timestamp);
                insert.addBatch();
            }
            insert.executeBatch();
        }
    }

    private static void insertTips(Connection connection, List<Tip> tips, String timestamp) throws SQLException {
        String sql = "INSERT INTO data (gamers_id, sz1, sz2, sz3, sz4, sz5, created_at, updated_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement insert = connection.prepareStatement(sql)) {
            for (Tip tip : tips) {
                insert.setInt(1, tip.gamerId());
                for (int i = 0; i < 5; i++) insert.setInt(i + 2, tip.numbers()[i]);
                insert.setString(7, timestamp);
                insert.setString(8, timestamp);
                insert.addBatch();
            }
            insert.executeBatch();
        }
    }

    private record Tip(int gamerId, int[] numbers) {}

    private record Arguments(Path gamersCsv, Path dataCsv, Path database, boolean replace) {
        static Arguments parse(String[] args) {
            List<String> positional = new ArrayList<>();
            boolean replace = false;
            for (String arg : args) {
                if (arg.equals("--replace")) replace = true;
                else positional.add(arg);
            }
            if (positional.size() < 2 || positional.size() > 3) throw new UsageException("Missing or invalid arguments.");
            Path database;
            if (positional.size() == 3) database = Path.of(positional.get(2));
            else {
                String envPath = System.getenv("LOTTO_DB_PATH");
                database = envPath == null || envPath.isBlank()
                        ? Path.of("../lotto-api/database/database.sqlite") : Path.of(envPath);
            }
            return new Arguments(Path.of(positional.get(0)), Path.of(positional.get(1)), database, replace);
        }
    }

    private static final class UsageException extends RuntimeException {
        UsageException(String message) { super(message); }
    }
}
