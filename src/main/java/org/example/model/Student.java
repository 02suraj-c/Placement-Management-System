package org.example.model;

import org.example.util.ValidationUtil;

import java.util.Objects;

/**
 * Shared student model representing student academic and profile details.
 */
public class Student {
    private String id;
    private String name;
    private String email;
    private String branch;
    private double cgpa;
    private int backlogCount;

    public Student() {
    }

    public Student(String id, String name, String email, String branch, double cgpa, int backlogCount) {
        this.id = ValidationUtil.requireNonBlank(id, "Student ID");
        this.name = ValidationUtil.requireNonBlank(name, "Student name");
        this.email = ValidationUtil.requireNonBlank(email, "Student email");
        this.branch = ValidationUtil.requireNonBlank(branch, "Student branch");
        this.cgpa = ValidationUtil.validateCGPA(cgpa, "Student CGPA");
        this.backlogCount = ValidationUtil.validateNonNegative(backlogCount, "Student backlog count");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = ValidationUtil.requireNonBlank(id, "Student ID");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = ValidationUtil.requireNonBlank(name, "Student name");
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = ValidationUtil.requireNonBlank(email, "Student email");
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = ValidationUtil.requireNonBlank(branch, "Student branch");
    }

    public double getCgpa() {
        return cgpa;
    }

    public void setCgpa(double cgpa) {
        this.cgpa = ValidationUtil.validateCGPA(cgpa, "Student CGPA");
    }

    public int getBacklogCount() {
        return backlogCount;
    }

    public void setBacklogCount(int backlogCount) {
        this.backlogCount = ValidationUtil.validateNonNegative(backlogCount, "Student backlog count");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return Objects.equals(id, student.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Student{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", branch='" + branch + '\'' +
                ", cgpa=" + cgpa +
                ", backlogCount=" + backlogCount +
                '}';
    }
}
