package com.bizflow.modules.billing.repository;

import com.bizflow.modules.billing.entity.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.time.LocalDateTime;
import java.util.List;

public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, Long> {
    @org.springframework.data.jpa.repository.Query("SELECT ii FROM InvoiceItem ii JOIN FETCH ii.item LEFT JOIN FETCH ii.variant WHERE ii.invoice.id = :invoiceId")
    List<InvoiceItem> findAllByInvoiceId(Long invoiceId);

    List<InvoiceItem> findAllByTenantIdAndInvoiceIdIn(Long tenantId, Collection<Long> invoiceIds);

    List<InvoiceItem> findAllByTenantIdAndInvoiceCreatedAtBetween(Long tenantId, LocalDateTime from, LocalDateTime to);
}
