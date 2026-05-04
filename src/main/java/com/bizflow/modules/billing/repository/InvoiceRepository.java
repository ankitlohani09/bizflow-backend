package com.bizflow.modules.billing.repository;

import com.bizflow.modules.billing.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    @org.springframework.data.jpa.repository.Query("SELECT i FROM Invoice i LEFT JOIN FETCH i.customer WHERE i.tenantId = :tenantId ORDER BY i.createdAt DESC")
    List<Invoice> findAllByTenantIdOrderByCreatedAtDesc(Long tenantId);

    @org.springframework.data.jpa.repository.Query("SELECT i FROM Invoice i LEFT JOIN FETCH i.customer WHERE i.id = :id AND i.tenantId = :tenantId")
    Optional<Invoice> findByIdAndTenantId(Long id, Long tenantId);

    Optional<Invoice> findByInvoiceNumberAndTenantId(String invoiceNumber, Long tenantId);

    long countByTenantId(Long tenantId);

    List<Invoice> findAllByTenantIdAndCreatedAtBetweenOrderByCreatedAtAsc(Long tenantId, LocalDateTime from,
            LocalDateTime to);
}
