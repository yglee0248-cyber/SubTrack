import axios from "axios";
import { useAuthStore } from "../../features/auth/store/authStore";

const BASE_URL = import.meta.env.VITE_BACKSERVER || "http://localhost:8080";

export const api = axios.create({
  baseURL: BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

api.interceptors.request.use(
  (config) => {
    const accessToken = useAuthStore.getState().accessToken;

    if (accessToken) {
      config.headers.Authorization = `Bearer ${accessToken}`;
    }

    return config;
  },
  (error) => Promise.reject(error)
);

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Later, this can trigger token expiration handling or a toast message.
    }

    return Promise.reject(error);
  }
);
