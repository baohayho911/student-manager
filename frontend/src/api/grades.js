import { getJson, sendWithCsrf } from "./http";

export function getGradeSections(role, { keyword = "", page = 0 }) {
  const url =
    role === "ADMIN"
      ? "/api/course-sections"
      : "/api/teaching/sections";

  const params = new URLSearchParams({
    keyword,
    page: String(page),
    size: "10",
  });

  return getJson(`${url}?${params.toString()}`);
}

export function getGradebook(role, sectionId) {
  const prefix =
    role === "ADMIN"
      ? "/api/admin/gradebook"
      : "/api/teaching/gradebook";

  return getJson(`${prefix}/${sectionId}`);
}

export function saveGradingPolicy(sectionId, body) {
  return sendWithCsrf(
    `/api/scores/policy/${sectionId}`,
    "PUT",
    body,
  );
}

export function saveEnrollmentScore(role, enrollmentId, body) {
  const url =
    role === "ADMIN"
      ? `/api/scores/enrollment/${enrollmentId}`
      : `/api/teaching/enrollments/${enrollmentId}/score`;

  return sendWithCsrf(url, "PUT", body);
}

export function getMyGradeResults() {
  return getJson("/api/me/grade-results");
}