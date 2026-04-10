-- TENANTS
CREATE TABLE tenants
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    email         VARCHAR(255) NOT NULL UNIQUE,
    phone         VARCHAR(20),
    address       TEXT,
    business_type VARCHAR(100),
    logo_url      VARCHAR(500),
    is_active     BOOLEAN  DEFAULT TRUE,
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- USERS
CREATE TABLE users
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id  BIGINT                                     NOT NULL,
    name       VARCHAR(255)                               NOT NULL,
    email      VARCHAR(255)                               NOT NULL,
    password   VARCHAR(255)                               NOT NULL,
    phone      VARCHAR(20),
    role       ENUM ('OWNER','MANAGER','CASHIER','STAFF') NOT NULL DEFAULT 'STAFF',
    is_active  BOOLEAN                                             DEFAULT TRUE,
    created_at DATETIME                                            DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME                                            DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    UNIQUE KEY unique_email_per_tenant (email, tenant_id)
);

-- CATEGORIES
CREATE TABLE categories
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id   BIGINT       NOT NULL,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    is_active   BOOLEAN  DEFAULT TRUE,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by  VARCHAR(255),
    updated_by  VARCHAR(255),
    FOREIGN KEY (tenant_id) REFERENCES tenants (id)
);

-- UNITS
CREATE TABLE units
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id  BIGINT      NOT NULL,
    name       VARCHAR(50) NOT NULL,
    symbol     VARCHAR(20),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    FOREIGN KEY (tenant_id) REFERENCES tenants (id)
);

-- ITEMS
CREATE TABLE items
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id       BIGINT         NOT NULL,
    category_id     BIGINT,
    name            VARCHAR(255)   NOT NULL,
    sku             VARCHAR(100),
    barcode         VARCHAR(100),
    description     TEXT,
    unit            VARCHAR(50),
    purchase_price  DECIMAL(12, 2)          DEFAULT 0.00,
    selling_price   DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    tax_rate        DECIMAL(5, 2)           DEFAULT 0.00,
    has_variants    BOOLEAN                 DEFAULT FALSE,
    track_inventory BOOLEAN                 DEFAULT TRUE,
    is_active       BOOLEAN                 DEFAULT TRUE,
    created_at      DATETIME                DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME                DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by      VARCHAR(255),
    updated_by      VARCHAR(255),
    FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    FOREIGN KEY (category_id) REFERENCES categories (id),
    UNIQUE KEY unique_sku_per_tenant (sku, tenant_id)
);

-- ITEM VARIANTS
CREATE TABLE item_variants
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id    BIGINT       NOT NULL,
    item_id      BIGINT       NOT NULL,
    variant_name VARCHAR(255) NOT NULL,
    sku          VARCHAR(100),
    barcode      VARCHAR(100),
    extra_price  DECIMAL(12, 2) DEFAULT 0.00,
    is_active    BOOLEAN        DEFAULT TRUE,
    created_at   DATETIME       DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by   VARCHAR(255),
    updated_by   VARCHAR(255),
    FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    FOREIGN KEY (item_id) REFERENCES items (id)
);

-- WAREHOUSES
CREATE TABLE warehouses
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id  BIGINT       NOT NULL,
    name       VARCHAR(255) NOT NULL,
    location   VARCHAR(500),
    is_active  BOOLEAN  DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    FOREIGN KEY (tenant_id) REFERENCES tenants (id)
);

-- INVENTORY
CREATE TABLE inventory
(
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id           BIGINT NOT NULL,
    item_id             BIGINT NOT NULL,
    variant_id          BIGINT,
    warehouse_id        BIGINT,
    available_qty       DECIMAL(12, 3) DEFAULT 0.000,
    damaged_qty         DECIMAL(12, 3) DEFAULT 0.000,
    expired_qty         DECIMAL(12, 3) DEFAULT 0.000,
    reserved_qty        DECIMAL(12, 3) DEFAULT 0.000,
    low_stock_threshold DECIMAL(12, 3) DEFAULT 10.000,
    created_at          DATETIME       DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    FOREIGN KEY (item_id) REFERENCES items (id),
    FOREIGN KEY (variant_id) REFERENCES item_variants (id),
    FOREIGN KEY (warehouse_id) REFERENCES warehouses (id)
);

-- STOCK MOVEMENTS
CREATE TABLE stock_movements
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id      BIGINT            NOT NULL,
    item_id        BIGINT            NOT NULL,
    variant_id     BIGINT,
    movement_type  VARCHAR(50)       NOT NULL,
    condition_type VARCHAR(50),
    direction      ENUM ('IN','OUT') NOT NULL,
    quantity       DECIMAL(12, 3)    NOT NULL,
    reference_type VARCHAR(100),
    reference_id   BIGINT,
    batch_no       VARCHAR(100),
    expiry_date    DATE,
    notes          TEXT,
    created_at     DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by     VARCHAR(255),
    updated_by     VARCHAR(255),
    FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    FOREIGN KEY (item_id) REFERENCES items (id),
    FOREIGN KEY (variant_id) REFERENCES item_variants (id)
);

