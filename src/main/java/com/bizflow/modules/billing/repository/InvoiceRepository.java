package com.bizflow.modules.billing.repository;

import com.bizflow.modules.billing.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findAllByTenantIdOrderByCreatedAtDesc(Long tenantId);

    Optional<Invoice> findByIdAndTenantId(Long id, Long tenantId);

    Optional<Invoice> findByInvoiceNumberAndTenantId(String invoiceNumber, Long tenantId);

    long countByTenantId(Long tenantId);
}