-- ============================================
-- V3__tax_rules_and_inventory_enhancement.sql
-- Aligning BizFlow with EvisionPosBE features
-- ============================================

-- 1. Create Tax Rules Table
CREATE TABLE tax_rules
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id  BIGINT       NOT NULL,
    name       VARCHAR(100) NOT NULL,
    rate       DECIMAL(5, 2) NOT NULL DEFAULT 0.00,
    tax_type   VARCHAR(50)  NOT NULL DEFAULT 'GST',
    description VARCHAR(255) NULL,
    is_active  BOOLEAN      DEFAULT TRUE,
    created_at DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100) DEFAULT NULL,
    updated_by VARCHAR(100) DEFAULT NULL,
    FOREIGN KEY (tenant_id) REFERENCES tenants (id)
);

-- 2. Enhance Inventory Table
ALTER TABLE inventory
    ADD COLUMN batch_no VARCHAR(50) NULL AFTER variant_id,
    ADD COLUMN expiry_date DATETIME NULL AFTER batch_no,
    ADD COLUMN mrp DECIMAL(14, 2) DEFAULT 0.00 AFTER low_stock_threshold,
    ADD COLUMN location VARCHAR(100) NULL AFTER mrp;

-- 3. Link Items to Tax Rules
ALTER TABLE items
    ADD COLUMN tax_rule_id BIGINT NULL AFTER tax_rate,
    ADD CONSTRAINT fk_items_tax_rule FOREIGN KEY (tax_rule_id) REFERENCES tax_rules (id);

-- 4. Audit Tax Rule in Invoice Items
ALTER TABLE invoice_items
    ADD COLUMN tax_rule_id BIGINT NULL AFTER tax_rate,
    ADD CONSTRAINT fk_invoice_items_tax_rule FOREIGN KEY (tax_rule_id) REFERENCES tax_rules (id);

-- 5. Category Enhancements
ALTER TABLE categories
    ADD COLUMN is_active BOOLEAN DEFAULT TRUE AFTER parent_id;

-- 5. Add default tax rules for common slabs (Optional, can be done via DataInitializer)
-- INSERT INTO tax_rules (tenant_id, name, rate, tax_type) VALUES (1, 'Exempt', 0.00, 'GST');
-- INSERT INTO tax_rules (tenant_id, name, rate, tax_type) VALUES (1, 'GST 5%', 5.00, 'GST');
-- INSERT INTO tax_rules (tenant_id, name, rate, tax_type) VALUES (1, 'GST 12%', 12.00, 'GST');
-- INSERT INTO tax_rules (tenant_id, name, rate, tax_type) VALUES (1, 'GST 18%', 18.00, 'GST');
-- INSERT INTO tax_rules (tenant_id, name, rate, tax_type) VALUES (1, 'GST 28%', 28.00, 'GST');
