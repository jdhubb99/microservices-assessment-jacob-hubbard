package com.jdhub.orderservice.entity;

import com.jdhub.orderservice.entity.enums.TenantStatus;
import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tenants")
@Getter
public class Tenant {
    @Id
    private UUID id;
    private String name;
    @Enumerated(EnumType.STRING)
    private TenantStatus status;
    @CreatedDate
    private Instant createdAt;

    public boolean isActive() {
        return status == TenantStatus.ACTIVE;
    }
}
