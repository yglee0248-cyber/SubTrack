CREATE TABLE IF NOT EXISTS subscription_status_history (
    status_history_id BIGINT NOT NULL AUTO_INCREMENT,
    subscription_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    effective_start_date DATE NOT NULL,
    effective_end_date DATE NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (status_history_id),
    CONSTRAINT fk_subscription_status_history_subscription
        FOREIGN KEY (subscription_id) REFERENCES subscription (subscription_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_subscription_status_history_status
        CHECK (status IN ('ACTIVE', 'PAUSED', 'CANCELED')),
    CONSTRAINT chk_subscription_status_history_date_range
        CHECK (effective_end_date IS NULL OR effective_start_date <= effective_end_date),
    KEY idx_status_history_subscription_period (subscription_id, effective_start_date, effective_end_date),
    KEY idx_status_history_subscription_status (subscription_id, status, effective_start_date, effective_end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO subscription_status_history (
    subscription_id,
    status,
    effective_start_date,
    effective_end_date
)
SELECT
    s.subscription_id,
    'ACTIVE',
    s.billing_start_date,
    DATE_SUB(DATE(s.updated_at), INTERVAL 1 DAY)
FROM subscription s
WHERE s.status IN ('PAUSED', 'CANCELED')
  AND DATE(s.updated_at) > s.billing_start_date
  AND NOT EXISTS (
      SELECT 1
      FROM subscription_status_history h
      WHERE h.subscription_id = s.subscription_id
        AND h.status = 'ACTIVE'
        AND h.effective_start_date = s.billing_start_date
        AND h.effective_end_date = DATE_SUB(DATE(s.updated_at), INTERVAL 1 DAY)
  );

INSERT INTO subscription_status_history (
    subscription_id,
    status,
    effective_start_date,
    effective_end_date
)
SELECT
    s.subscription_id,
    s.status,
    CASE
        WHEN s.status IN ('PAUSED', 'CANCELED') AND DATE(s.updated_at) > s.billing_start_date
            THEN DATE(s.updated_at)
        ELSE s.billing_start_date
    END,
    NULL
FROM subscription s
WHERE NOT EXISTS (
    SELECT 1
    FROM subscription_status_history h
    WHERE h.subscription_id = s.subscription_id
      AND h.effective_end_date IS NULL
);
