package org.example.model;

import org.example.util.ValidationUtil;

import java.util.Objects;

/**
 * Represents a recruiting company participating in campus placements.
 */
public class Company {
    private String id;
    private String name;
    private String description;
    private String location;
    private String website;

    public Company() {
    }

    public Company(String id, String name, String description, String location, String website) {
        this.id = ValidationUtil.requireNonBlank(id, "Company ID");
        this.name = ValidationUtil.requireNonBlank(name, "Company name");
        this.description = description != null ? description.trim() : "";
        this.location = location != null ? location.trim() : "";
        this.website = ValidationUtil.validateWebsite(website);
    }

    public Company(String name, String description, String location, String website) {
        this.name = ValidationUtil.requireNonBlank(name, "Company name");
        this.description = description != null ? description.trim() : "";
        this.location = location != null ? location.trim() : "";
        this.website = ValidationUtil.validateWebsite(website);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = ValidationUtil.requireNonBlank(id, "Company ID");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = ValidationUtil.requireNonBlank(name, "Company name");
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description != null ? description.trim() : "";
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location != null ? location.trim() : "";
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = ValidationUtil.validateWebsite(website);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Company company = (Company) o;
        return Objects.equals(id, company.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Company{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", location='" + location + '\'' +
                ", website='" + website + '\'' +
                '}';
    }
}
