package org.example.repository;

import org.example.model.PlacementDrive;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Thread-safe in-memory implementation of PlacementDriveRepository.
 */
public class InMemoryPlacementDriveRepository implements PlacementDriveRepository {
    private final Map<String, PlacementDrive> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public PlacementDrive save(PlacementDrive drive) {
        if (drive == null) {
            throw new IllegalArgumentException("Placement drive cannot be null");
        }
        if (drive.getId() == null || drive.getId().trim().isEmpty()) {
            drive.setId("DRIVE-" + idGenerator.getAndIncrement());
        }
        storage.put(drive.getId(), drive);
        return drive;
    }

    @Override
    public Optional<PlacementDrive> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<PlacementDrive> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public List<PlacementDrive> findByCompanyId(String companyId) {
        if (companyId == null) return Collections.emptyList();
        return storage.values().stream()
                .filter(d -> Objects.equals(d.getCompanyId(), companyId))
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(String id) {
        return id != null && storage.containsKey(id);
    }

    @Override
    public boolean deleteById(String id) {
        if (id == null) return false;
        return storage.remove(id) != null;
    }

    @Override
    public void clear() {
        storage.clear();
    }
}
