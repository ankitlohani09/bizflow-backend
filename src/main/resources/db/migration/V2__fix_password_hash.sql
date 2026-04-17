-- =============================================
-- V2__bootstrap_admin.sql
-- Only tenant + role (NO USER HERE)
-- =============================================

CREATE DATABASE IF NOT EXISTS bizflow_db;
USE bizflow_db;

SET FOREIGN_KEY_CHECKS = 0;

-- Ensure one default tenant exists
INSERT INTO tenants (name, code, email, is_active)
SELECT 'BizFlow', 'BIZFLOW', 'admin@bizflow.com', TRUE
WHERE NOT EXISTS (SELECT 1 FROM tenants);

-- Ensure ADMIN role exists
INSERT INTO roles (tenant_id, name, description)
SELECT t.id, 'ADMIN', 'System administrator'
FROM tenants t
WHERE t.id = (SELECT MIN(id) FROM tenants)
  AND NOT EXISTS (SELECT 1
                  FROM roles r
                  WHERE r.tenant_id = t.id
                    AND r.name = 'ADMIN');

SET FOREIGN_KEY_CHECKS = 1;