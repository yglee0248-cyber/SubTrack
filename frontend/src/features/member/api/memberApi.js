import { api } from "../../../shared/api/api";

export async function getMyProfile() {
  const response = await api.get("/api/members/me");
  return response.data.data;
}

export async function updateMyProfile(payload) {
  const response = await api.patch("/api/members/me/profile", payload);
  return response.data.data;
}

export async function updateMyNickname(payload) {
  const response = await api.patch("/api/members/me/nickname", payload);
  return response.data.data;
}

export async function changeMyPassword(payload) {
  const response = await api.patch("/api/members/me/password", payload);
  return response.data.data;
}
