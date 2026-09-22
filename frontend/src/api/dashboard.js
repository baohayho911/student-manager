import { getJson } from "./http";

const endpoints = {
  ADMIN: "/api/admin/dashboard",
  TEACHER: "/api/teaching/dashboard",
  STUDENT: "/api/me/dashboard",
};

export function getDashboard(role) {
  const endpoint = endpoints[role];

  if (!endpoint) {
    throw new Error("Vai trò không được hỗ trợ.");
  }

  return getJson(endpoint);
}