package com.bizflow.modules.staff.entity;

import com.bizflow.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "staff_biometrics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class StaffBiometric extends BaseEntity {

    @OneToOne
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff staff;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String credentialId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String publicKey;

    @Builder.Default
    private Long signCount = 0L;
}
