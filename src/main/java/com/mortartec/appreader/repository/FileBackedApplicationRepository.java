package com.mortartec.appreader.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mortartec.appreader.model.MortgageApplication;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Local storage, implemented as a JSON file on the local filesystem
 * (./data/applications.json by default), backed by an in-memory cache so
 * reads never hit disk.
 *
 * This intentionally avoids a database (Postgres/MySQL/H2/etc.) — the task
 * calls for "local storage", and a flat JSON file next to the running
 * process is the simplest thing that satisfies that while still surviving a
 * restart, which a plain in-memory map would not.
 */
@Repository
public class FileBackedApplicationRepository implements LocalStorageApplicationRepository {

    private static final Logger log = LoggerFactory.getLogger(FileBackedApplicationRepository.class);

    private final Path storageFile;
    private final ObjectMapper objectMapper;
    private final Map<String, MortgageApplication> cache = new ConcurrentHashMap<>();
    private final ReentrantLock writeLock = new ReentrantLock();

    public FileBackedApplicationRepository(
            @Value("${app.storage.file:./data/applications.json}") String storageFilePath) {
        this.storageFile = Path.of(storageFilePath);
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .enable(SerializationFeature.INDENT_OUTPUT);
    }

    @PostConstruct
    void loadFromDisk() {
        try {
            Files.createDirectories(storageFile.toAbsolutePath().getParent());
            if (Files.exists(storageFile) && Files.size(storageFile) > 0) {
                List<MortgageApplication> loaded = objectMapper.readValue(
                        storageFile.toFile(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, MortgageApplication.class)
                );
                loaded.forEach(app -> cache.put(app.getId(), app));
                log.info("Loaded {} application(s) from local storage at {}", loaded.size(), storageFile.toAbsolutePath());
            } else {
                log.info("No existing local storage file found; starting empty ({})", storageFile.toAbsolutePath());
            }
        } catch (IOException ex) {
            log.warn("Could not read local storage file {} - starting with empty storage: {}",
                    storageFile.toAbsolutePath(), ex.getMessage());
        }
    }

    @Override
    public MortgageApplication save(MortgageApplication application) {
        cache.put(application.getId(), application);
        flushToDisk();
        return application;
    }

    @Override
    public List<MortgageApplication> findAll() {
        return new ArrayList<>(cache.values());
    }

    @Override
    public Optional<MortgageApplication> findById(String id) {
        return Optional.ofNullable(cache.get(id));
    }

    private void flushToDisk() {
        writeLock.lock();
        try {
            objectMapper.writeValue(storageFile.toFile(), new ArrayList<>(cache.values()));
        } catch (IOException ex) {
            log.error("Failed to persist local storage file {}: {}", storageFile.toAbsolutePath(), ex.getMessage());
        } finally {
            writeLock.unlock();
        }
    }
}
