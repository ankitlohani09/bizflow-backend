CREATE TABLE white_label_settings
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id       BIGINT       NOT NULL,
    brand_name      VARCHAR(150) NULL,
    logo_url        VARCHAR(500) NULL,
    primary_color   VARCHAR(30)  NULL,
    secondary_color VARCHAR(30)  NULL,
    domain_name     VARCHAR(255) NULL,
    support_email   VARCHAR(200) NULL,
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by      VARCHAR(100) DEFAULT NULL,
    updated_by      VARCHAR(100) DEFAULT NULL,
    UNIQUE KEY unique_white_label_per_tenant (tenant_id),
    FOREIGN KEY (tenant_id) REFERENCES tenants (id)
);

CREATE TABLE kitchen_orders
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id    BIGINT                                       NOT NULL,
    order_number VARCHAR(60)                                  NOT NULL,
    table_no     VARCHAR(50)                                  NULL,
    customer_name VARCHAR(200)                                NULL,
    status       ENUM ('PLACED','IN_PREP','READY','SERVED','CANCELLED') NOT NULL DEFAULT 'PLACED',
    total_amount DECIMAL(14, 2)                               NOT NULL DEFAULT 0.00,
    notes        TEXT                                         NULL,
    created_at   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by   VARCHAR(100) DEFAULT NULL,
    updated_by   VARCHAR(100) DEFAULT NULL,
    UNIQUE KEY unique_kitchen_order_per_tenant (tenant_id, order_number),
    FOREIGN KEY (tenant_id) REFERENCES tenants (id)
);

CREATE TABLE kitchen_order_items
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id        BIGINT         NOT NULL,
    kitchen_order_id BIGINT         NOT NULL,
    item_id          BIGINT         NOT NULL,
    variant_id       BIGINT         NULL,
    quantity         DECIMAL(12, 3) NOT NULL,
    unit_price       DECIMAL(12, 2) NOT NULL,
    line_total       DECIMAL(14, 2) NOT NULL,
    notes            VARCHAR(500)   NULL,
    created_at       DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by       VARCHAR(100) DEFAULT NULL,
    updated_by       VARCHAR(100) DEFAULT NULL,
    FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    FOREIGN KEY (kitchen_order_id) REFERENCES kitchen_orders (id),
    FOREIGN KEY (item_id) REFERENCES items (id),
    FOREIGN KEY (variant_id) REFERENCES item_variants (id)
);
