package com.bizflow.modules.returns.entity;

import com.bizflow.common.BaseEntity;
import com.bizflow.modules.billing.entity.PaymentMode;
import com.bizflow.modules.billing.entity.Invoice;
import com.bizflow.common.enums.ReturnStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "returns")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Return extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    @Column(name = "return_number", unique = true, nullable = false, length = 60)
    private String returnNumber;

    @Column(name = "customer_name", length = 200)
    private String customerName;

    @Column(name = "customer_phone", length = 20)
    private String customerPhone;

    @Column(name = "total_refund", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalRefund;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReturnStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_mode_id")
    private PaymentMode paymentMode;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

}