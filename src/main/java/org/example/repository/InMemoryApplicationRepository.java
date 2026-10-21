package org.example.repository;

import org.example.model.Application;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Thread-safe in-memory implementation of ApplicationRepository.
 */
public class InMemoryApplicationRepository implements ApplicationRepository {
    private final Map<String, Application> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Application save(Application application) {
        if (application == null) {
            throw new IllegalArgumentException("Application cannot be null");
        }
        if (application.getId() == null || application.getId().trim().isEmpty()) {
            application.setId("APP-" + idGenerator.getAndIncrement());
        }
        storage.put(application.getId(), application);
        return application;
    }

    @Override
    public Optional<Application> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Application> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public List<Application> findByStudentId(String studentId) {
        if (studentId == null) return Collections.emptyList();
        return storage.values().stream()
                .filter(a -> Objects.equals(a.getStudentId(), studentId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Application> findByDriveId(String driveId) {
        if (driveId == null) return Collections.emptyList();
        return storage.values().stream()
                .filter(a -> Objects.equals(a.getDriveId(), driveId))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Application> findByDriveIdAndStudentId(String driveId, String studentId) {
        if (driveId == null || studentId == null) return Optional.empty();
        return storage.values().stream()
                .filter(a -> Objects.equals(a.getDriveId(), driveId) && Objects.equals(a.getStudentId(), studentId))
                .findFirst();
    }

    @Override
    public boolean existsByDriveIdAndStudentId(String driveId, String studentId) {
        return findByDriveIdAndStudentId(driveId, studentId).isPresent();
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
