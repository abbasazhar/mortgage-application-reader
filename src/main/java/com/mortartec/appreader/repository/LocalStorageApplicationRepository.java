package com.mortartec.appreader.repository;

import com.mortartec.appreader.model.MortgageApplication;

import java.util.List;
import java.util.Optional;

/**
 * "Local storage" for mortgage applications — no external database, just
 * this service's own disk/process. Kept as an interface so the storage
 * mechanism (file-backed JSON, in-memory, etc.) can change without touching
 * the service or controller layers.
 */
public interface LocalStorageApplicationRepository {

    MortgageApplication save(MortgageApplication application);

    List<MortgageApplication> findAll();

    Optional<MortgageApplication> findById(String id);
}
