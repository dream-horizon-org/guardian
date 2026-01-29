-- liquibase formatted sql
-- changeset guardian:4

-- Password/PIN block config for MFA signin (blocks only password and PIN methods, not entire MFA API).
CREATE TABLE password_pin_block_config
(
    tenant_id                CHAR(10) PRIMARY KEY,
    attempts_allowed         INT         NOT NULL DEFAULT 5 COMMENT 'Max wrong attempts per (userId, deviceId)',
    attempts_window_seconds  INT         NOT NULL DEFAULT 3600 COMMENT 'Duration after which attempts counter resets',
    block_interval_seconds  INT         NOT NULL DEFAULT 86400 COMMENT 'Block duration from block initiated time (e.g. 24 hrs)',
    created_at               TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at               TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_tenant_password_pin_block_config FOREIGN KEY (tenant_id)
        REFERENCES tenant (id) ON DELETE CASCADE,
    UNIQUE KEY idx_tenant_id (tenant_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
