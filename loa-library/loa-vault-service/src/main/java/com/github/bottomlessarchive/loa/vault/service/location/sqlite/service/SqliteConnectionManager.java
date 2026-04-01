package com.github.bottomlessarchive.loa.vault.service.location.sqlite.service;

import com.github.bottomlessarchive.loa.vault.domain.exception.StorageAccessException;
import com.github.bottomlessarchive.loa.vault.service.location.sqlite.configuration.SqliteConfigurationProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value = "loa.vault.location.type", havingValue = "sqlite")
public class SqliteConnectionManager {

    private final SqliteConfigurationProperties sqliteConfigurationProperties;

    private final ConcurrentHashMap<Integer, Connection> readConnections = new ConcurrentHashMap<>();
    private volatile Connection activeWriteConnection;
    private volatile int activeVaultFileNumber;
    private final AtomicInteger activeDocumentCount = new AtomicInteger();
    private final ReentrantLock writeLock = new ReentrantLock();

    @PostConstruct
    public void initialize() {
        final Path vaultPath = sqliteConfigurationProperties.path();

        if (!Files.exists(vaultPath)) {
            try {
                log.info("Vault directory doesn't exist. Creating: {}.", vaultPath);
                Files.createDirectories(vaultPath);
            } catch (final IOException e) {
                throw new StorageAccessException("Unable to create vault directory!", e);
            }
        }

        int highestFileNumber = 0;

        try (Stream<Path> files = Files.list(vaultPath)) {
            for (final Path file : (Iterable<Path>) files::iterator) {
                final String fileName = file.getFileName().toString();

                if (fileName.startsWith("vault-") && fileName.endsWith(".db")) {
                    final String numberPart = fileName.substring(6, fileName.length() - 3);

                    try {
                        final int fileNumber = Integer.parseInt(numberPart);

                        if (fileNumber > highestFileNumber) {
                            highestFileNumber = fileNumber;
                        }
                    } catch (final NumberFormatException ignored) {
                        // Not a vault file, skip
                    }
                }
            }
        } catch (final IOException e) {
            throw new StorageAccessException("Unable to scan vault directory!", e);
        }

        if (highestFileNumber == 0) {
            highestFileNumber = 1;
        }

        activeVaultFileNumber = highestFileNumber;
        activeWriteConnection = openWriteConnection(activeVaultFileNumber);

        final int rowCount = countRows(activeWriteConnection);
        activeDocumentCount.set(rowCount);

        log.info("Initialized SQLite vault. Active file: vault-{}, document count: {}.",
                String.format("%06d", activeVaultFileNumber), rowCount);
    }

    public int getActiveVaultFileNumber() {
        return activeVaultFileNumber;
    }

    public void insertDocument(final String docId, final InputStream content) {
        writeLock.lock();
        try {
            try (PreparedStatement stmt = activeWriteConnection.prepareStatement(
                    "INSERT INTO documents (id, content) VALUES (?, ?)")) {
                stmt.setString(1, docId);
                stmt.setBytes(2, content.readAllBytes());
                stmt.executeUpdate();
            } catch (final Exception e) {
                throw new StorageAccessException("Unable to insert document into SQLite vault!", e);
            }

            activeDocumentCount.incrementAndGet();
            rotateIfNeeded();
        } finally {
            writeLock.unlock();
        }
    }

