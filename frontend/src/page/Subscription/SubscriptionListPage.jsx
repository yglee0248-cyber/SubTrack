import { Alert, Button, CircularProgress, Pagination, Snackbar, Typography } from "@mui/material";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useMemo, useState } from "react";
import { getCategories } from "../../features/category/api/categoryApi";
import {
  createSubscription,
  deleteSubscription,
  getSubscription,
  getSubscriptions,
  updateSubscription,
} from "../../features/subscription/api/subscriptionApi";
import { getApiErrorMessage } from "../../shared/utils/errorMessage";
import { SubscriptionCard } from "./components/SubscriptionCard";
import { SubscriptionEmptyState } from "./components/SubscriptionEmptyState";
import { SubscriptionFilter } from "./components/SubscriptionFilter";
import { SubscriptionFormModal } from "./components/SubscriptionFormModal";
import styles from "./SubscriptionListPage.module.css";

const DEFAULT_FILTERS = {
  keyword: "",
  categoryId: "",
  status: "",
  paymentStatus: "",
  page: 0,
  size: 12,
};

function buildQueryParams(filters) {
  return {
    ...filters,
    categoryId: filters.categoryId ? Number(filters.categoryId) : "",
    page: Number(filters.page) || 0,
    size: Number(filters.size) || 12,
  };
}

