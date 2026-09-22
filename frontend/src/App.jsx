
import { useEffect, useState } from "react";
import { ApiError } from "./api/http";
import { getCurrentAccount, login, logout } from "./api/auth";
import StudentPage from "./pages/StudentPage";
import CatalogPage from "./pages/CatalogPage";
import StudentPortalPage from "./pages/StudentPortalPage";
import GradebookPage from "./pages/GradebookPage";
import StudentGradesPage from "./pages/StudentGradesPage";
import DashboardPage from "./pages/DashboardPage";
import "./App.css";

const roleNames = {
  ADMIN: "Quản trị viên",
  TEACHER: "Giảng viên",
  STUDENT: "Sinh viên",
};

const roleFeatures = {
  ADMIN: [
    {
      title: "Quản lý sinh viên",
      description: "Quản lý hồ sơ sinh viên và lớp hành chính.",
    },
    {
      title: "Quản lý đào tạo",
      description: "Quản lý khoa, ngành, môn học và lớp học phần.",
    },
    {
      title: "Quản lý kết quả",
      description: "Theo dõi điểm và cấu hình tỷ trọng điểm.",
    },
  ],
  TEACHER: [
    {
      title: "Lớp được phân công",
      description: "Theo dõi các lớp học phần được giao giảng dạy.",
    },
    {
      title: "Danh sách sinh viên",
      description: "Xem sinh viên đăng ký trong lớp phụ trách.",
    },
    {
      title: "Nhập điểm",
      description: "Nhập điểm TX1, TX2 và thi kết thúc học phần.",
    },
  ],
  STUDENT: [
    {
      title: "Hồ sơ cá nhân",
      description: "Xem thông tin sinh viên của mình.",
    },
    {
      title: "Đăng ký học phần",
      description: "Theo dõi và đăng ký các lớp học phần đang mở.",
    },
    {
      title: "Kết quả học tập",
      description: "Xem điểm thành phần, điểm tổng kết và điểm chữ.",
    },
  ],
};

export default function App() {
  const [activePage, setActivePage] = useState("dashboard");
  const [account, setAccount] = useState(null);
  const [initializing, setInitializing] = useState(true);
  const [startupError, setStartupError] = useState("");

  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);

  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");

  useEffect(() => {
    let active = true;

    async function restoreSession() {
      try {
        const currentAccount = await getCurrentAccount();

        if (active) {
          setAccount(currentAccount);
        }
      } catch (requestError) {
        if (!active) {
          return;
        }

        if (
          requestError instanceof ApiError &&
          requestError.status === 401
        ) {
          // Chưa đăng nhập: hiển thị biểu mẫu đăng nhập.
          setAccount(null);
        } else {
          setStartupError(requestError.message);
        }
      } finally {
        if (active) {
          setInitializing(false);
        }
      }
    }

    restoreSession();

    return () => {
      active = false;
    };
  }, []);

  async function handleLogin(event) {
    event.preventDefault();

    if (busy) {
      return;
    }

    setError("");
    setNotice("");

    if (!username.trim() || !password) {
      setError("Vui lòng nhập tên đăng nhập và mật khẩu.");
      return;
    }

    setBusy(true);

    try {
      const currentAccount = await login(username, password);

      setAccount(currentAccount);
      setActivePage("dashboard");
      setPassword("");
      setShowPassword(false);
    } catch (requestError) {
      if (
        requestError instanceof ApiError &&
        requestError.status === 401
      ) {
        setError("Tên đăng nhập hoặc mật khẩu không đúng, hoặc tài khoản đã bị khóa.");
      } else if (
        requestError instanceof ApiError &&
        requestError.status === 403
      ) {
        setError("Yêu cầu bị từ chối. Hãy tải lại trang rồi đăng nhập lại.");
      } else {
        setError(
          `${requestError.message} Nếu đã gửi đăng nhập, hãy tải lại trang để kiểm tra phiên.`,
        );
      }
    } finally {
      setBusy(false);
    }
  }

  async function handleLogout() {
    if (busy) {
      return;
    }

    setBusy(true);
    setError("");
    setNotice("");

    try {
      await logout();

      setAccount(null);
      setActivePage("dashboard");
      setUsername("");
      setPassword("");
      setShowPassword(false);
      setNotice("Bạn đã đăng xuất thành công.");
    } catch (requestError) {
      setError(
        `${requestError.message} Hãy tải lại trang để kiểm tra trạng thái đăng nhập.`,
      );
    } finally {
      setBusy(false);
    }
  }

  if (initializing) {
    return (
      <main className="auth-screen">
        <section className="auth-card" aria-live="polite">
          <h1>Student Manager</h1>
          <p>Đang kiểm tra phiên đăng nhập...</p>
        </section>
      </main>
    );
  }

  if (startupError) {
    return (
      <main className="auth-screen">
        <section className="auth-card">
          <h1>Chưa thể kết nối</h1>

          <p className="auth-message error" role="alert">
            {startupError}
          </p>

          <button
            className="primary-button"
            type="button"
            onClick={() => window.location.reload()}
          >
            Thử lại
          </button>
        </section>
      </main>
    );
  }

  if (!account) {
    return (
      <main className="auth-screen">
        <section className="auth-card">
          <div className="brand-logo">SM</div>

          <p className="auth-eyebrow">STUDENT MANAGER</p>
          <h1>Đăng nhập</h1>
          <p className="auth-description">
            Sử dụng tài khoản được cấp để truy cập hệ thống.
          </p>

          {notice && (
            <p className="auth-message success" role="status">
              {notice}
            </p>
          )}

          {error && (
            <p className="auth-message error" role="alert">
              {error}
            </p>
          )}

          <form onSubmit={handleLogin}>
            <div className="form-field">
              <label htmlFor="username">Tên đăng nhập</label>
              <input
                id="username"
                name="username"
                type="text"
                autoComplete="username"
                value={username}
                onChange={(event) => setUsername(event.target.value)}
                maxLength={50}
                disabled={busy}
                required
              />
            </div>

            <div className="form-field">
              <label htmlFor="password">Mật khẩu</label>
              <input
                id="password"
                name="password"
                type={showPassword ? "text" : "password"}
                autoComplete="current-password"
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                disabled={busy}
                required
              />
            </div>

            <label className="show-password">
              <input
                type="checkbox"
                checked={showPassword}
                onChange={(event) => setShowPassword(event.target.checked)}
                disabled={busy}
              />
              Hiện mật khẩu
            </label>

            <button
              className="primary-button full-width"
              type="submit"
              disabled={busy}
            >
              {busy ? "Đang đăng nhập..." : "Đăng nhập"}
            </button>
          </form>
        </section>
      </main>
    );
  }

  const features = roleFeatures[account.role] || [];

  const menus = {
  ADMIN: [
    ["dashboard", "Tổng quan"],
    ["students", "Quản lý sinh viên"],
    ["catalogs", "Danh mục và đào tạo"],
    ["gradebook", "Quản lý điểm"],
    ["account", "Tài khoản"],
  ],
  TEACHER: [
    ["dashboard", "Tổng quan"],
    ["gradebook", "Nhập điểm"],
    ["account", "Tài khoản"],
  ],
  STUDENT: [
    ["dashboard", "Tổng quan"],
    ["portal", "Cổng sinh viên"],
    ["grades", "Kết quả học tập"],
    ["account", "Tài khoản"],
  ],
};