    public InputStream readDocument(final int fileNumber, final String docId) {
        final Connection connection = getConnection(fileNumber);

        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT content FROM documents WHERE id = ?")) {
            stmt.setString(1, docId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    final byte[] content = rs.getBytes("content");
                    return new ByteArrayInputStream(content);
                }
            }
        } catch (final SQLException e) {
            throw new StorageAccessException("Unable to read document from SQLite vault!", e);
        }

        throw new StorageAccessException("Document not found in SQLite vault: " + docId);
    }

    public boolean documentExists(final int fileNumber, final String docId) {
        final Connection connection = getConnection(fileNumber);

        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT 1 FROM documents WHERE id = ?")) {
            stmt.setString(1, docId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (final SQLException e) {
            throw new StorageAccessException("Unable to check document existence in SQLite vault!", e);
        }
    }

    public void deleteDocument(final int fileNumber, final String docId) {
        final Connection connection = getConnection(fileNumber);

        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM documents WHERE id = ?")) {
            stmt.setString(1, docId);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw new StorageAccessException("Unable to delete document from SQLite vault!", e);
        }
    }

    public long getAvailableSpace() {
        try {
            return Files.getFileStore(sqliteConfigurationProperties.path()).getUsableSpace();
        } catch (final IOException e) {
            throw new StorageAccessException("Unable to determine available space!", e);
        }
    }

    @PreDestroy
    public void close() {
        if (activeWriteConnection != null) {
            try {
                activeWriteConnection.close();
            } catch (final SQLException e) {
                log.warn("Error closing active write connection.", e);
            }
        }

        for (final Connection connection : readConnections.values()) {
            try {
                connection.close();
            } catch (final SQLException e) {
                log.warn("Error closing read connection.", e);
            }
        }

        readConnections.clear();
    }

    private Connection openWriteConnection(final int fileNumber) {
        try {
            final String url = "jdbc:sqlite:" + vaultFilePath(fileNumber);
            final Connection connection = DriverManager.getConnection(url);

            try (var stmt = connection.createStatement()) {
                stmt.execute("PRAGMA journal_mode=WAL");
            }
            try (var stmt = connection.createStatement()) {
                stmt.execute("PRAGMA synchronous=NORMAL");
            }

            initializeSchema(connection);

            return connection;
        } catch (final SQLException e) {
            throw new StorageAccessException("Unable to open SQLite write connection!", e);
        }
    }

    private Connection openReadConnection(final int fileNumber) {
        try {
            final String filePath = vaultFilePath(fileNumber).replace("\\", "/");
            final String url = "jdbc:sqlite:file:" + filePath + "?mode=ro";

            return DriverManager.getConnection(url);
        } catch (final SQLException e) {
            throw new StorageAccessException("Unable to open SQLite read connection!", e);
        }
    }

    private void initializeSchema(final Connection connection) throws SQLException {
        try (var stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS schema_version (version INTEGER NOT NULL)");
        }

        try (var stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT version FROM schema_version")) {
            if (!rs.next()) {
                try (var insertStmt = connection.createStatement()) {
                    insertStmt.execute("INSERT INTO schema_version (version) VALUES (1)");
                }
            }
        }

        try (var stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS documents (id TEXT PRIMARY KEY, content BLOB NOT NULL)");
        }
    }

    private void rotateIfNeeded() {
        if (activeDocumentCount.get() >= sqliteConfigurationProperties.batchSize()) {
            log.info("Rotating SQLite vault file. Current file vault-{} reached {} documents.",
                    String.format("%06d", activeVaultFileNumber), activeDocumentCount.get());

            readConnections.put(activeVaultFileNumber, openReadConnection(activeVaultFileNumber));

            try {
                activeWriteConnection.close();
            } catch (final SQLException e) {
                log.warn("Error closing write connection during rotation.", e);
            }

            activeVaultFileNumber++;
            activeWriteConnection = openWriteConnection(activeVaultFileNumber);
            activeDocumentCount.set(0);

            log.info("Rotated to new SQLite vault file: vault-{}.",
                    String.format("%06d", activeVaultFileNumber));
        }
    }

    private Connection getConnection(final int fileNumber) {
        if (fileNumber == activeVaultFileNumber) {
            return activeWriteConnection;
        }

        return readConnections.computeIfAbsent(fileNumber, this::openReadConnection);
    }

    private String vaultFilePath(final int fileNumber) {
        return sqliteConfigurationProperties.path()
                .resolve(String.format("vault-%06d.db", fileNumber)).toString();
    }

    private int countRows(final Connection connection) {
        try (var stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM documents")) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (final SQLException e) {
            throw new StorageAccessException("Unable to count documents in SQLite vault!", e);
        }
    }
}
