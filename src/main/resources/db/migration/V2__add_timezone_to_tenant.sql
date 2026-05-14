ALTER TABLE tenants ADD COLUMN timezone VARCHAR(50) DEFAULT 'Asia/Kolkata';

-- Remove the tenant_id column from the tenants table
-- Tenants are root entities and do not belong to any specific tenant.
-- All other tables retain their tenant_id column via BaseEntity.
ALTER TABLE `tenants` DROP COLUMN `tenant_id`;
