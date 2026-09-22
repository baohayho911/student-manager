import { useEffect, useState } from "react";
import StudentForm from "../components/StudentForm";
import { ApiError } from "../api/http";
import {
  getStudents,
  getAdministrativeClasses,
  createStudent,
  updateStudent,
  deleteStudent,
} from "../api/students";
import "./StudentPage.css";

function errorMessage(error) {
  if (error instanceof ApiError && error.status === 401) {
    return "Phiên đăng nhập đã hết. Hãy nhấn F5 và đăng nhập lại.";
  }

  if (error instanceof ApiError && error.status === 403) {
    return "Yêu cầu bị từ chối. Hãy nhấn F5 và kiểm tra bạn đang đăng nhập ADMIN.";
  }

  return error.message;
}

export default function StudentPage() {
  const [classes, setClasses] = useState([]);
  const [classesLoading, setClassesLoading] = useState(true);
  const [classesError, setClassesError] = useState("");

  const [keywordInput, setKeywordInput] = useState("");
  const [classInput, setClassInput] = useState("");

  const [filters, setFilters] = useState({
    keyword: "",
    classId: "",
    page: 0,
    size: 10,
  });

  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(true);
  const [listError, setListError] = useState("");
  const [reload, setReload] = useState(0);

  const [editor, setEditor] = useState(null);
  const [busy, setBusy] = useState(false);
  const [formError, setFormError] = useState("");
  const [actionError, setActionError] = useState("");
  const [success, setSuccess] = useState("");

  useEffect(() => {
    let active = true;

    async function loadClasses() {
      try {
        const data = await getAdministrativeClasses();

        if (!Array.isArray(data)) {
          throw new Error("API lớp hành chính chưa trả về danh sách dạng mảng.");
        }

        if (active) {
          setClasses(data);
        }
      } catch (error) {
        if (active) {
          setClassesError(errorMessage(error));
        }
      } finally {
        if (active) {
          setClassesLoading(false);
        }
      }
    }

    loadClasses();

    return () => {
      active = false;
    };
  }, []);

  useEffect(() => {
    let active = true;

    async function loadStudents() {
      setLoading(true);
      setListError("");

      try {
        const data = await getStudents(filters);

        if (
          !Array.isArray(data?.content) ||
          !Number.isInteger(data?.totalPages) ||
          !Number.isInteger(data?.totalElements)
        ) {
          throw new Error(
            "API sinh viên chưa trả về cấu trúc phân trang của bài 13.",
          );
        }

        if (!active) {
          return;
        }

        // Ví dụ trang cuối không còn dữ liệu sau khi một bản ghi bị xóa.
        if (filters.page > 0 && filters.page >= data.totalPages) {
          setFilters((previous) => ({
            ...previous,
            page: Math.max(0, data.totalPages - 1),
          }));
          return;
        }

        setResult(data);
      } catch (error) {
        if (active) {
          setListError(errorMessage(error));
          setResult(null);
        }
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    }

    loadStudents();

    return () => {
      active = false;
    };
  }, [filters, reload]);

  function clearMessages() {
    setSuccess("");
    setActionError("");
    setFormError("");
  }

  function handleSearch(event) {
    event.preventDefault();
    clearMessages();

    setFilters((previous) => ({
      ...previous,
      keyword: keywordInput.trim(),
      classId: classInput,
      page: 0,
    }));
  }

  function resetFilters() {
    setKeywordInput("");
    setClassInput("");
    clearMessages();

    setFilters((previous) => ({
      ...previous,
      keyword: "",
      classId: "",
      page: 0,
    }));
  }

  function openCreate() {
    clearMessages();
    setEditor({ student: null });
  }

  function openEdit(student) {
    clearMessages();
    setEditor({ student });
  }

  async function handleSave(data) {
    if (busy) {
      return;
    }

    const editingId = editor?.student?.id;

    setBusy(true);
    clearMessages();

    try {
      if (editingId) {
        await updateStudent(editingId, data);
      } else {
        await createStudent(data);
      }

      setEditor(null);
      setSuccess(
        editingId
          ? "Đã cập nhật sinh viên thành công."
          : "Đã thêm sinh viên thành công. Bạn có thể tìm bằng mã sinh viên.",
      );

      setReload((value) => value + 1);
    } catch (error) {
      setFormError(errorMessage(error));
    } finally {
      setBusy(false);
    }
  }

  async function handleDelete(student) {
    if (busy) {
      return;
    }

    const confirmed = window.confirm(
      `Bạn có chắc muốn xóa sinh viên ${student.studentCode} - ${student.fullName}?`,
    );

    if (!confirmed) {
      return;
    }

    setBusy(true);
    clearMessages();

    try {
      await deleteStudent(student.id);

      setSuccess(`Đã xóa sinh viên ${student.studentCode}.`);
      setReload((value) => value + 1);
    } catch (error) {
      setActionError(errorMessage(error));
    } finally {
      setBusy(false);
    }
  }

  function classLabel(classId) {
    const item = classes.find(
      (classItem) => classItem.id === classId,
    );

    return item ? item.classCode : `Lớp #${classId}`;
  }

  const controlsDisabled = busy || loading || editor !== null;
  const canOpenForm =
    !classesLoading && !classesError && classes.length > 0;

  return (
    <section id="students" className="students-panel">
      <div className="students-heading">
        <div>
          <h2>Quản lý sinh viên</h2>
          <p>Tra cứu và cập nhật hồ sơ sinh viên.</p>
        </div>

        <button
          type="button"
          className="primary-button"
          onClick={openCreate}
          disabled={controlsDisabled || !canOpenForm}
        >
          + Thêm sinh viên
        </button>
      </div>

      {classesLoading && <p>Đang tải danh sách lớp...</p>}

      {classesError && (
        <p className="auth-message error" role="alert">
          Không tải được lớp hành chính: {classesError}
          {" "}Hãy kiểm tra backend rồi tải lại trang.
        </p>
      )}

      {!classesLoading && !classesError && classes.length === 0 && (
        <p className="auth-message error" role="alert">
          Chưa có lớp hành chính. Cần tạo lớp trước khi thêm sinh viên.
        </p>
      )}

      {success && (
        <p className="auth-message success" role="status">
          {success}
        </p>
      )}

      {actionError && (
        <p className="auth-message error" role="alert">
          {actionError}
        </p>
      )}

      {editor && (
        <StudentForm
          key={editor.student?.id ?? "new"}
          student={editor.student}
          classes={classes}
          busy={busy}
          error={formError}
          onSave={handleSave}
          onCancel={() => {
            setEditor(null);
            setFormError("");
          }}
        />
      )}

      <form onSubmit={handleSearch} className="student-filters">
        <div className="form-field">
          <label htmlFor="student-search">Mã sinh viên hoặc họ tên</label>

          <input
            id="student-search"
            type="search"
            value={keywordInput}
            onChange={(event) => setKeywordInput(event.target.value)}
            placeholder="Nhập từ khóa..."
            disabled={controlsDisabled}
          />
        </div>

        <div className="form-field">
          <label htmlFor="student-class-filter">Lớp hành chính</label>

          <select
            id="student-class-filter"
            value={classInput}
            onChange={(event) => setClassInput(event.target.value)}
            disabled={controlsDisabled || classesLoading || !!classesError}
          >
            <option value="">Tất cả lớp</option>

            {classes.map((item) => (
              <option key={item.id} value={item.id}>
                {item.classCode} - {item.className}
              </option>
            ))}
          </select>
        </div>

        <div className="student-actions">
          <button
            className="primary-button"
            type="submit"
            disabled={controlsDisabled}
          >
            Tìm kiếm
          </button>

          <button
            className="secondary-button"
            type="button"
            onClick={resetFilters}
            disabled={controlsDisabled}
          >
            Bỏ lọc
          </button>
        </div>
      </form>

      <div className="student-list-tools">
        <label htmlFor="student-page-size">Số dòng mỗi trang:</label>

        <select
          id="student-page-size"
          value={filters.size}
          disabled={controlsDisabled}
          onChange={(event) => {
            const size = Number(event.target.value);

            setFilters((previous) => ({
              ...previous,
              size,
              page: 0,
            }));
          }}
        >
          <option value={5}>5</option>
          <option value={10}>10</option>
          <option value={20}>20</option>
        </select>

        <button
          type="button"
          className="secondary-button"
          onClick={() => setReload((value) => value + 1)}
          disabled={busy || loading}
        >
          Tải lại danh sách
        </button>
      </div>

      {loading ? (
        <p role="status">Đang tải danh sách sinh viên...</p>
      ) : listError ? (
        <p className="auth-message error" role="alert">
          {listError}
        </p>
      ) : (
        <>
          <p>Tổng số: <strong>{result?.totalElements ?? 0}</strong> sinh viên</p>

          <div className="student-table-wrapper">
            <table className="student-table">
              <thead>
                <tr>
                  <th scope="col">STT</th>
                  <th scope="col">Mã sinh viên</th>
                  <th scope="col">Họ và tên</th>
                  <th scope="col">Lớp</th>
                  <th scope="col">Email</th>
                  <th scope="col">Điện thoại</th>
                  <th scope="col">Thao tác</th>
                </tr>
              </thead>

              <tbody>
                {result?.content.map((student, index) => (
                  <tr key={student.id}>
                    <td>{filters.page * filters.size + index + 1}</td>
                    <td>{student.studentCode}</td>
                    <td>{student.fullName}</td>
                    <td>{classLabel(student.classId)}</td>
                    <td>{student.email || "—"}</td>
                    <td>{student.phone || "—"}</td>

                    <td>
                      <div className="student-actions">
                        <button
                          type="button"
                          className="secondary-button"
                          onClick={() => openEdit(student)}
                          disabled={controlsDisabled || !canOpenForm}
                        >
                          Sửa
                        </button>

                        <button
                          type="button"
                          className="danger-button"
                          onClick={() => handleDelete(student)}
                          disabled={controlsDisabled}
                        >
                          Xóa
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}

                {result?.content.length === 0 && (
                  <tr>
                    <td colSpan={7} className="student-empty">
                      Không tìm thấy sinh viên phù hợp.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>

          {result && result.totalPages > 0 && (
            <div className="student-pagination">
              <button
                type="button"
                className="secondary-button"
                disabled={controlsDisabled || filters.page === 0}
                onClick={() =>
                  setFilters((previous) => ({
                    ...previous,
                    page: previous.page - 1,
                  }))
                }
              >
                Trang trước
              </button>

              <span>
                Trang {filters.page + 1} / {result.totalPages}
              </span>

              <button
                type="button"
                className="secondary-button"
                disabled={
                  controlsDisabled ||
                  filters.page + 1 >= result.totalPages
                }
                onClick={() =>
                  setFilters((previous) => ({
                    ...previous,
                    page: previous.page + 1,
                  }))
                }
              >
                Trang sau
              </button>
            </div>
          )}
        </>
      )}
    </section>
  );
}