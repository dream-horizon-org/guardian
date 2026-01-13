-- liquibase formatted sql
-- changeset guardian:2

-- Add OTP rate limiting columns to otp_config table
ALTER TABLE otp_config ADD COLUMN otp_send_window_seconds INT NOT NULL DEFAULT 86400 COMMENT 'Window for OTP resend counter';

ALTER TABLE otp_config ADD COLUMN otp_send_window_max_count INT NOT NULL DEFAULT 10 COMMENT 'Max resend count for a user within a window';

ALTER TABLE otp_config ADD COLUMN otp_send_block_seconds INT NOT NULL DEFAULT 86400 COMMENT 'Block duration in seconds when max resend count exceeded';
