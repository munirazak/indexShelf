package com.collabera.librarysystem.config;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Runs {@code schema.sql} before the app DataSource starts.
 * {@code CREATE DATABASE} is executed against the default {@code postgres} DB
 * when the target database is missing; remaining statements run against the app DB.
 */
public class SchemaBootstrap implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        Environment env = event.getEnvironment();
        String jdbcUrl = env.getRequiredProperty("spring.datasource.url");
        String username = env.getRequiredProperty("spring.datasource.username");
        String password = env.getProperty("spring.datasource.password", "");
        String databaseName = jdbcUrl.substring(jdbcUrl.lastIndexOf('/') + 1).split("\\?")[0];
        String adminUrl = jdbcUrl.substring(0, jdbcUrl.lastIndexOf('/') + 1) + "postgres";

        try {
            List<String> createDb = new ArrayList<>();
            List<String> tables = new ArrayList<>();
            for (String sql : parseStatements(loadSchemaSql())) {
                if (sql.toUpperCase(Locale.ROOT).startsWith("CREATE DATABASE")) {
                    createDb.add(sql);
                } else {
                    tables.add(sql);
                }
            }

            try (Connection conn = DriverManager.getConnection(adminUrl, username, password);
                 Statement stmt = conn.createStatement()) {
                if (!databaseExists(stmt, databaseName)) {
                    for (String sql : createDb) {
                        stmt.execute(sql);
                    }
                }
            }

            try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
                 Statement stmt = conn.createStatement()) {
                for (String sql : tables) {
                    stmt.execute(sql);
                }
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to run schema.sql", ex);
        }
    }

    private boolean databaseExists(Statement stmt, String databaseName) throws Exception {
        try (ResultSet rs = stmt.executeQuery(
                "SELECT 1 FROM pg_database WHERE datname = '" + databaseName + "'")) {
            return rs.next();
        }
    }

    private String loadSchemaSql() throws Exception {
        return StreamUtils.copyToString(
                new ClassPathResource("schema.sql").getInputStream(), StandardCharsets.UTF_8);
    }

    private List<String> parseStatements(String script) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String line : script.split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("--")) {
                continue;
            }
            current.append(trimmed).append(' ');
            if (trimmed.endsWith(";")) {
                String sql = current.toString().trim();
                statements.add(sql.substring(0, sql.length() - 1).trim());
                current.setLength(0);
            }
        }
        return statements;
    }
}
