import { api } from "../../../shared/api/api";

export async function signup(payload) {
  const response = await api.post("/api/auth/signup", payload);
  return response.data.data;
}

export async function login(payload) {
  const response = await api.post("/api/auth/login", payload);
  return response.data.data;
}

export async function logout() {
  const response = await api.post("/api/auth/logout");
  return response.data.data;
}

export async function getMe() {
  const response = await api.get("/api/members/me");
  return response.data.data;
}

export async function updateNickname(payload) {
  const response = await api.patch("/api/members/me/nickname", payload);
  return response.data.data;
}