-- SUPPLIERS
CREATE TABLE suppliers
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id  BIGINT       NOT NULL,
    name       VARCHAR(255) NOT NULL,
    email      VARCHAR(255),
    phone      VARCHAR(20),
    address    TEXT,
    gstin      VARCHAR(50),
    is_active  BOOLEAN  DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    FOREIGN KEY (tenant_id) REFERENCES tenants (id)
);

-- PURCHASES
CREATE TABLE purchases
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id       BIGINT         NOT NULL,
    supplier_id     BIGINT,
    purchase_number VARCHAR(60)    NOT NULL,
    purchase_date   DATE           NOT NULL,
    subtotal        DECIMAL(14, 2) NOT NULL,
    tax_amount      DECIMAL(14, 2)                    DEFAULT 0.00,
    grand_total     DECIMAL(14, 2) NOT NULL,
    payment_status  ENUM ('PAID','PENDING','PARTIAL') DEFAULT 'PENDING',
    notes           TEXT,
    created_at      DATETIME                          DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME                          DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by      VARCHAR(255),
    updated_by      VARCHAR(255),
    FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    FOREIGN KEY (supplier_id) REFERENCES suppliers (id),
    UNIQUE KEY unique_purchase_per_tenant (purchase_number, tenant_id)
);

-- PURCHASE ITEMS
CREATE TABLE purchase_items
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id   BIGINT         NOT NULL,
    purchase_id BIGINT         NOT NULL,
    item_id     BIGINT         NOT NULL,
    variant_id  BIGINT,
    quantity    DECIMAL(12, 3) NOT NULL,
    unit_cost   DECIMAL(12, 2) NOT NULL,
    tax_rate    DECIMAL(5, 2) DEFAULT 0.00,
    line_total  DECIMAL(14, 2) NOT NULL,
    batch_no    VARCHAR(100),
    expiry_date DATE,
    created_at  DATETIME      DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by  VARCHAR(255),
    updated_by  VARCHAR(255),
    FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    FOREIGN KEY (purchase_id) REFERENCES purchases (id),
    FOREIGN KEY (item_id) REFERENCES items (id),
    FOREIGN KEY (variant_id) REFERENCES item_variants (id)
);

-- PAYMENT MODES
CREATE TABLE payment_modes
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id  BIGINT       NOT NULL,
    name       VARCHAR(100) NOT NULL,
    is_active  BOOLEAN  DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    FOREIGN KEY (tenant_id) REFERENCES tenants (id)
);

-- INVOICES
CREATE TABLE invoices
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id       BIGINT         NOT NULL,
    invoice_number  VARCHAR(60)    NOT NULL,
    invoice_type    VARCHAR(50)    NOT NULL           DEFAULT 'SALE',
    customer_name   VARCHAR(255),
    customer_phone  VARCHAR(20),
    subtotal        DECIMAL(14, 2) NOT NULL,
    discount_amount DECIMAL(14, 2)                    DEFAULT 0.00,
    tax_amount      DECIMAL(14, 2)                    DEFAULT 0.00,
    grand_total     DECIMAL(14, 2) NOT NULL,
    paid_amount     DECIMAL(14, 2)                    DEFAULT 0.00,
    change_amount   DECIMAL(14, 2)                    DEFAULT 0.00,
    payment_status  ENUM ('PAID','PENDING','PARTIAL') DEFAULT 'PAID',
    notes           TEXT,
    created_at      DATETIME                          DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME                          DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by      VARCHAR(255),
    updated_by      VARCHAR(255),
    FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    UNIQUE KEY unique_invoice_per_tenant (invoice_number, tenant_id)
);

-- INVOICE ITEMS
CREATE TABLE invoice_items
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id    BIGINT         NOT NULL,
    invoice_id   BIGINT         NOT NULL,
    item_id      BIGINT         NOT NULL,
    variant_id   BIGINT,
    quantity     DECIMAL(12, 3) NOT NULL,
    unit_price   DECIMAL(12, 2) NOT NULL,
    discount_pct DECIMAL(5, 2) DEFAULT 0.00,
    tax_rate     DECIMAL(5, 2) DEFAULT 0.00,
    line_total   DECIMAL(14, 2) NOT NULL,
    created_at   DATETIME      DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by   VARCHAR(255),
    updated_by   VARCHAR(255),
    FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    FOREIGN KEY (invoice_id) REFERENCES invoices (id),
    FOREIGN KEY (item_id) REFERENCES items (id),
    FOREIGN KEY (variant_id) REFERENCES item_variants (id)
);

