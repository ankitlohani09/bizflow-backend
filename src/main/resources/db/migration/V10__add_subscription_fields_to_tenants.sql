-- ============================================
-- V10__add_subscription_fields_to_tenants.sql
-- ============================================

ALTER TABLE tenants
ADD COLUMN subscription_plan VARCHAR(50) DEFAULT 'TRIAL',
ADD COLUMN expiry_date DATETIME NULL,
ADD COLUMN max_users INT DEFAULT 5;
