import { Button, Container } from "@mui/material";
import { NavLink, Outlet, useNavigate } from "react-router-dom";
import { logout as requestLogout } from "../../features/auth/api/authApi";
import { useAuthStore } from "../../features/auth/store/authStore";
import styles from "./MainLayout.module.css";

export function MainLayout() {
  const navigate = useNavigate();
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

  return (
    <div className={styles.appShell}>
      <header className={styles.header}>
        <Container maxWidth="lg" className={styles.navInner}>
          <NavLink to="/" className={styles.brand}>
            SubTrack
          </NavLink>

          <nav className={styles.navLinks} aria-label="main navigation">
            {isAuthenticated && (
              <>
                <NavLink to="/dashboard" className={styles.navLink}>
                  Dashboard
                </NavLink>
                <NavLink to="/subscriptions" className={styles.navLink}>
                  Subscriptions
                </NavLink>
                <NavLink to="/my" className={styles.navLink}>
                  My Page
                </NavLink>
              </>
            )}
          </nav>

          <div className={styles.authArea}>
            {isAuthenticated ? (
              <>
                <span className={styles.nickname}>{member?.nickname}</span>
                <Button size="small" variant="outlined" onClick={handleLogout}>
                  Logout
                </Button>
              </>
            ) : (
              <>
                <Button size="small" variant="text" onClick={() => navigate("/login")}>
                  Login
                </Button>
                <Button size="small" variant="contained" onClick={() => navigate("/signup")}>
                  Sign up
                </Button>
              </>
            )}
          </div>
        </Container>
      </header>

      <main className={styles.main}>
        <Container maxWidth="lg">
          <Outlet />
        </Container>
      </main>
    </div>
  );
}
