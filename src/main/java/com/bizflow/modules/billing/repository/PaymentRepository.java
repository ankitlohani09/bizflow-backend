package com.bizflow.modules.billing.repository;

import com.bizflow.modules.billing.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findAllByInvoiceId(Long invoiceId);

    List<Payment> findAllByTenantIdAndPaidAtBetween(Long tenantId, LocalDateTime from, LocalDateTime to);

    List<Payment> findAllByTenantIdAndInvoiceIdIn(Long tenantId, Collection<Long> invoiceIds);
}
