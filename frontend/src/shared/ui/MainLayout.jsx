import { NavLink, Outlet, useLocation, useNavigate } from "react-router-dom";
import { logout as requestLogout } from "../../features/auth/api/authApi";
import { useAuthStore } from "../../features/auth/store/authStore";
import styles from "./MainLayout.module.css";

export function MainLayout() {
  const navigate = useNavigate();
  const location = useLocation();
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const member = useAuthStore((state) => state.member);
  const logout = useAuthStore((state) => state.logout);

  const handleLogout = async () => {
    try {
      await requestLogout();
    } finally {
      logout();
      navigate("/login");
    }
  };

  const getNavLinkClassName = ({ isActive }) =>
    isActive ? `${styles.navLink} ${styles.activeNavLink}` : styles.navLink;

  const isLoginPage = location.pathname === "/login";
  const isSignupPage = location.pathname === "/signup";

  return (
    <div className={styles.appShell}>
      <header className={styles.header}>
        <div className={styles.headerInner}>
          <NavLink
            to={isAuthenticated ? "/dashboard" : "/"}
            className={styles.brand}
            aria-label="SubTrack home"
          >
            <span className={styles.brandMark}>S</span>
            <span className={styles.brandText}>SubTrack</span>
          </NavLink>

          {isAuthenticated ? (
            <>
              <nav className={styles.appNav} aria-label="main navigation">
                <NavLink to="/dashboard" className={getNavLinkClassName}>
                  Dashboard
                </NavLink>
                <NavLink to="/subscriptions" className={getNavLinkClassName}>
                  Subscriptions
                </NavLink>
                <NavLink to="/my" className={getNavLinkClassName}>
                  My Page
                </NavLink>
              </nav>

              <div className={styles.accountArea}>
                <span className={styles.userName}>{member?.nickname || "사용자"}</span>
                <button type="button" className={styles.logoutButton} onClick={handleLogout}>
                  Logout
                </button>
              </div>
            </>
          ) : (
            <div className={styles.authActions}>
              {!isLoginPage && (
                <NavLink to="/login" className={styles.authLink}>
                  로그인
                </NavLink>
              )}
              {!isSignupPage && (
                <NavLink to="/signup" className={`${styles.authLink} ${styles.primaryAuthLink}`}>
                  회원가입
                </NavLink>
              )}
            </div>
          )}
        </div>
      </header>

      <main className={styles.main}>
        <div className={styles.mainInner}>
          <Outlet />
        </div>
      </main>
    </div>
  );
}
