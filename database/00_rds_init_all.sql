-- SubTrack RDS initial SQL for a new empty MySQL database.
-- Recommended flow: create the RDS database with DB name `subtrack`,
-- connect to that database, then run this file.
-- This file intentionally does not include CREATE DATABASE or USE statements.
-- It initializes the tables used by the current backend runtime.
-- P1 candidate tables such as payment_history, notification, and push_log
-- are intentionally not created here.

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `member` (
    member_id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    nickname VARCHAR(30) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    push_enabled TINYINT(1) NOT NULL DEFAULT 0,
    onesignal_player_id VARCHAR(255) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted_at DATETIME(6) NULL,
    PRIMARY KEY (member_id),
    UNIQUE KEY uk_member_email (email),
    KEY idx_member_status_deleted (status, deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS subscription_category (
    category_id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    color_code VARCHAR(20) NULL,
    icon VARCHAR(50) NULL,
    sort_order INT NOT NULL DEFAULT 0,
    is_default TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (category_id),
    UNIQUE KEY uk_subscription_category_name (name),
    KEY idx_subscription_category_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO subscription_category
    (category_id, name, color_code, icon, sort_order, is_default)
VALUES
    (1, '영상', '#E53935', 'movie', 1, 1),
    (2, '음악', '#8E24AA', 'music_note', 2, 1),
    (3, '클라우드', '#1E88E5', 'cloud', 3, 1),
    (4, '생산성', '#43A047', 'work', 4, 1),
    (5, '인공지능 도구', '#5E35B1', 'smart_toy', 5, 1),
    (6, '쇼핑', '#FB8C00', 'shopping_cart', 6, 1),
    (7, '교육', '#3949AB', 'school', 7, 1),
    (8, '금융', '#00897B', 'account_balance', 8, 1),
    (9, '생활', '#6D4C41', 'favorite', 9, 1),
    (10, '기타', '#757575', 'more_horiz', 99, 1)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    color_code = VALUES(color_code),
    icon = VALUES(icon),
    sort_order = VALUES(sort_order),
    is_default = VALUES(is_default),
    updated_at = NOW(6);

CREATE TABLE IF NOT EXISTS subscription (
    subscription_id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    price DECIMAL(12, 2) NOT NULL,
    currency CHAR(3) NOT NULL DEFAULT 'KRW',
    billing_cycle VARCHAR(20) NOT NULL,
    billing_anchor_day TINYINT UNSIGNED NOT NULL,
    billing_start_date DATE NOT NULL COMMENT '구독 시작일 또는 첫 결제일',
    next_payment_date DATE NOT NULL,
    payment_method VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    memo VARCHAR(500) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted_at DATETIME(6) NULL,
    PRIMARY KEY (subscription_id),
    CONSTRAINT fk_subscription_member
        FOREIGN KEY (member_id) REFERENCES `member` (member_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_subscription_category
        FOREIGN KEY (category_id) REFERENCES subscription_category (category_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_subscription_price
        CHECK (price >= 0),
    CONSTRAINT chk_subscription_billing_anchor_day
        CHECK (billing_anchor_day BETWEEN 1 AND 31),
    KEY idx_subscription_member_deleted_next_payment (member_id, deleted_at, next_payment_date),
    KEY idx_subscription_member_billing_start (member_id, billing_start_date),
    KEY idx_subscription_member_status_deleted (member_id, status, deleted_at),
    KEY idx_subscription_member_category (member_id, category_id),
    KEY idx_subscription_member_name (member_id, name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS subscription_status_history (
    status_history_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '구독 상태 이력 ID',
    subscription_id BIGINT NOT NULL COMMENT '구독 ID',
    status VARCHAR(20) NOT NULL COMMENT '상태: ACTIVE, PAUSED, CANCELED',
    effective_start_date DATE NOT NULL COMMENT '상태 적용 시작일',
    effective_end_date DATE NULL COMMENT '상태 적용 종료일',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='구독 상태 이력';

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