-- PAYMENTS
CREATE TABLE payments
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id       BIGINT         NOT NULL,
    invoice_id      BIGINT         NOT NULL,
    payment_mode_id BIGINT         NOT NULL,
    amount          DECIMAL(14, 2) NOT NULL,
    reference_no    VARCHAR(100),
    paid_at         DATETIME,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by      VARCHAR(255),
    updated_by      VARCHAR(255),
    FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    FOREIGN KEY (invoice_id) REFERENCES invoices (id),
    FOREIGN KEY (payment_mode_id) REFERENCES payment_modes (id)
);

-- RETURNS
CREATE TABLE returns
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id     BIGINT         NOT NULL,
    return_number VARCHAR(60)    NOT NULL,
    return_type   VARCHAR(50)    NOT NULL,
    invoice_id    BIGINT,
    purchase_id   BIGINT,
    return_date   DATE           NOT NULL,
    total_amount  DECIMAL(14, 2) NOT NULL,
    reason        TEXT,
    status        VARCHAR(50) DEFAULT 'PENDING',
    notes         TEXT,
    created_at    DATETIME    DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by    VARCHAR(255),
    updated_by    VARCHAR(255),
    FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    FOREIGN KEY (invoice_id) REFERENCES invoices (id),
    FOREIGN KEY (purchase_id) REFERENCES purchases (id)
);

-- RETURN ITEMS
CREATE TABLE return_items
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id  BIGINT         NOT NULL,
    return_id  BIGINT         NOT NULL,
    item_id    BIGINT         NOT NULL,
    variant_id BIGINT,
    quantity   DECIMAL(12, 3) NOT NULL,
    unit_price DECIMAL(12, 2) NOT NULL,
    line_total DECIMAL(14, 2) NOT NULL,
    reason     TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    FOREIGN KEY (return_id) REFERENCES returns (id),
    FOREIGN KEY (item_id) REFERENCES items (id),
    FOREIGN KEY (variant_id) REFERENCES item_variants (id)
);

-- EXPENSE CATEGORIES
CREATE TABLE expense_categories
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id   BIGINT       NOT NULL,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    is_active   BOOLEAN  DEFAULT TRUE,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by  VARCHAR(255),
    updated_by  VARCHAR(255),
    FOREIGN KEY (tenant_id) REFERENCES tenants (id)
);

-- EXPENSES
CREATE TABLE expenses
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id    BIGINT         NOT NULL,
    category_id  BIGINT,
    title        VARCHAR(255)   NOT NULL,
    amount       DECIMAL(14, 2) NOT NULL,
    expense_date DATE           NOT NULL,
    payment_mode VARCHAR(100),
    reference_no VARCHAR(100),
    notes        TEXT,
    created_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by   VARCHAR(255),
    updated_by   VARCHAR(255),
    FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    FOREIGN KEY (category_id) REFERENCES expense_categories (id)
);

-- STAFF
CREATE TABLE staff
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id   BIGINT       NOT NULL,
    name        VARCHAR(255) NOT NULL,
    phone       VARCHAR(20),
    email       VARCHAR(255),
    designation VARCHAR(100),
    salary      DECIMAL(12, 2) DEFAULT 0.00,
    join_date   DATE,
    is_active   BOOLEAN        DEFAULT TRUE,
    created_at  DATETIME       DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by  VARCHAR(255),
    updated_by  VARCHAR(255),
    FOREIGN KEY (tenant_id) REFERENCES tenants (id)
);

-- ATTENDANCE
CREATE TABLE attendance
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id  BIGINT                                       NOT NULL,
    staff_id   BIGINT                                       NOT NULL,
    date       DATE                                         NOT NULL,
    status     ENUM ('PRESENT','ABSENT','HALF_DAY','LEAVE') NOT NULL,
    check_in   TIME,
    check_out  TIME,
    notes      TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    FOREIGN KEY (staff_id) REFERENCES staff (id),
    UNIQUE KEY unique_attendance (staff_id, date)
);

-- STAFF ADVANCES
CREATE TABLE staff_advances
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id    BIGINT         NOT NULL,
    staff_id     BIGINT         NOT NULL,
    amount       DECIMAL(12, 2) NOT NULL,
    advance_date DATE           NOT NULL,
    reason       TEXT,
    is_recovered BOOLEAN  DEFAULT FALSE,
    created_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by   VARCHAR(255),
    updated_by   VARCHAR(255),
    FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    FOREIGN KEY (staff_id) REFERENCES staff (id)
);

-- AI QUERIES LOG
CREATE TABLE ai_queries
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id  BIGINT NOT NULL,
    user_id    BIGINT,
    query      TEXT   NOT NULL,
    answer     TEXT,
    query_type VARCHAR(50),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    FOREIGN KEY (user_id) REFERENCES users (id)
);
