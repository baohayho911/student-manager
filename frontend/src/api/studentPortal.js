import { getJson, sendWithCsrf } from "./http";

export function getMyProfile() {
  return getJson("/api/me/portal/profile");
}

export function getOpenSections({ keyword = "", page = 0 }) {
  const params = new URLSearchParams({
    keyword,
    page: String(page),
    size: "10",
  });

  return getJson(`/api/me/portal/sections?${params.toString()}`);
}

export function getMyEnrollments() {
  return getJson("/api/me/portal/enrollments");
}

export function registerSection(courseSectionId) {
  return sendWithCsrf("/api/me/enrollments", "POST", {
    courseSectionId,
  });
}

export function cancelMyEnrollment(enrollmentId) {
  return sendWithCsrf(
    `/api/me/enrollments/${enrollmentId}/cancel`,
    "PATCH",
  );
}