package com.bizflow.modules.tenant.entity;

import com.bizflow.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tenants")
@Data
@Builder
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
    @Column(nullable = false)
    private Boolean isActive = true;
}