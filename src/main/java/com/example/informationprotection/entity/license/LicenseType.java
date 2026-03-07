package com.example.informationprotection.entity.license;

import jakarta.persistence.*;

@Entity
@Table(name = "license_type")
public class LicenseType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String name;

    @Column(name = "default_duration_in_days", nullable = false)
    private Integer defaultDurationInDays;

    @Column(length = 1024)
    private String description;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getDefaultDurationInDays() {
        return defaultDurationInDays;
    }

    public void setDefaultDurationInDays(Integer defaultDurationInDays) {
        this.defaultDurationInDays = defaultDurationInDays;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