function SubscriptionListPage() {
  const queryClient = useQueryClient();
  const [filters, setFilters] = useState(DEFAULT_FILTERS);
  const [draftFilters, setDraftFilters] = useState(DEFAULT_FILTERS);
  const [modalMode, setModalMode] = useState(null);
  const [editingSubscription, setEditingSubscription] = useState(null);
  const [formErrorMessage, setFormErrorMessage] = useState("");
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: "",
    severity: "success",
  });

  const categoriesQuery = useQuery({
    queryKey: ["categories"],
    queryFn: getCategories,
  });

  const subscriptionsQuery = useQuery({
    queryKey: ["subscriptions", filters],
    queryFn: () => getSubscriptions(buildQueryParams(filters)),
  });

  const detailQuery = useQuery({
    queryKey: ["subscription", editingSubscription?.subscriptionId],
    queryFn: () => getSubscription(editingSubscription.subscriptionId),
    enabled: modalMode === "edit" && Boolean(editingSubscription?.subscriptionId),
  });

  const categories = categoriesQuery.data || [];
  const subscriptions = subscriptionsQuery.data?.content || [];
  const totalCount = subscriptionsQuery.data?.totalCount || 0;
  const pageSize = subscriptionsQuery.data?.size || filters.size;
  const pageCount = Math.max(1, Math.ceil(totalCount / pageSize));

  const categoryMap = useMemo(() => {
    return categories.reduce((acc, category) => {
      acc[String(category.categoryId)] = category;
      return acc;
    }, {});
  }, [categories]);

  const createMutation = useMutation({
    mutationFn: createSubscription,
    onSuccess: () => {
      invalidateSubscriptionQueries();
      closeModal();
      showSnackbar("구독이 등록되었습니다.", "success");
    },
    onError: (error) => {
      setFormErrorMessage(getApiErrorMessage(error, "구독 등록에 실패했습니다."));
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ subscriptionId, payload }) => updateSubscription(subscriptionId, payload),
    onSuccess: (_data, variables) => {
      invalidateSubscriptionQueries();
      queryClient.invalidateQueries({ queryKey: ["subscription", variables.subscriptionId] });
      closeModal();
      showSnackbar("구독이 수정되었습니다.", "success");
    },
    onError: (error) => {
      setFormErrorMessage(getApiErrorMessage(error, "구독 수정에 실패했습니다."));
    },
  });

  const deleteMutation = useMutation({
    mutationFn: deleteSubscription,
    onSuccess: () => {
      invalidateSubscriptionQueries();
      showSnackbar("구독이 삭제되었습니다.", "success");
    },
    onError: (error) => {
      showSnackbar(getApiErrorMessage(error, "구독 삭제에 실패했습니다."), "error");
    },
  });

  const isModalSubmitting = createMutation.isPending || updateMutation.isPending;

  function invalidateSubscriptionQueries() {
    queryClient.invalidateQueries({ queryKey: ["subscriptions"] });
    queryClient.invalidateQueries({ queryKey: ["dashboardSummary"] });
    queryClient.invalidateQueries({ queryKey: ["dashboardUpcoming"] });
    queryClient.invalidateQueries({ queryKey: ["dashboardCategoryExpenses"] });
  }

  function showSnackbar(message, severity = "success") {
    setSnackbar({
      open: true,
      message,
      severity,
    });
  }

  function openCreateModal() {
    setModalMode("create");
    setEditingSubscription(null);
    setFormErrorMessage("");
  }

  function openEditModal(subscription) {
    setModalMode("edit");
    setEditingSubscription(subscription);
    setFormErrorMessage("");
  }

  function closeModal() {
    setModalMode(null);
    setEditingSubscription(null);
    setFormErrorMessage("");
  }

  function handleFilterChange(field, value) {
    setDraftFilters((prev) => ({
      ...prev,
      [field]: value,
    }));
  }

  function applyFilters() {
    setFilters((prev) => ({
      ...prev,
      ...draftFilters,
      page: 0,
    }));
  }

  function resetFilters() {
    setDraftFilters(DEFAULT_FILTERS);
    setFilters(DEFAULT_FILTERS);
  }

  function handlePageChange(_event, page) {
    setFilters((prev) => ({
      ...prev,
      page: page - 1,
    }));
  }

  function handleSubmitForm(payload) {
    if (modalMode === "edit" && editingSubscription?.subscriptionId) {
      updateMutation.mutate({
        subscriptionId: editingSubscription.subscriptionId,
        payload,
      });
      return;
    }

    createMutation.mutate(payload);
  }

  function handleDelete(subscription) {
    const confirmed = window.confirm(`'${subscription.name}' 구독을 삭제할까요?`);

    if (confirmed) {
      deleteMutation.mutate(subscription.subscriptionId);
    }
  }

  const modalInitialValues = detailQuery.data || editingSubscription;
  const isFirstLoading = subscriptionsQuery.isLoading || categoriesQuery.isLoading;
  const hasError = subscriptionsQuery.isError || categoriesQuery.isError;

  return (
    <section className={styles.page}>
      <div className={styles.pageHeader}>
        <div>
          <Typography variant="h4" component="h1" className={styles.title}>
            구독 관리
          </Typography>
          <Typography variant="body2" className={styles.description}>
            반복 결제되는 서비스를 등록하고 다음 결제일을 확인하세요.
          </Typography>
        </div>
        <Button
          variant="contained"
          onClick={openCreateModal}
          className={styles.addButton}
          data-testid="subscription-add-button"
        >
          구독 추가
        </Button>
      </div>

      <SubscriptionFilter
        filters={draftFilters}
        categories={categories}
        onChange={handleFilterChange}
        onSubmit={applyFilters}
        onReset={resetFilters}
        isLoading={subscriptionsQuery.isFetching}
      />

      {hasError && (
        <Alert severity="error" className={styles.stateBox}>
          {getApiErrorMessage(
            subscriptionsQuery.error || categoriesQuery.error,
            "구독 정보를 불러오지 못했습니다."
          )}
        </Alert>
      )}

      {isFirstLoading ? (
        <div className={styles.loadingBox}>
          <CircularProgress size={28} />
          <Typography variant="body2">구독 목록을 불러오는 중입니다.</Typography>
        </div>
      ) : (
        !hasError && (
          <>
            <div className={styles.listSummary}>
              <span>총 {totalCount.toLocaleString("ko-KR")}개</span>
              {subscriptionsQuery.isFetching && <span>새로고침 중</span>}
            </div>

            {subscriptions.length === 0 ? (
              <SubscriptionEmptyState onCreate={openCreateModal} />
            ) : (
              <div className={styles.cardGrid}>
                {subscriptions.map((subscription) => (
                  <SubscriptionCard
                    key={subscription.subscriptionId}
                    subscription={subscription}
                    category={categoryMap[String(subscription.categoryId)]}
                    onEdit={openEditModal}
                    onDelete={handleDelete}
                    isDeleting={deleteMutation.isPending}
                  />
                ))}
              </div>
            )}

            {totalCount > 0 && (
              <div className={styles.paginationArea}>
                <Pagination
                  count={pageCount}
                  page={filters.page + 1}
                  onChange={handlePageChange}
                  color="primary"
                  showFirstButton
                  showLastButton
                />
              </div>
            )}
          </>
        )
      )}

      <SubscriptionFormModal
        open={Boolean(modalMode)}
        mode={modalMode}
        categories={categories}
        initialValues={modalInitialValues}
        isInitialLoading={modalMode === "edit" && detailQuery.isLoading}
        isSubmitting={isModalSubmitting}
        errorMessage={formErrorMessage}
        onClose={closeModal}
        onSubmit={handleSubmitForm}
      />

      <Snackbar
        open={snackbar.open}
        autoHideDuration={2800}
        onClose={() => setSnackbar((prev) => ({ ...prev, open: false }))}
        anchorOrigin={{ vertical: "bottom", horizontal: "center" }}
      >
        <Alert
          severity={snackbar.severity}
          variant="filled"
          onClose={() => setSnackbar((prev) => ({ ...prev, open: false }))}
        >
          {snackbar.message}
        </Alert>
      </Snackbar>
    </section>
  );
}

export default SubscriptionListPage;
