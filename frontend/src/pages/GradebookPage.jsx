import { useEffect, useState } from "react";
import { ApiError } from "../api/http";
import {
  getGradeSections,
  getGradebook,
  saveGradingPolicy,
  saveEnrollmentScore,
} from "../api/grades";
import { policyForm, scoreForm } from "../config/gradingForms";
import CatalogForm from "../components/CatalogForm";
import "./GradebookPage.css";

function messageOf(error) {
  if (error instanceof ApiError && error.status === 401) {
    return "Phiên đăng nhập đã hết. Hãy nhấn F5 rồi đăng nhập lại.";
  }

  return error.message;
}

function displayScore(value) {
  return value === null || value === undefined ? "—" : value;
}

function SectionPicker({
  role,
  selectedId,
  locked,
  onSelect,
}) {
  const [input, setInput] = useState("");
  const [query, setQuery] = useState({ keyword: "", page: 0 });
  const [revision, setRevision] = useState(0);
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let active = true;

    async function load() {
      setLoading(true);
      setError("");

      try {
        const result = await getGradeSections(role, query);

        if (
          !Array.isArray(result?.content) ||
          !Number.isInteger(result?.totalPages)
        ) {
          throw new Error("Danh sách lớp chưa đúng cấu trúc phân trang.");
        }

        if (active) {
          setData(result);
        }
      } catch (requestError) {
        if (active) {
          setError(messageOf(requestError));
        }
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    }

    load();

    return () => {
      active = false;
    };
  }, [role, query, revision]);

  function changePage(page) {
    onSelect("");
    setQuery((previous) => ({ ...previous, page }));
  }

  return (
    <div className="grade-picker">
      <form
        className="grade-search"
        onSubmit={(event) => {
          event.preventDefault();
          onSelect("");
          setQuery({ keyword: input.trim(), page: 0 });
          setRevision((value) => value + 1);
        }}
      >
        <div className="form-field">
          <label htmlFor="grade-section-keyword">Tìm mã lớp học phần</label>

          <input
            id="grade-section-keyword"
            value={input}
            onChange={(event) => setInput(event.target.value)}
            disabled={locked || loading}
          />
        </div>

        <button
          type="submit"
          className="primary-button"
          disabled={locked || loading}
        >
          Tìm kiếm
        </button>

        <button
          type="button"
          className="secondary-button"
          disabled={locked || loading}
          onClick={() => {
            onSelect("");
            setInput("");
            setQuery({ keyword: "", page: 0 });
            setRevision((value) => value + 1);
          }}
        >
          Bỏ lọc
        </button>
      </form>

      {loading ? (
        <p>Đang tải danh sách lớp...</p>
      ) : error ? (
        <p className="auth-message error" role="alert">{error}</p>
      ) : (
        <>
          <div className="form-field">
            <label htmlFor="grade-section">Chọn lớp học phần</label>

            <select
              id="grade-section"
              value={selectedId}
              disabled={locked}
              onChange={(event) => onSelect(event.target.value)}
            >
              <option value="">-- Chọn lớp --</option>

              {data?.content.map((section) => (
                <option key={section.id} value={section.id}>
                  {section.sectionCode}
                </option>
              ))}
            </select>
          </div>

          {data?.content.length === 0 && (
            <p>Không có lớp phù hợp hoặc bạn chưa được phân công lớp.</p>
          )}

          {data && data.totalPages > 1 && (
            <div className="grade-actions">
              <button
                type="button"
                className="secondary-button"
                disabled={locked || query.page === 0}
                onClick={() => changePage(query.page - 1)}
              >
                Trang trước
              </button>

              <span>Trang {query.page + 1} / {data.totalPages}</span>

              <button
                type="button"
                className="secondary-button"
                disabled={locked || query.page + 1 >= data.totalPages}
                onClick={() => changePage(query.page + 1)}
              >
                Trang sau
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
}

export default function GradebookPage({ role }) {
  const [sectionId, setSectionId] = useState("");
  const [book, setBook] = useState(null);
  const [revision, setRevision] = useState(0);
  const [loading, setLoading] = useState(false);
  const [loadError, setLoadError] = useState("");

  const [editor, setEditor] = useState(null);
  const [busy, setBusy] = useState(false);
  const [formError, setFormError] = useState("");
  const [success, setSuccess] = useState("");

  useEffect(() => {
    if (!sectionId) {
      return;
    }

    let active = true;

    async function load() {
      setLoading(true);
      setLoadError("");

      try {
        const result = await getGradebook(role, sectionId);

        if (!Array.isArray(result?.rows)) {
          throw new Error("Bảng điểm không đúng cấu trúc.");
        }

        if (active) {
          setBook(result);
        }
      } catch (error) {
        if (active) {
          setBook(null);
          setLoadError(messageOf(error));
        }
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    }

    load();

    return () => {
      active = false;
    };
  }, [role, sectionId, revision]);

  function selectSection(value) {
    setSectionId(value);
    setBook(null);
    setEditor(null);
    setLoadError("");
    setFormError("");
    setSuccess("");
    setLoading(Boolean(value));
  }

  function openEditor(value) {
    setEditor(value);
    setFormError("");
    setSuccess("");
  }

  async function save(body) {
    if (busy) {
      return;
    }

    setBusy(true);
    setFormError("");
    setSuccess("");

    try {
      if (editor.type === "policy") {
        await saveGradingPolicy(book.sectionId, body);
        setSuccess("Đã lưu cấu hình tỷ trọng điểm.");
      } else {
        await saveEnrollmentScore(
          role,
          editor.row.enrollmentId,
          body,
        );
        setSuccess(`Đã lưu điểm cho ${editor.row.fullName}.`);
      }

      setEditor(null);
      setRevision((value) => value + 1);
    } catch (error) {
      setFormError(messageOf(error));
    } finally {
      setBusy(false);
    }
  }

  const locked = busy || loading || editor !== null;

  return (
    <section id="gradebook" className="grade-panel">
      <h2>{role === "ADMIN" ? "Quản lý điểm" : "Nhập điểm lớp phụ trách"}</h2>

      <SectionPicker
        role={role}
        selectedId={sectionId}
        locked={locked}
        onSelect={selectSection}
      />

      {success && (
        <p className="auth-message success" role="status">{success}</p>
      )}

      {loading && <p role="status">Đang tải bảng điểm...</p>}

      {!loading && loadError && (
        <p className="auth-message error" role="alert">{loadError}</p>
      )}

      {sectionId && !loading && (
        <button
          type="button"
          className="secondary-button grade-reload"
          disabled={busy || editor !== null}
          onClick={() => setRevision((value) => value + 1)}
        >
          Tải lại bảng điểm
        </button>
      )}

      {!loading && book && (
        <>
          <div className="grade-summary">
            <h3>{book.sectionCode} — {book.subjectName}</h3>
            <p>{book.semester}</p>

            <p>
              TX1: <strong>{book.tx1Weight}%</strong>
              {" · "}TX2: <strong>{book.tx2Weight}%</strong>
              {" · "}KTHP: <strong>{book.kthpWeight}%</strong>
            </p>

            <p>
              {book.gradingConfigured
                ? "Đã xác nhận cấu hình điểm."
                : "Chưa xác nhận cấu hình điểm. ADMIN cần cấu hình trước khi nhập điểm."}
            </p>

            {role === "ADMIN" && (
              <button
                type="button"
                className="secondary-button"
                disabled={locked}
                onClick={() => openEditor({ type: "policy" })}
              >
                Cấu hình tỷ trọng
              </button>
            )}
          </div>

          {editor && (
            <>
              {editor.type === "score" && (
                <h3>
                  {editor.row.studentCode} — {editor.row.fullName}
                </h3>
              )}

              <CatalogForm
                key={`${editor.type}-${editor.row?.enrollmentId ?? book.sectionId}`}
                config={editor.type === "policy" ? policyForm : scoreForm}
                item={editor.type === "policy" ? book : editor.row.score}
                lookups={{}}
                busy={busy}
                error={formError}
                onSave={save}
                onCancel={() => {
                  setEditor(null);
                  setFormError("");
                }}
              />
            </>
          )}

          <div className="grade-table-wrapper">
            <table className="grade-table">
              <thead>
                <tr>
                  <th scope="col">Mã sinh viên</th>
                  <th scope="col">Họ tên</th>
                  <th scope="col">TX1</th>
                  <th scope="col">TX2</th>
                  <th scope="col">KTHP</th>
                  <th scope="col">Tổng kết</th>
                  <th scope="col">Điểm chữ</th>
                  <th scope="col">Thao tác</th>
                </tr>
              </thead>

              <tbody>
                {book.rows.map((row) => (
                  <tr key={row.enrollmentId}>
                    <td>{row.studentCode}</td>
                    <td>{row.fullName}</td>
                    <td>{displayScore(row.score?.tx1Score)}</td>
                    <td>{displayScore(row.score?.tx2Score)}</td>
                    <td>{displayScore(row.score?.kthpScore)}</td>
                    <td>{displayScore(row.score?.averageScore)}</td>
                    <td>{row.score?.letterGrade || "Chưa có điểm"}</td>

                    <td>
                      <button
                        type="button"
                        className="secondary-button"
                        disabled={locked || !book.gradingConfigured}
                        onClick={() => openEditor({ type: "score", row })}
                      >
                        {row.score ? "Sửa điểm" : "Nhập điểm"}
                      </button>
                    </td>
                  </tr>
                ))}

                {book.rows.length === 0 && (
                  <tr>
                    <td colSpan={8}>
                      Chưa có đăng ký còn hiệu lực trong lớp này.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </>
      )}
    </section>
  );
}