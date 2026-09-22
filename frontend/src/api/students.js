import { getJson, sendWithCsrf } from "./http";

export function getStudents({
  keyword = "",
  classId = "",
  page = 0,
  size = 10,
}) {
  const params = new URLSearchParams();

  params.set("keyword", keyword);
  params.set("page", String(page));
  params.set("size", String(size));

  if (classId) {
    params.set("classId", String(classId));
  }

  return getJson(`/api/students?${params.toString()}`);
}

export const CLASS_API = "/api/classes";

export function getAdministrativeClasses() {
  return getJson(CLASS_API);
}

export function createStudent(data) {
  return sendWithCsrf("/api/students", "POST", data);
}

export function updateStudent(id, data) {
  return sendWithCsrf(`/api/students/${id}`, "PUT", data);
}

export function deleteStudent(id) {
  return sendWithCsrf(`/api/students/${id}`, "DELETE");
}