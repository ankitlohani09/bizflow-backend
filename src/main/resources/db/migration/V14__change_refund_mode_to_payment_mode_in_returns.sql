ALTER TABLE returns DROP COLUMN refund_mode;
ALTER TABLE returns ADD COLUMN payment_mode_id BIGINT;
ALTER TABLE returns ADD CONSTRAINT fk_returns_payment_mode FOREIGN KEY (payment_mode_id) REFERENCES payment_modes(id);
