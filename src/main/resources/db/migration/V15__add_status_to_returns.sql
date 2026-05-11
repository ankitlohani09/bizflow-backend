-- Add status column to returns table
ALTER TABLE returns ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDING';
