-- Add permissions column to roles table
ALTER TABLE roles ADD COLUMN permissions TEXT NULL;

-- Update default roles with permissions
UPDATE roles SET permissions = 'ALL' WHERE name = 'OWNER';
UPDATE roles SET permissions = 'CUSTOMER_READ,CUSTOMER_WRITE,INVOICE_READ,INVOICE_WRITE,RETURN_READ,RETURN_WRITE,KITCHEN_READ,KITCHEN_WRITE' WHERE name = 'MANAGER';
UPDATE roles SET permissions = 'CUSTOMER_READ,INVOICE_READ,INVOICE_WRITE,RETURN_READ,KITCHEN_READ' WHERE name = 'CASHIER';
