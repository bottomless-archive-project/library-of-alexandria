package com.github.bottomlessarchive.loa.vault.service.location.sqlite.service;

import com.github.bottomlessarchive.loa.vault.domain.exception.StorageAccessException;
import com.github.bottomlessarchive.loa.vault.service.location.sqlite.configuration.SqliteConfigurationProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
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
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class SqliteConnectionManager {

    private static final long IDLE_TIMEOUT_MINUTES = 3;

    private final SqliteConfigurationProperties sqliteConfigurationProperties;

    private final ConcurrentHashMap<Integer, Connection> writeConnections = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, Instant> lastWriteTime = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, Connection> readConnections = new ConcurrentHashMap<>();
    private volatile int activeVaultFileNumber;
    private final AtomicInteger activeDocumentCount = new AtomicInteger();
    private final ReentrantLock assignLock = new ReentrantLock();

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

        final Connection writeConnection = openWriteConnection(activeVaultFileNumber);
        writeConnections.put(activeVaultFileNumber, writeConnection);
        lastWriteTime.put(activeVaultFileNumber, Instant.now());

        final int rowCount = countRows(writeConnection);
        activeDocumentCount.set(rowCount);

        log.info("Initialized SQLite vault. Active file: vault-{}, document count: {}.",
                String.format("%06d", activeVaultFileNumber), rowCount);
    }

    /**
     * Assigns a vault file number for a document. This must be called before the document is persisted to MongoDB,
     * so the file number is stored as part of the document metadata. The assignment is protected by a lock and
     * triggers rotation when the batch size is reached.
     *
     * @param docId the document ID being assigned
     * @return the vault file number the document should be written to
     */
    public int assignVaultFileNumber(final String docId) {
        assignLock.lock();
        try {
            if (activeDocumentCount.get() >= sqliteConfigurationProperties.batchSize()) {
                rotate();
            }

            final int assignedFileNumber = activeVaultFileNumber;
            activeDocumentCount.incrementAndGet();

            log.debug("Assigned document {} to vault file vault-{}.", docId,
                    String.format("%06d", assignedFileNumber));

            return assignedFileNumber;
        } finally {
            assignLock.unlock();
        }
    }

    /**
     * Inserts a document into the specified vault file. The file number must have been previously obtained via
     * {@link #assignVaultFileNumber(String)}.
     *
     * @param fileNumber the vault file to write to
     * @param docId      the document ID
     * @param content    the document content
     */
    public void insertDocument(final int fileNumber, final String docId, final InputStream content) {
        final Connection connection = getOrOpenWriteConnection(fileNumber);

        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO documents (id, content) VALUES (?, ?)")) {
            stmt.setString(1, docId);
            stmt.setBytes(2, content.readAllBytes());
            stmt.executeUpdate();
        } catch (final Exception e) {
            throw new StorageAccessException("Unable to insert document into SQLite vault!", e);
        }

        lastWriteTime.put(fileNumber, Instant.now());
    }

    public InputStream readDocument(final int fileNumber, final String docId) {
        final Connection connection = getReadConnection(fileNumber);

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
        final Connection connection = getReadConnection(fileNumber);

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
        final Connection connection = getOrOpenWriteConnection(fileNumber);

        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM documents WHERE id = ?")) {
            stmt.setString(1, docId);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw new StorageAccessException("Unable to delete document from SQLite vault!", e);
        }

        lastWriteTime.put(fileNumber, Instant.now());
    }

    public long getAvailableSpace() {
        try {
            return Files.getFileStore(sqliteConfigurationProperties.path()).getUsableSpace();
        } catch (final IOException e) {
            throw new StorageAccessException("Unable to determine available space!", e);
        }
    }

    @Scheduled(fixedDelay = 60000)
    public void closeIdleWriteConnections() {
        final Instant cutoff = Instant.now().minus(IDLE_TIMEOUT_MINUTES, ChronoUnit.MINUTES);

        for (final Map.Entry<Integer, Instant> entry : lastWriteTime.entrySet()) {
            final int fileNumber = entry.getKey();

            // Never close the active file's write connection
            if (fileNumber == activeVaultFileNumber) {
                continue;
            }

            if (entry.getValue().isBefore(cutoff)) {
                final Connection connection = writeConnections.remove(fileNumber);

                if (connection != null) {
                    try {
                        connection.close();
                        log.info("Closed idle write connection for vault-{}.",
                                String.format("%06d", fileNumber));
                    } catch (final SQLException e) {
                        log.warn("Error closing idle write connection for vault-{}.",
                                String.format("%06d", fileNumber), e);
                    }
                }

                lastWriteTime.remove(fileNumber);
            }
        }
    }

    @PreDestroy
    public void close() {
        for (final Map.Entry<Integer, Connection> entry : writeConnections.entrySet()) {
            try {
                entry.getValue().close();
            } catch (final SQLException e) {
                log.warn("Error closing write connection for vault-{}.",
                        String.format("%06d", entry.getKey()), e);
            }
        }

        writeConnections.clear();
        lastWriteTime.clear();

        for (final Connection connection : readConnections.values()) {
            try {
                connection.close();
            } catch (final SQLException e) {
                log.warn("Error closing read connection.", e);
            }
        }

        readConnections.clear();
    }

    private void rotate() {
        log.info("Rotating SQLite vault file. Current file vault-{} reached {} documents.",
                String.format("%06d", activeVaultFileNumber), activeDocumentCount.get());

        activeVaultFileNumber++;
        activeDocumentCount.set(0);

        final Connection writeConnection = openWriteConnection(activeVaultFileNumber);
        writeConnections.put(activeVaultFileNumber, writeConnection);
        lastWriteTime.put(activeVaultFileNumber, Instant.now());

        log.info("Rotated to new SQLite vault file: vault-{}.",
                String.format("%06d", activeVaultFileNumber));
    }

    private Connection getOrOpenWriteConnection(final int fileNumber) {
        return writeConnections.computeIfAbsent(fileNumber, fn -> {
            log.info("Reopening write connection for vault-{}.", String.format("%06d", fn));
            return openWriteConnection(fn);
        });
    }

    private Connection getReadConnection(final int fileNumber) {
        // If a write connection is open for this file, use it for reads too
        final Connection writeConn = writeConnections.get(fileNumber);

        if (writeConn != null) {
            return writeConn;
        }

        return readConnections.computeIfAbsent(fileNumber, this::openReadConnection);
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
