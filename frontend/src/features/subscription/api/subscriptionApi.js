import { api } from "../../../shared/api/api";

function cleanParams(params = {}) {
  return Object.entries(params).reduce((acc, [key, value]) => {
    if (value !== "" && value !== null && value !== undefined) {
      acc[key] = value;
    }

    return acc;
  }, {});
}

export async function getSubscriptions(params) {
  const response = await api.get("/api/subscriptions", {
    params: cleanParams(params),
  });

  return response.data.data;
}

export async function getSubscription(subscriptionId) {
  const response = await api.get(`/api/subscriptions/${subscriptionId}`);
  return response.data.data;
}

export async function createSubscription(payload) {
  const response = await api.post("/api/subscriptions", payload);
  return response.data.data;
}

export async function updateSubscription(subscriptionId, payload) {
  const response = await api.put(`/api/subscriptions/${subscriptionId}`, payload);
  return response.data.data;
}

export async function deleteSubscription(subscriptionId) {
  const response = await api.delete(`/api/subscriptions/${subscriptionId}`);
  return response.data.data;
}
