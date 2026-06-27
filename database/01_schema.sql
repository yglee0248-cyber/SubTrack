CREATE TABLE `member` (
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


CREATE TABLE subscription_category (
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


CREATE TABLE subscription (
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


CREATE TABLE subscription_status_history (
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


CREATE TABLE payment_history (
    payment_history_id BIGINT NOT NULL AUTO_INCREMENT,
    subscription_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    scheduled_payment_date DATE NOT NULL,
    paid_date DATE NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    currency CHAR(3) NOT NULL DEFAULT 'KRW',
    payment_method VARCHAR(30) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (payment_history_id),
    UNIQUE KEY uk_payment_history_subscription_scheduled_date (subscription_id, scheduled_payment_date),
    CONSTRAINT fk_payment_history_subscription
        FOREIGN KEY (subscription_id) REFERENCES subscription (subscription_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_payment_history_member
        FOREIGN KEY (member_id) REFERENCES `member` (member_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_payment_history_amount
        CHECK (amount >= 0),
    KEY idx_payment_history_member_paid_date (member_id, paid_date),
    KEY idx_payment_history_member_scheduled_date (member_id, scheduled_payment_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE exchange_rate (
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


CREATE TABLE notification (
    notification_id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    subscription_id BIGINT NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    scheduled_date DATE NOT NULL,
    title VARCHAR(100) NOT NULL,
    message VARCHAR(500) NOT NULL,
    is_read TINYINT(1) NOT NULL DEFAULT 0,
    read_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (notification_id),
    UNIQUE KEY uk_notification_unique_event (member_id, subscription_id, notification_type, scheduled_date),
    CONSTRAINT fk_notification_member
        FOREIGN KEY (member_id) REFERENCES `member` (member_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_notification_subscription
        FOREIGN KEY (subscription_id) REFERENCES subscription (subscription_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    KEY idx_notification_member_read_created (member_id, is_read, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE push_log (
    push_log_id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    subscription_id BIGINT NOT NULL,
    notification_id BIGINT NULL,
    notification_type VARCHAR(50) NOT NULL,
    scheduled_date DATE NOT NULL,
    target_player_id VARCHAR(255) NULL,
    send_status VARCHAR(20) NOT NULL,
    http_status_code INT NULL,
    request_body JSON NULL,
    response_body JSON NULL,
    error_message VARCHAR(1000) NULL,
    sent_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (push_log_id),
    UNIQUE KEY uk_push_log_subscription_type_date (subscription_id, notification_type, scheduled_date),
    CONSTRAINT fk_push_log_member
        FOREIGN KEY (member_id) REFERENCES `member` (member_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_push_log_subscription
        FOREIGN KEY (subscription_id) REFERENCES subscription (subscription_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_push_log_notification
        FOREIGN KEY (notification_id) REFERENCES notification (notification_id)
        ON UPDATE CASCADE ON DELETE SET NULL,
    KEY idx_push_log_member_scheduled_date (member_id, scheduled_date),
    KEY idx_push_log_status_created (send_status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
