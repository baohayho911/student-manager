import { getJson, requestJson } from "./http";

async function getCsrf() {
  const csrf = await getJson("/api/auth/csrf");

  if (
    typeof csrf?.headerName !== "string" ||
    typeof csrf?.token !== "string"
  ) {
    throw new Error("Dữ liệu CSRF token không hợp lệ.");
  }

  return csrf;
}

export function getCurrentAccount() {
  return getJson("/api/auth/me");
}

export async function login(username, password) {
  const csrf = await getCsrf();

  const body = new URLSearchParams();
  body.set("username", username.trim());
  body.set("password", password);

  await requestJson("/api/auth/login", {
    method: "POST",
    headers: {
      "Content-Type": "application/x-www-form-urlencoded",
      [csrf.headerName]: csrf.token,
    },
    body: body.toString(),
  });

  // Token trước đăng nhập đã bị xóa: lấy token mới.
  await getCsrf();

  return getCurrentAccount();
}

export async function logout() {
  const csrf = await getCsrf();

  return requestJson("/api/auth/logout", {
    method: "POST",
    headers: {
      [csrf.headerName]: csrf.token,
    },
  });
}