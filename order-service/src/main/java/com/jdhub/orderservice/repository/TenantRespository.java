package com.jdhub.orderservice.repository;

import com.jdhub.orderservice.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TenantRespository extends JpaRepository<Tenant, UUID> {
}
