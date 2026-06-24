import { create } from "zustand";

const ACCESS_TOKEN_KEY = "subtrack.accessToken";
const MEMBER_KEY = "subtrack.member";

function getInitialMember() {
  const savedMember = localStorage.getItem(MEMBER_KEY);

  if (!savedMember) {
    return null;
  }

  try {
    return JSON.parse(savedMember);
  } catch {
    localStorage.removeItem(MEMBER_KEY);
    return null;
  }
}

export const useAuthStore = create((set) => ({
  accessToken: localStorage.getItem(ACCESS_TOKEN_KEY),
  member: getInitialMember(),
  isAuthenticated: Boolean(localStorage.getItem(ACCESS_TOKEN_KEY)),

  setLogin: (accessToken, member) => {
    localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
    localStorage.setItem(MEMBER_KEY, JSON.stringify(member));

    set({
      accessToken,
      member,
      isAuthenticated: true,
    });
  },

  setMember: (member) => {
    localStorage.setItem(MEMBER_KEY, JSON.stringify(member));
    set({ member });
  },

  logout: () => {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(MEMBER_KEY);

    set({
      accessToken: null,
      member: null,
      isAuthenticated: false,
    });
  },
}));
