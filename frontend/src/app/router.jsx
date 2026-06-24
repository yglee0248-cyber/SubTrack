import { createBrowserRouter, Navigate, Outlet } from "react-router-dom";
import { MainLayout } from "../shared/ui/MainLayout";
import { ProtectedRoute } from "../shared/ui/ProtectedRoute";
import LoginPage from "../page/Auth/LoginPage";
import SignupPage from "../page/Auth/SignupPage";
import DashboardPage from "../page/Dashboard/DashboardPage";
import LandingPage from "../page/Landing/LandingPage";
import MyPage from "../page/MyPage/MyPage";
import SubscriptionListPage from "../page/Subscription/SubscriptionListPage";

const protectedRoutes = (
  <ProtectedRoute>
    <Outlet />
  </ProtectedRoute>
);

export const router = createBrowserRouter([
  {
    element: <MainLayout />,
    children: [
      {
        path: "/",
        element: <LandingPage />,
      },
      {
        path: "/login",
        element: <LoginPage />,
      },
      {
        path: "/signup",
        element: <SignupPage />,
      },
      {
        element: protectedRoutes,
        children: [
          {
            path: "/dashboard",
            element: <DashboardPage />,
          },
          {
            path: "/subscriptions",
            element: <SubscriptionListPage />,
          },
          {
            path: "/my",
            element: <MyPage />,
          },
        ],
      },
      {
        path: "*",
        element: <Navigate to="/" replace />,
      },
    ],
  },
]);
