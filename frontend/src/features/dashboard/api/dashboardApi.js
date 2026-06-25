import { api } from "../../../shared/api/api";

export async function getDashboardSummary(yearMonth) {
  const response = await api.get("/api/dashboard/summary", {
    params: { yearMonth },
  });

  return response.data.data;
}

export async function getDashboardUpcoming(days = 7) {
  const response = await api.get("/api/dashboard/upcoming", {
    params: { days },
  });

  return response.data.data;
}

export async function getCategoryExpenses(yearMonth) {
  const response = await api.get("/api/dashboard/category-expenses", {
    params: { yearMonth },
  });

  return response.data.data;
}