const navigation = menus[account.role] || [];

  return (
    <div className="app-layout">
      <aside className="sidebar">
        <div className="brand">
          <div className="brand-logo">SM</div>
          <div>
            <strong>Student Manager</strong>
            <span>Quản lý sinh viên</span>
          </div>
        </div>

        <nav className="sidebar-nav" aria-label="Điều hướng chính">
  {navigation.map(([page, label]) => (
    <button
      key={page}
      type="button"
      className={activePage === page ? "active" : ""}
      aria-current={activePage === page ? "page" : undefined}
      disabled={busy}
      onClick={() => setActivePage(page)}
    >
      {label}
    </button>
  ))}
</nav>

        <p className="sidebar-note">
          Vai trò: {roleNames[account.role] || account.role}
        </p>
      </aside>

      <div className="main-layout">
        <header className="topbar">
          <span>Hệ thống quản lý sinh viên</span>

          <div className="account-actions">
            <span>{account.username}</span>

            <button
              className="secondary-button"
              type="button"
              onClick={handleLogout}
              disabled={busy}
            >
              {busy ? "Đang đăng xuất..." : "Đăng xuất"}
            </button>
          </div>
        </header>

        <main className="page-content">
  {error && (
    <p className="auth-message error" role="alert">
      {error}
    </p>
  )}

  {activePage === "dashboard" && (
    <>
      <section className="welcome">
        <p className="eyebrow">
          {roleNames[account.role] || account.role}
        </p>

        <h1>Xin chào, {account.username}!</h1>

        <p>
          Chọn chức năng ở menu để bắt đầu làm việc.
        </p>
      </section>

      <DashboardPage role={account.role} />

      <section>
        <h2>Các nhóm chức năng</h2>

        <div className="feature-grid">
          {features.map((feature) => (
            <article className="feature-card" key={feature.title}>
              <h3>{feature.title}</h3>
              <p>{feature.description}</p>
            </article>
          ))}
        </div>
      </section>
    </>
  )}

  {activePage === "students" && account.role === "ADMIN" && (
    <StudentPage />
  )}

  {activePage === "catalogs" && account.role === "ADMIN" && (
    <CatalogPage />
  )}

  {activePage === "gradebook" &&
    ["ADMIN", "TEACHER"].includes(account.role) && (
      <GradebookPage role={account.role} />
    )}

  {activePage === "portal" && account.role === "STUDENT" && (
    <StudentPortalPage />
  )}

  {activePage === "grades" && account.role === "STUDENT" && (
    <StudentGradesPage />
  )}

  {activePage === "account" && (
    <section className="connection-card">
      <h2>Thông tin tài khoản</h2>

      <dl className="account-details">
        <div>
          <dt>Tên đăng nhập</dt>
          <dd>{account.username}</dd>
        </div>

        <div>
          <dt>Vai trò</dt>
          <dd>{roleNames[account.role] || account.role}</dd>
        </div>

        <div>
          <dt>Trạng thái</dt>
          <dd>{account.enabled ? "Đang hoạt động" : "Đã khóa"}</dd>
        </div>
      </dl>
    </section>
  )}
</main>

        <footer className="footer">
          Student Manager · Ứng dụng web quản lý sinh viên
        </footer>
      </div>
    </div>
  );
}