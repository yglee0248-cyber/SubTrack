CREATE TABLE IF NOT EXISTS exchange_rate (
    exchange_rate_id BIGINT NOT NULL AUTO_INCREMENT,
    currency_code VARCHAR(3) NOT NULL,
    target_currency VARCHAR(3) NOT NULL DEFAULT 'KRW',
    rate_to_krw DECIMAL(18, 6) NOT NULL,
    rate_date DATE NOT NULL,
    provider VARCHAR(50) NOT NULL,
    fetched_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (exchange_rate_id),
    UNIQUE KEY uk_exchange_rate_currency_rate_date_provider
        (currency_code, target_currency, rate_date, provider),
    KEY idx_exchange_rate_currency_fetched_at
        (currency_code, target_currency, fetched_at),
    CONSTRAINT chk_exchange_rate_rate_to_krw
        CHECK (rate_to_krw > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO exchange_rate (
    currency_code,
    target_currency,
    rate_to_krw,
    rate_date,
    provider,
    fetched_at
) VALUES
    ('USD', 'KRW', 1400.000000, '1970-01-01', 'SYSTEM', '1970-01-01 00:00:00.000000'),
    ('JPY', 'KRW', 9.500000, '1970-01-01', 'SYSTEM', '1970-01-01 00:00:00.000000'),
    ('CNY', 'KRW', 190.000000, '1970-01-01', 'SYSTEM', '1970-01-01 00:00:00.000000'),
    ('EUR', 'KRW', 1500.000000, '1970-01-01', 'SYSTEM', '1970-01-01 00:00:00.000000')
ON DUPLICATE KEY UPDATE
    rate_to_krw = VALUES(rate_to_krw),
    fetched_at = VALUES(fetched_at),
    updated_at = NOW(6);
