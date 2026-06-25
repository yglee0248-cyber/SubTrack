import { Button, TextField } from "@mui/material";
import {
  PAYMENT_STATUS_OPTIONS,
  SUBSCRIPTION_STATUS_OPTIONS,
} from "../../../features/subscription/utils/subscriptionLabel";
import styles from "../SubscriptionListPage.module.css";

export function SubscriptionFilter({ filters, categories, onChange, onSubmit, onReset, isLoading }) {
  const handleSubmit = (event) => {
    event.preventDefault();
    onSubmit();
  };

  return (
    <form className={styles.filterPanel} onSubmit={handleSubmit} data-testid="subscription-filter-form">
      <TextField
        label="검색어"
        placeholder="구독 이름 검색"
        value={filters.keyword}
        onChange={(event) => onChange("keyword", event.target.value)}
        InputLabelProps={{ shrink: true }}
        inputProps={{ "data-testid": "filter-keyword-input" }}
        size="small"
        fullWidth
      />

      <TextField
        select
        label="카테고리"
        value={filters.categoryId}
        onChange={(event) => onChange("categoryId", event.target.value)}
        InputLabelProps={{ shrink: true }}
        SelectProps={{ native: true }}
        inputProps={{ "data-testid": "filter-category-select" }}
        size="small"
        fullWidth
      >
        <option value="">전체</option>
        {categories.map((category) => (
          <option key={category.categoryId} value={category.categoryId}>
            {category.name}
          </option>
        ))}
      </TextField>

      <TextField
        select
        label="구독 상태"
        value={filters.status}
        onChange={(event) => onChange("status", event.target.value)}
        InputLabelProps={{ shrink: true }}
        SelectProps={{ native: true }}
        inputProps={{ "data-testid": "filter-status-select" }}
        size="small"
        fullWidth
      >
        <option value="">전체</option>
        {SUBSCRIPTION_STATUS_OPTIONS.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </TextField>

      <TextField
        select
        label="결제 상태"
        value={filters.paymentStatus}
        onChange={(event) => onChange("paymentStatus", event.target.value)}
        InputLabelProps={{ shrink: true }}
        SelectProps={{ native: true }}
        inputProps={{ "data-testid": "filter-payment-status-select" }}
        size="small"
        fullWidth
      >
        <option value="">전체</option>
        {PAYMENT_STATUS_OPTIONS.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </TextField>

      <TextField
        select
        label="페이지 크기"
        value={filters.size}
        onChange={(event) => onChange("size", Number(event.target.value))}
        InputLabelProps={{ shrink: true }}
        SelectProps={{ native: true }}
        inputProps={{ "data-testid": "filter-size-select" }}
        size="small"
        fullWidth
      >
        <option value={6}>6개</option>
        <option value={12}>12개</option>
        <option value={24}>24개</option>
      </TextField>

      <div className={styles.filterActions}>
        <Button type="submit" variant="contained" disabled={isLoading} data-testid="filter-submit-button">
          검색
        </Button>
        <Button type="button" variant="outlined" onClick={onReset} disabled={isLoading}>
          초기화
        </Button>
      </div>
    </form>
  );
}
