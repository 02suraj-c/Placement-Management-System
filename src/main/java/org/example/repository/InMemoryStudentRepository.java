package org.example.repository;

import org.example.model.Student;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe in-memory implementation of StudentRepository.
 */
public class InMemoryStudentRepository implements StudentRepository {
    private final Map<String, Student> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Student save(Student student) {
        if (student == null) {
            throw new IllegalArgumentException("Student cannot be null");
        }
        if (student.getId() == null || student.getId().trim().isEmpty()) {
            student.setId("STU-" + idGenerator.getAndIncrement());
        }
        storage.put(student.getId(), student);
        return student;
    }

    @Override
    public Optional<Student> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Student> findAll() {
        return new ArrayList<>(storage.values());
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
