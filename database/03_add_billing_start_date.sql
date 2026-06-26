ALTER TABLE subscription
  ADD COLUMN billing_start_date DATE NULL COMMENT '구독 시작일 또는 첫 결제일'
  AFTER billing_anchor_day;

UPDATE subscription
SET billing_start_date = next_payment_date
WHERE billing_start_date IS NULL;

ALTER TABLE subscription
  MODIFY billing_start_date DATE NOT NULL COMMENT '구독 시작일 또는 첫 결제일';

CREATE INDEX idx_subscription_member_billing_start
  ON subscription (member_id, billing_start_date);
