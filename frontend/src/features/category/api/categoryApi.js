import { api } from "../../../shared/api/api";

export async function getCategories() {
  const response = await api.get("/api/categories");
  return response.data.data;
}
