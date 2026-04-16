-- =============================================
-- V2__fix_password_hash.sql
-- Bootstrap only ADMIN user and role
-- Target DB: bizflow_db
-- =============================================

CREATE DATABASE IF NOT EXISTS bizflow_db;
USE bizflow_db;

SET FOREIGN_KEY_CHECKS = 0;

-- Ensure one default tenant exists (required by FK constraints)
INSERT INTO tenants (name, code, email, is_active)
SELECT 'BizFlow', 'BIZFLOW', 'admin@bizflow.com', TRUE
WHERE NOT EXISTS (SELECT 1 FROM tenants);

-- Ensure ADMIN role exists for the first tenant
INSERT INTO roles (tenant_id, name, description)
SELECT t.id, 'ADMIN', 'System administrator'
FROM tenants t
WHERE t.id = (SELECT MIN(id) FROM tenants)
  AND NOT EXISTS (
      SELECT 1
      FROM roles r
      WHERE r.tenant_id = t.id
        AND r.name = 'ADMIN'
  );

-- Ensure only admin demo user exists
INSERT INTO users (tenant_id, name, email, password, is_active)
SELECT t.id,
       'Admin User',
       'admin@bizflow.com',
       '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.TVuHOn2',
       TRUE
FROM tenants t
WHERE t.id = (SELECT MIN(id) FROM tenants)
  AND NOT EXISTS (
      SELECT 1
      FROM users u
      WHERE u.email = 'admin@bizflow.com'
        AND u.tenant_id = t.id
  );

-- Ensure admin user is linked to ADMIN role
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.tenant_id = u.tenant_id AND r.name = 'ADMIN'
WHERE u.email = 'admin@bizflow.com'
  AND NOT EXISTS (
      SELECT 1
      FROM user_roles ur
      WHERE ur.user_id = u.id
        AND ur.role_id = r.id
  );

SET FOREIGN_KEY_CHECKS = 1;
