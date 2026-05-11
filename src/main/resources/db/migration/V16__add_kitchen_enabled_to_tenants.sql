-- Add is_kitchen_enabled column to tenants table
ALTER TABLE tenants ADD COLUMN is_kitchen_enabled BOOLEAN NOT NULL DEFAULT FALSE;
