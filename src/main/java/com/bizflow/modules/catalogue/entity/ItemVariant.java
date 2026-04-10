package com.bizflow.modules.catalogue.entity;

import com.bizflow.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "item_variants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ItemVariant extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(name = "variant_name", nullable = false, length = 150)
    private String variantName;

    @Column(name = "sku", unique = true, length = 100)
    private String sku;

    @Column(name = "barcode", unique = true, length = 100)
    private String barcode;

    @Column(name = "selling_price", precision = 12, scale = 2)
    private BigDecimal sellingPrice;

    @Column(name = "cost_price", precision = 12, scale = 2)
    private BigDecimal costPrice;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;
}