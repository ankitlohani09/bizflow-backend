-- ============================================
-- V1__init_schema.sql
-- BizFlow Complete Database Schema
-- ============================================

-- TENANTS
CREATE TABLE tenants
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(150) NOT NULL,
    slug          VARCHAR(100) NOT NULL UNIQUE,
    logo_url      VARCHAR(500),
    primary_color VARCHAR(10),
    domain        VARCHAR(200) UNIQUE,
    plan          VARCHAR(50)  NOT NULL DEFAULT 'basic',
    is_active     BOOLEAN               DEFAULT TRUE,
    created_at    DATETIME              DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ROLES (DB-Driven)
CREATE TABLE roles
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id   BIGINT       NOT NULL,
    name        VARCHAR(80)  NOT NULL,
    description VARCHAR(300),
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by  VARCHAR(100) DEFAULT NULL,
    updated_by  VARCHAR(100) DEFAULT NULL,
    FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    UNIQUE KEY unique_role_per_tenant (name, tenant_id)
);

-- USERS
CREATE TABLE users
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id  BIGINT       NOT NULL,
    name       VARCHAR(150) NOT NULL,
    email      VARCHAR(200) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    phone      VARCHAR(20),
    is_active  BOOLEAN      DEFAULT TRUE,
    created_at DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100) DEFAULT NULL,
    updated_by VARCHAR(100) DEFAULT NULL,
    FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    UNIQUE KEY unique_email_per_tenant (email, tenant_id)
);

-- USER_ROLES (Many-to-Many)
CREATE TABLE user_roles
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT   NOT NULL,
    role_id     BIGINT   NOT NULL,
    assigned_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (role_id) REFERENCES roles (id),
    UNIQUE KEY unique_user_role (user_id, role_id)
);

-- CATEGORIES
CREATE TABLE categories
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id   BIGINT       NOT NULL,
    name        VARCHAR(150) NOT NULL,
    parent_id   BIGINT,
    description TEXT,
    is_active   BOOLEAN      DEFAULT TRUE,
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by  VARCHAR(100) DEFAULT NULL,
    updated_by  VARCHAR(100) DEFAULT NULL,
    FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    FOREIGN KEY (parent_id) REFERENCES categories (id)
);

-- UNITS
CREATE TABLE units
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id  BIGINT       NOT NULL,
    name       VARCHAR(60)  NOT NULL,
    symbol     VARCHAR(15)  NOT NULL,
    created_at DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100) DEFAULT NULL,
    updated_by VARCHAR(100) DEFAULT NULL,
    FOREIGN KEY (tenant_id) REFERENCES tenants (id)
);

-- ITEMS
CREATE TABLE items
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id       BIGINT                      NOT NULL,
    category_id     BIGINT,
    unit_id         BIGINT,
    type            ENUM ('PRODUCT', 'SERVICE') NOT NULL DEFAULT 'PRODUCT',
    name            VARCHAR(200)                NOT NULL,
    barcode         VARCHAR(100) UNIQUE,
    description     TEXT,
    selling_price   DECIMAL(12, 2)              NOT NULL DEFAULT 0.00,
    cost_price      DECIMAL(12, 2)                       DEFAULT 0.00,
    tax_rate        DECIMAL(5, 2)                        DEFAULT 0.00,
    has_variants    BOOLEAN                              DEFAULT FALSE,
    track_inventory BOOLEAN                              DEFAULT TRUE,
    is_active       BOOLEAN                              DEFAULT TRUE,
    created_at      DATETIME                             DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME                             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by      VARCHAR(100)                         DEFAULT NULL,
    updated_by      VARCHAR(100)                         DEFAULT NULL,
    FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    FOREIGN KEY (category_id) REFERENCES categories (id),
    FOREIGN KEY (unit_id) REFERENCES units (id)
);

-- ITEM VARIANTS
CREATE TABLE item_variants
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id     BIGINT       NOT NULL,
    item_id       BIGINT       NOT NULL,
    variant_name  VARCHAR(150) NOT NULL,
    sku           VARCHAR(100) UNIQUE,
    barcode       VARCHAR(100) UNIQUE,
    selling_price DECIMAL(12, 2),
    cost_price    DECIMAL(12, 2),
    is_active     BOOLEAN      DEFAULT TRUE,
    created_at    DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by    VARCHAR(100) DEFAULT NULL,
    updated_by    VARCHAR(100) DEFAULT NULL,
    FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    FOREIGN KEY (item_id) REFERENCES items (id)
);

