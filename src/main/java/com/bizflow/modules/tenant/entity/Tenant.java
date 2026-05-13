package com.bizflow.modules.tenant.entity;

import com.bizflow.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "tenants")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Tenant extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String email;

    private String phone;
    private String address;
    private String businessType;

    @Builder.Default
    private String subscriptionPlan = "TRIAL";

    private java.time.LocalDateTime expiryDate;

    @Builder.Default
    private Integer maxUsers = 5;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isActive = true;

    @Builder.Default
    private Boolean isGpsMandatory = false;

    @Builder.Default
    private Boolean isSelfieMandatory = false;

    @Builder.Default
    private Boolean isKitchenEnabled = false;

    @Builder.Default
    private String timezone = "Asia/Kolkata";
}