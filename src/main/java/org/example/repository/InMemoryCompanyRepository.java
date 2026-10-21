package org.example.repository;

import org.example.model.Company;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Thread-safe in-memory implementation of CompanyRepository.
 */
public class InMemoryCompanyRepository implements CompanyRepository {
    private final Map<String, Company> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Company save(Company company) {
        if (company == null) {
            throw new IllegalArgumentException("Company cannot be null");
        }
        if (company.getId() == null || company.getId().trim().isEmpty()) {
            company.setId("COMP-" + idGenerator.getAndIncrement());
        }
        storage.put(company.getId(), company);
        return company;
    }

    @Override
    public Optional<Company> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Company> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public List<Company> findByNameContainingIgnoreCase(String name) {
        if (name == null || name.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String lowerQuery = name.trim().toLowerCase();
        return storage.values().stream()
                .filter(c -> c.getName() != null && c.getName().toLowerCase().contains(lowerQuery))
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