-- INVENTORY
CREATE TABLE inventory
(
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id           BIGINT         NOT NULL,
    item_id             BIGINT         NOT NULL,
    variant_id          BIGINT,
    available_qty       DECIMAL(12, 3) NOT NULL DEFAULT 0.000,
    damaged_qty         DECIMAL(12, 3) NOT NULL DEFAULT 0.000,
    expired_qty         DECIMAL(12, 3) NOT NULL DEFAULT 0.000,
    reserved_qty        DECIMAL(12, 3) NOT NULL DEFAULT 0.000,
    low_stock_threshold DECIMAL(12, 3)          DEFAULT 10.000,
    updated_at          DATETIME                DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    FOREIGN KEY (item_id) REFERENCES items (id),
    FOREIGN KEY (variant_id) REFERENCES item_variants (id),
    UNIQUE KEY unique_inventory (tenant_id, item_id, variant_id)
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
    reference_type VARCHAR(60),
    reference_id   BIGINT,
    batch_no       VARCHAR(100),
    expiry_date    DATE,
    notes          TEXT,
    created_by     BIGINT,
    created_at     DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    FOREIGN KEY (item_id) REFERENCES items (id),
    FOREIGN KEY (variant_id) REFERENCES item_variants (id)
);

-- SUPPLIERS
CREATE TABLE suppliers
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id    BIGINT       NOT NULL,
    name         VARCHAR(200) NOT NULL,
    contact_name VARCHAR(150),
    phone        VARCHAR(20),
    email        VARCHAR(200),
    address      TEXT,
    gstin        VARCHAR(20),
    is_active    BOOLEAN      DEFAULT TRUE,
    created_at   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by   VARCHAR(100) DEFAULT NULL,
    updated_by   VARCHAR(100) DEFAULT NULL,
    FOREIGN KEY (tenant_id) REFERENCES tenants (id)
);

-- PAYMENT MODES (DB-Driven)
CREATE TABLE payment_modes
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id  BIGINT       NOT NULL,
    name       VARCHAR(100) NOT NULL,
    is_active  BOOLEAN      DEFAULT TRUE,
    created_at DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100) DEFAULT NULL,
    updated_by VARCHAR(100) DEFAULT NULL,
    FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    UNIQUE KEY unique_mode_per_tenant (name, tenant_id)
);

-- INVOICES
CREATE TABLE invoices
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id       BIGINT                                       NOT NULL,
    invoice_number  VARCHAR(60)                                  NOT NULL,
    invoice_type    ENUM ('SALE','ESTIMATE','CREDIT_NOTE')       NOT NULL DEFAULT 'SALE',
    customer_name   VARCHAR(200),
    customer_phone  VARCHAR(20),
    subtotal        DECIMAL(14, 2)                               NOT NULL,
    discount_amount DECIMAL(14, 2)                                        DEFAULT 0.00,
    tax_amount      DECIMAL(14, 2)                                        DEFAULT 0.00,
    grand_total     DECIMAL(14, 2)                               NOT NULL,
    paid_amount     DECIMAL(14, 2)                                        DEFAULT 0.00,
    change_amount   DECIMAL(14, 2)                                        DEFAULT 0.00,
    payment_status  ENUM ('PAID','PARTIAL','UNPAID','CANCELLED') NOT NULL DEFAULT 'UNPAID',
    notes           TEXT,
    created_at      DATETIME                                              DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME                                              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by      VARCHAR(100)                                          DEFAULT NULL,
    updated_by      VARCHAR(100)                                          DEFAULT NULL,
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
    reference_no    VARCHAR(150),
    paid_at         DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at      DATETIME                DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    FOREIGN KEY (invoice_id) REFERENCES invoices (id),
    FOREIGN KEY (payment_mode_id) REFERENCES payment_modes (id)
);

-- PURCHASES
CREATE TABLE purchases
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id       BIGINT                           NOT NULL,
    supplier_id     BIGINT,
    purchase_number VARCHAR(60)                      NOT NULL,
    purchase_date   DATE                             NOT NULL,
    subtotal        DECIMAL(14, 2)                   NOT NULL,
    tax_amount      DECIMAL(14, 2)                            DEFAULT 0.00,
    grand_total     DECIMAL(14, 2)                   NOT NULL,
    payment_status  ENUM ('PAID','PARTIAL','UNPAID') NOT NULL DEFAULT 'UNPAID',
    notes           TEXT,
    created_at      DATETIME                                  DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME                                  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by      VARCHAR(100)                              DEFAULT NULL,
    updated_by      VARCHAR(100)                              DEFAULT NULL,
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
    cost_price  DECIMAL(12, 2) NOT NULL,
    tax_rate    DECIMAL(5, 2) DEFAULT 0.00,
    line_total  DECIMAL(14, 2) NOT NULL,
    batch_no    VARCHAR(100),
    expiry_date DATE,
    FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    FOREIGN KEY (purchase_id) REFERENCES purchases (id),
    FOREIGN KEY (item_id) REFERENCES items (id),
    FOREIGN KEY (variant_id) REFERENCES item_variants (id)
);

