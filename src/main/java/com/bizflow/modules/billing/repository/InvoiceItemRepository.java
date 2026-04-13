package com.bizflow.modules.billing.repository;

import com.bizflow.modules.billing.entity.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.time.LocalDateTime;
import java.util.List;

public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, Long> {
    List<InvoiceItem> findAllByInvoiceId(Long invoiceId);

    List<InvoiceItem> findAllByTenantIdAndInvoiceIdIn(Long tenantId, Collection<Long> invoiceIds);

    List<InvoiceItem> findAllByTenantIdAndInvoiceCreatedAtBetween(Long tenantId, LocalDateTime from, LocalDateTime to);
}
