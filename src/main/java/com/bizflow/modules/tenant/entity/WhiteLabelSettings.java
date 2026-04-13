package com.bizflow.modules.tenant.entity;

import com.bizflow.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "white_label_settings")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class WhiteLabelSettings extends BaseEntity {

    @Column(name = "brand_name", length = 150)
    private String brandName;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "primary_color", length = 30)
    private String primaryColor;

    @Column(name = "secondary_color", length = 30)
    private String secondaryColor;

    @Column(name = "domain_name", length = 255)
    private String domainName;

    @Column(name = "support_email", length = 200)
    private String supportEmail;
}