-- RETURNS
CREATE TABLE returns
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id     BIGINT                            NOT NULL,
    invoice_id    BIGINT                            NOT NULL,
    return_number VARCHAR(60)                       NOT NULL,
    return_date   DATE                              NOT NULL,
    refund_amount DECIMAL(14, 2)                    NOT NULL,
    refund_mode   ENUM ('CASH','UPI','CREDIT_NOTE') NOT NULL,
    notes         TEXT,
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_by    BIGINT,
    FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    FOREIGN KEY (invoice_id) REFERENCES invoices (id),
    UNIQUE KEY unique_return_per_tenant (return_number, tenant_id)
);

-- RETURN ITEMS
CREATE TABLE return_items
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id      BIGINT                            NOT NULL,
    return_id      BIGINT                            NOT NULL,
    item_id        BIGINT                            NOT NULL,
    variant_id     BIGINT,
    quantity       DECIMAL(12, 3)                    NOT NULL,
    condition_type ENUM ('GOOD','DAMAGED','EXPIRED') NOT NULL,
    unit_price     DECIMAL(12, 2)                    NOT NULL,
    line_refund    DECIMAL(14, 2)                    NOT NULL,
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
    name        VARCHAR(150) NOT NULL,
    description TEXT,
    is_active   BOOLEAN      DEFAULT TRUE,
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by  VARCHAR(100) DEFAULT NULL,
    updated_by  VARCHAR(100) DEFAULT NULL,
    FOREIGN KEY (tenant_id) REFERENCES tenants (id)
);

-- EXPENSES
CREATE TABLE expenses
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id    BIGINT         NOT NULL,
    category_id  BIGINT,
    title        VARCHAR(200)   NOT NULL,
    amount       DECIMAL(14, 2) NOT NULL,
    expense_date DATE           NOT NULL,
    payment_mode VARCHAR(100),
    reference_no VARCHAR(100),
    notes        TEXT,
    created_at   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    created_by   VARCHAR(100) DEFAULT NULL,
    FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    FOREIGN KEY (category_id) REFERENCES expense_categories (id)
);

-- STAFF
CREATE TABLE staff
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id  BIGINT         NOT NULL,
    user_id    BIGINT,
    name       VARCHAR(150)   NOT NULL,
    role       VARCHAR(100),
    phone      VARCHAR(20),
    email      VARCHAR(200),
    salary     DECIMAL(12, 2) DEFAULT 0.00,
    join_date  DATE,
    is_active  BOOLEAN        DEFAULT TRUE,
    created_at DATETIME       DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    FOREIGN KEY (user_id) REFERENCES users (id)
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
    reason       VARCHAR(300),
    is_recovered BOOLEAN  DEFAULT FALSE,
    created_by   BIGINT,
    created_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    FOREIGN KEY (staff_id) REFERENCES staff (id)
);

-- ACTIVITY LOGS
CREATE TABLE activity_logs
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id   BIGINT       NOT NULL,
    user_id     BIGINT,
    action      VARCHAR(100) NOT NULL,
    entity_type VARCHAR(80),
    entity_id   BIGINT,
    description TEXT,
    ip_address  VARCHAR(45),
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    FOREIGN KEY (user_id) REFERENCES users (id)
);

-- AI LOGS
CREATE TABLE ai_logs
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id      BIGINT       NOT NULL,
    user_id        BIGINT,
    query_type     VARCHAR(100) NOT NULL,
    input_payload  JSON,
    output_payload JSON,
    latency_ms     INT,
    created_at     DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    FOREIGN KEY (user_id) REFERENCES users (id)
);

-- ============================================
-- DEFAULT SEED DATA
-- ============================================

-- Default Tenant
INSERT INTO tenants (name, slug, plan, is_active)
VALUES ('BizFlow Demo', 'bizflow-demo', 'enterprise', TRUE);

-- Default Roles (for tenant_id = 1)
INSERT INTO roles (tenant_id, name, description)
VALUES (1, 'OWNER', 'Full access to everything'),
       (1, 'MANAGER', 'Manage day-to-day operations'),
       (1, 'CASHIER', 'Billing and payments only'),
       (1, 'STAFF', 'Basic access');

-- Default User (admin@bizflow.com / password123)
INSERT INTO users (tenant_id, name, email, password, is_active)
VALUES (1, 'BizFlow Admin', 'admin@bizflow.com',
        '$2b$10$zw0OroU8gwU9A0hw239M8uagRVYxvqwEdnb29UsG/3xMnc4d29rHK', TRUE);

-- Assign OWNER role to admin
INSERT INTO user_roles (user_id, role_id)
VALUES (1, 1);

-- Default Payment Modes (for tenant_id = 1)
INSERT INTO payment_modes (tenant_id, name, is_active)
VALUES (1, 'CASH', TRUE),
       (1, 'UPI', TRUE),
       (1, 'CARD', TRUE),
       (1, 'CREDIT', TRUE);