-- Add PIN to staff
ALTER TABLE staff ADD COLUMN pin VARCHAR(4);

-- Add photo_url and location to attendance
ALTER TABLE attendance ADD COLUMN photo_url VARCHAR(255);
ALTER TABLE attendance ADD COLUMN location VARCHAR(255);
