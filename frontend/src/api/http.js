export class ApiError extends Error {
  constructor(message, status) {
    super(message);
    this.name = "ApiError";
    this.status = status;
  }
}

export async function requestJson(path, options = {}) {
  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), 10000);

  try {
    const response = await fetch(path, {
      ...options,
      credentials: "include",
      headers: {
        Accept: "application/json",
        ...options.headers,
      },
      signal: controller.signal,
    });

    const text = await response.text();
    const contentType = response.headers.get("content-type") || "";

    let data = null;

    if (text && contentType.includes("application/json")) {
      try {
        data = JSON.parse(text);
      } catch {
        throw new Error("Máy chủ trả về JSON không hợp lệ.");
      }
    }

    if (!response.ok) {
      throw new ApiError(
        data?.message || `Yêu cầu thất bại: HTTP ${response.status}`,
        response.status,
      );
    }

    // Request xóa có thể thành công mà không trả về nội dung.
    if (!text) {
      return null;
    }

    if (!contentType.includes("application/json")) {
      throw new Error(
        "Máy chủ không trả về JSON. Hãy kiểm tra backend và proxy.",
      );
    }

    return data;
  } catch (error) {
    if (error.name === "AbortError") {
      throw new Error(
        "Máy chủ phản hồi quá lâu. Nếu vừa lưu hoặc xóa, hãy tải lại danh sách để kiểm tra trước khi thử lại.",{ cause: error }
      );
    }

    if (error instanceof TypeError) {
      throw new Error(
        "Mất kết nối đến máy chủ. Hãy kiểm tra backend; nếu vừa lưu hoặc xóa, hãy tải lại danh sách trước khi thử lại.",{ cause: error }
      );
    }

    throw error;
  } finally {
    clearTimeout(timeoutId);
  }
}

export function getJson(path) {
  return requestJson(path, {
    method: "GET",
  });
}

export async function sendWithCsrf(path, method, body) {
  const csrf = await getJson("/api/auth/csrf");

  if (
    typeof csrf?.headerName !== "string" ||
    typeof csrf?.token !== "string"
  ) {
    throw new Error("Không lấy được CSRF token hợp lệ.");
  }

  const headers = {
    [csrf.headerName]: csrf.token,
  };

  const options = {
    method,
    headers,
  };

  if (body !== undefined) {
    headers["Content-Type"] = "application/json";
    options.body = JSON.stringify(body);
  }

  return requestJson(path, options);
}