package com.bizflow.modules.billing.entity;

import com.bizflow.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "payment_modes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PaymentMode extends BaseEntity {

    @Column(name = "name", nullable = false, length = 60)
    private String name;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;
}
