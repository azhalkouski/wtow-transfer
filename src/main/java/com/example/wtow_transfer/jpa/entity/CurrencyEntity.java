package com.example.wtow_transfer.jpa.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.springframework.data.annotation.Immutable;

@Entity
@Immutable
@Table(name = "currencies", schema = "core")
public class CurrencyEntity {
    @Id
    private Long id;
    private String code;
    private String description;

    protected CurrencyEntity() {}

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
